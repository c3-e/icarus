package com.instaclustr.icarus.operations.rebuild;

import static java.lang.String.format;
import static java.util.stream.Collectors.joining;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.instaclustr.operations.FunctionWithEx;
import com.instaclustr.operations.Operation;
import jmx.org.apache.cassandra.service.CassandraJMXService;
import jmx.org.apache.cassandra.service.cassandra3.StorageServiceMBean;

public class RebuildOperation extends Operation<RebuildOperationRequest> {

    private final CassandraJMXService cassandraJMXService;

    @Inject
    public RebuildOperation(final CassandraJMXService cassandraJMXService,
                            @Assisted final RebuildOperationRequest request) {
        super(request);
        this.cassandraJMXService = cassandraJMXService;
    }

    // this constructor is not meant to be instantiated manually
    // and it fulfills the purpose of deserialisation from JSON string to an Operation object, currently just for testing purposes
    @JsonCreator
    private RebuildOperation(@JsonProperty("type") final String type,
                             @JsonProperty("id") final UUID id,
                             @JsonProperty("creationTime") final Instant creationTime,
                             @JsonProperty("state") final State state,
                             @JsonProperty("errors") final List<Error> errors,
                             @JsonProperty("progress") final float progress,
                             @JsonProperty("startTime") final Instant startTime,
                             @JsonProperty("sourceDC") final String sourceDC,
                             @JsonProperty("keyspace") final String keyspace,
                             @JsonProperty("specificTokens") final Set<RebuildOperationRequest.TokenRange> specificTokens,
                             @JsonProperty("specificSources") final Set<String> specificSources) {
        super(type, id, creationTime, state, errors, progress, startTime, new RebuildOperationRequest(type,
                                                                                                      sourceDC,
                                                                                                      keyspace,
                                                                                                      specificTokens,
                                                                                                      specificSources));
        cassandraJMXService = null;
    }

    @Override
    protected void run0() throws Exception {
        assert cassandraJMXService != null;

        final String specificTokens = prepareSpecificTokens(request.specificTokens);
        final String specificSources = prepareSpecificSources(request.specificSources);

        cassandraJMXService.doWithStorageServiceMBean(new FunctionWithEx<StorageServiceMBean, Void>() {
            @Override
            public Void apply(final StorageServiceMBean object) {

                object.rebuild(request.sourceDC,
                               request.keyspace,
                               specificTokens,
                               specificSources);

                return null;
            }
        });
    }

    private String prepareSpecificTokens(Set<RebuildOperationRequest.TokenRange> specificTokens) {
        if (specificTokens == null || specificTokens.isEmpty()) {
            return null;
        }

        return specificTokens.stream().map(token -> format("(%s,%s]", token.start, token.end)).collect(joining(","));
    }

    private String prepareSpecificSources(Set<String> specificSources) {
        if (specificSources == null || specificSources.isEmpty()) {
            return null;
        }

        return String.join(",", specificSources);
    }
}
