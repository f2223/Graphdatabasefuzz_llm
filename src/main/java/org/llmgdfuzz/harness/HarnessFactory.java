package org.llmgdfuzz.harness;



public class HarnessFactory {
    public static Harness select(String graphdb) {
        switch (graphdb.toLowerCase()) {
            case "neo4j":
                return new Neo4jHarness();
            default:
                return new Neo4jHarness();
        }
    }
}
