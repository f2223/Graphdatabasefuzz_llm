package org.llmgdfuzz.harness;

import org.neo4j.graphdb.*;

public interface Harness {
    GraphDatabaseService startEmbeddedDB(String dbDirPath) throws Exception;

    void executeCypher(GraphDatabaseService db, String cypher);

    void shutdown();
}
