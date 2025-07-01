package org.llmgdfuzz;

import org.neo4j.graphdb.GraphDatabaseService;
import org.kohsuke.args4j.CmdLineException;
import org.llmgdfuzz.Coverage.CoverageTracker;
import org.llmgdfuzz.harness.Neo4jHarness;
import org.llmgdfuzz.initgd.Randomgraph;
import org.llmgdfuzz.Cypher.Cypher;
import org.llmgdfuzz.Cypher.GraphDBFactory;
import org.llmgdfuzz.harness.Harness;
import org.llmgdfuzz.harness.HarnessFactory;
import org.llmgdfuzz.tools.Normal_Distribution;
import org.llmgdfuzz.key.key;
import org.llmgdfuzz.key.keyFactory;

import org.neo4j.graphdb.*;

import org.neo4j.dbms.api.*;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.llmgdfuzz.llm.llm_call.generateCypherQueries;

public class test {
    public static void main(String[] args) throws Exception {
        /*Randomgraph geng = new Randomgraph();
        geng.randomgen();
        System.out.println(geng.getTagattribute());
        System.out.println(geng.getNodenum());
        System.out.println(geng.getRelattribute());
        System.out.println(geng.getTagattribute().size());
        System.out.println(geng.getNodenum().size());
        System.out.println(geng.getRelattribute().keySet());
        System.out.println(geng.getpathln());
        System.out.println(geng.getPaths());
        Random random = new Random();*/

        //CoverageTracker tracker = new CoverageTracker();


        //String path = "/home/neo4j_Cypher/neo4j/packaging/standalone/target/neo4j-community-5.20.0-SNAPSHOT/bin/neo4j";
        /*String path = "data";
        Harness har =HarnessFactory.select("neo4j");
        GraphDatabaseService db = har.startEmbeddedDB(path);
        Cypher cdb = GraphDBFactory.select("neo4j");
        cdb.database_clean(db);
        cdb.database_creat(geng, db);*/
        //Driver driver = Neo4jHarness.startAndConnect(path);

        /*Neo4jHarness.executeCypher(db, "CREATE (n:Person {name: 'Alice'})");

        int newCoverage = tracker.getNewCoverageCount();
        if (newCoverage > 0) {
            System.out.println("[+] Found new coverage in iteration " + newCoverage);
            // 可以保存对应输入或加入 corpus
        }

        Neo4jHarness.executeCypher(db, "MATCH (n:Person) RETURN n");

        newCoverage = tracker.getNewCoverageCount();
        if (newCoverage > 0) {
            System.out.println("[+] Found new coverage in iteration " + newCoverage);
            // 可以保存对应输入或加入 corpus
        }*/

        /*try (Transaction tx = db.beginTx()) {

            System.out.println("=== 所有节点 ===");
            for (Node node : tx.getAllNodes()) {
                System.out.println("Node ID: " + node.getElementId());
                System.out.print("Labels: ");
                for (Label label : node.getLabels()) {
                    System.out.print(label.name() + " ");
                }
                System.out.println();
                System.out.println("Properties:");
                for (String key : node.getPropertyKeys()) {
                    System.out.println("  " + key + ": " + node.getProperty(key));
                }
                System.out.println();
            }

            System.out.println("=== 所有关系 ===");
            for (Relationship rel : tx.getAllRelationships()) {
                System.out.println("Relationship ID: " + rel.getElementId());
                System.out.println("Type: " + rel.getType().name());
                System.out.println("Start Node ID: " + rel.getStartNode().getElementId());
                System.out.println("End Node ID: " + rel.getEndNode().getElementId());
                System.out.println("Properties:");
                for (String key : rel.getPropertyKeys()) {
                    System.out.println("  " + key + ": " + rel.getProperty(key));
                }
                System.out.println();
            }

            tx.commit();
        }

        har.shutdown();*/
        /*String task = "找出所有在 2023 年发帖并获得超过10个点赞的用户名称";
        String schema = "(:User)-[:POSTED]->(:Post), (:User)-[:LIKED]->(:Post)";
        String cpath = "查询路径设置为从user经过posted到post";
        List<String> queries = generateCypherQueries(task, schema, cpath, 3);
        for (int i = 0; i < queries.size(); i++) {
            System.out.println("Query " + (i + 1) + ":\n" + queries.get(i));
            System.out.println("--------");
        }*/

        /*key ks = keyFactory.select("neo4j");
        int cou = 0;
        while (cou < 50){
            System.out.println(ks.getNextKeyCombo());
            cou++;
        }*/

        File targetDir = new File("data"); // 替换为你的路径
        deleteAllInDirectory(targetDir);



    }
    public static void deleteAllInDirectory(File dir) {
        if (dir == null || !dir.exists() || !dir.isDirectory()) {
            System.err.println("目标不是有效的文件夹: " + dir);
            return;
        }

        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                deleteRecursively(file);
            }
        }
    }

    private static void deleteRecursively(File file) {
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null) {
                for (File child : children) {
                    deleteRecursively(child);
                }
            }
        }
        if (!file.delete()) {
            System.err.println("删除失败: " + file.getAbsolutePath());
        }
    }
}
