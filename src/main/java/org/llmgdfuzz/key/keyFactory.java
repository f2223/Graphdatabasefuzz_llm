package org.llmgdfuzz.key;



public class keyFactory {

    public static key select(String graphdb) {
        switch (graphdb.toLowerCase()) {
            case "neo4j":
                return new Neo4jKey();
            default:
                return new Neo4jKey();
        }
    }

}
