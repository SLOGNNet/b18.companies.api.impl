package com.bridge18.company.impl.core;

import com.datastax.driver.core.BoundStatement;
import com.lightbend.lagom.javadsl.persistence.cassandra.CassandraReadSide;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletionStage;

public class CassandraReadSideUtils {
    public static CompletionStage<List<BoundStatement>> completedStatements(BoundStatement... statements) {
        return CassandraReadSide.completedStatements(Arrays.asList(statements));
    }
}
