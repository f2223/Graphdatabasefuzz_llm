package org.llmgdfuzz.Cypher;

public class GraphDBFactory {


    public static Cypher select(String graphdb) {
        switch (graphdb.toLowerCase()) {
            case "neo4j":
                return new neo4j_cypher();
            default:
                return new neo4j_cypher();
        }
    }

}
