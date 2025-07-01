package org.llmgdfuzz.harness;

import org.neo4j.graphdb.*;

import org.neo4j.dbms.api.*;

//import org.neo4j.driver.*;

import java.nio.file.Path;

public class Neo4jHarness implements Harness{


    private DatabaseManagementService managementService;

    public GraphDatabaseService startEmbeddedDB(String dbDirPath) throws Exception {
        Path dbPath = Path.of(dbDirPath);
        managementService = new DatabaseManagementServiceBuilder(dbPath).build();
        System.out.println("[INFO] Embedded Neo4j started at " + dbPath.toAbsolutePath());
        return managementService.database("neo4j");
    }


    public void executeCypher(GraphDatabaseService db, String cypher) {
        try (Transaction tx = db.beginTx()) {
            Result result = tx.execute(cypher);
            tx.commit();
            System.out.println("[INFO] Executed Cypher: " + cypher);
        } catch (Exception e) {
            System.err.println("[ERROR] Failed to execute Cypher: " + cypher);
            //e.printStackTrace();
        }
    }

    public void shutdown() {
        if (managementService != null) {
            managementService.shutdown();
            System.out.println("[INFO] Embedded Neo4j shut down.");
        }
    }
}
