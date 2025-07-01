package org.llmgdfuzz.Cypher;

import org.llmgdfuzz.initgd.Randomgraph;

import org.neo4j.graphdb.*;

public interface Cypher {

    void database_creat(Randomgraph c_graph, GraphDatabaseService db);

    void database_clean(GraphDatabaseService db);
}
