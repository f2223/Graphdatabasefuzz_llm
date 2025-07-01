package org.llmgdfuzz;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.CmdLineParser;

import java.io.IOException;
import java.util.*;

import org.llmgdfuzz.Cypher.Cypher;
import org.llmgdfuzz.Cypher.GraphDBFactory;
import org.llmgdfuzz.harness.Harness;
import org.llmgdfuzz.harness.HarnessFactory;
import org.llmgdfuzz.key.key;
import org.llmgdfuzz.key.keyFactory;
import org.llmgdfuzz.Coverage.CoverageTracker;
import org.neo4j.graphdb.GraphDatabaseService;
import java.io.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

import org.llmgdfuzz.initgd.Randomgraph;
import org.llmgdfuzz.tools.Normal_Distribution;

import static org.llmgdfuzz.llm.llm_call.generateCypherQueries;

public class Main {

    @Option(name = "-gd", aliases = "--graphdatabase", required	= false, usage = "Specify the graph database for fuzzing")
    private String gdname = "neo4j";

    @Option(name = "-o", aliases = "--crashdir", required	= false, usage = "Crash Folder")
    private String outputdir = "./output";

    private static String[] gdlist = {"neo4j", "redisGraph"};

    private static final String crashDir = "crashes/";
    private static int crashCount = 0;

    private static final int MAX_QUERIES_PER_GRAPH = 100;
    private static BlockingQueue<String> inputPool = new LinkedBlockingQueue<>(MAX_QUERIES_PER_GRAPH);
    private static final int QUERIES_PER_GRAPH_GENERATION = 20;

    private static AtomicBoolean generatorPaused = new AtomicBoolean(false);

    private static Randomgraph geng;

    private static final ExecutorService timeoutExecutor = Executors.newCachedThreadPool();

    private static GraphDatabaseService db;

    private static String SchemaDescription = "";

    public static void main(String[] args) throws CmdLineException, IOException {
        new Main().fuzz(args);
    }

    public void fuzz(String[] args) throws IOException {
        CmdLineParser parser = new CmdLineParser(this);

        try {
            parser.parseArgument(args);

            if(!Arrays.asList(gdlist).contains(gdname)){
                gdname = "neo4j";
            }

            String dbPath = "data";
            Harness har = HarnessFactory.select(gdname);
            db = har.startEmbeddedDB(dbPath);
            Cypher cdb = GraphDBFactory.select(gdname);
            key keygen = keyFactory.select(gdname);
            CoverageTracker ct = new CoverageTracker();

            ExecutorService executor = Executors.newFixedThreadPool(2);

            executor.submit(() -> generateLoop(cdb, dbPath, keygen));
            executor.submit(() -> executeLoop(har, cdb));

            executor.shutdown();
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);

            timeoutExecutor.shutdown();
            timeoutExecutor.awaitTermination(60, TimeUnit.SECONDS);


        } catch (Exception e) {
            parser.printUsage(System.out);
        }
    }

    private static void generateLoop(Cypher cdb, String dbPath, key keygen) {
        while (true) {
            try {
                while (!inputPool.isEmpty()) {
                    Thread.sleep(1000);
                }

                if (inputPool.remainingCapacity() < QUERIES_PER_GRAPH_GENERATION) {
                    generatorPaused.set(true);
                    Thread.sleep(500); // 暂停
                    continue;
                }

                generatorPaused.set(false);
                geng = new Randomgraph();
                geng.randomgen();

                getSchema();

                cdb.database_clean(db);
                cdb.database_creat(geng, db);

                for (int i = 0; i < 3; i++) {
                    String taskDescription = getTaskDescription(keygen);
                    String cpath = getpath();

                    int diversityLevel = 20;

                    List<String> queries = generateCypherQueries(taskDescription, SchemaDescription, cpath, diversityLevel);

                    for (String query : queries) {
                        if (query == null || query.trim().isEmpty()) continue;
                        inputPool.put(query);
                        if (inputPool.remainingCapacity() == 0) break;
                    }
                }

            } catch (Exception e) {
                System.err.println("生成阶段异常: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private static void executeLoop(Harness har, Cypher cdb) {
        while (true) {
            try {
                while (inputPool.isEmpty()) {
                    Thread.sleep(1000);
                }
                String input = inputPool.take();
                try {
                    GraphDatabaseService db2 = db;
                    Future<?> future = timeoutExecutor.submit(() -> {
                        har.executeCypher(db2, input);
                    });
                    future.get(30, TimeUnit.SECONDS);
                } catch (TimeoutException te) {
                    saveCrash(input, new Exception("查询执行超时"));
                    try {
                        har.shutdown();
                        File targetDir = new File("data");
                        deleteAllInDirectory(targetDir);
                        db = har.startEmbeddedDB("data");
                        cdb.database_clean(db);
                        cdb.database_creat(geng, db);
                    } catch (Exception er){
                        System.out.println("重启数据库失败！");
                    }
                }catch (Throwable e) {
                    if (isSevereCrash(e)) {
                        saveCrash(input, e);
                        try {
                            har.shutdown();
                            File targetDir = new File("data");
                            deleteAllInDirectory(targetDir);
                            db = har.startEmbeddedDB("data");
                            cdb.database_clean(db);
                            cdb.database_creat(geng, db);
                        } catch (Exception er){
                            System.out.println("重启数据库失败！");
                        }
                    }
                }
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                System.err.println("执行阶段异常: " + e.getMessage());
                e.printStackTrace();
            }
        }
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

    private static boolean isSevereCrash(Throwable e) {
        String msg = e.getClass().getSimpleName();
        return !(msg.contains("SyntaxError") || msg.contains("ClientException") || msg.contains("QueryExecutionException"));
    }

    private static void saveCrash(String query, Throwable e) {
        try {
            String fileName = "crashes/crash-" + (crashCount++) + ".txt";
            try (PrintWriter writer = new PrintWriter(new FileWriter(fileName))) {
                writer.println("Crash Query:\n" + query);
                writer.println("\nGraph Tag Attribute:\n" + geng.getTagattribute());
                writer.println("\nNode Count:\n" + geng.getNodenum());
                writer.println("\nRelationship Attributes:\n" + geng.getRelattribute());
                writer.println("\nException Stack Trace:");
                e.printStackTrace(writer);
            }
            System.out.println("[Executor] Crash saved to " + fileName);
        } catch (IOException ex) {
            System.err.println("[Executor] Failed to save crash.");
            ex.printStackTrace();
        }
    }

    private static ArrayList<Integer> generatepath(){
        Random random = new Random();
        Normal_Distribution brandom = new Normal_Distribution();
        ArrayList<Integer> abrpath = new ArrayList<Integer>();
        int tagunum = geng.getTagattribute().size();
        int select = random.nextInt(10);
        if(select == 0) {
            abrpath.add(0);
            abrpath.add(random.nextInt(tagunum));
        } else if(select <=5) {
            abrpath.add(1);
            int stratnode = -1;
            do {
                stratnode = random.nextInt(tagunum);
            } while (geng.getpathln().get(stratnode) <= 0);
            int hopn = brandom.generateBiasedRandomr(random, 3, 1, geng.getpathln().get(stratnode));

            ArrayList<ArrayList<Integer>> longLists = new ArrayList<>();
            for (ArrayList<Integer> sublist : geng.getPaths().get(stratnode)) {
                if (sublist.size() > hopn) {
                    longLists.add(sublist);
                }
            }

            if (!longLists.isEmpty()) {
                ArrayList<Integer> selected = longLists.get(random.nextInt(longLists.size()));
                List<Integer> path = selected.subList(0, hopn + 1);
                abrpath.addAll(path);
            } else {
                abrpath.add(stratnode);
            }
        } else {
            abrpath.add(2);
            int selectnode = -1;
            do {
                selectnode = random.nextInt(tagunum);
            } while (geng.getpathln().get(selectnode) > 0 && geng.getreversepathln().get(selectnode) > 0);
            abrpath.add(selectnode);
            if (geng.getpathln().get(selectnode) > 0) {
                abrpath.add(1 + random.nextInt(geng.getpathln().get(selectnode) - 1));
            }
            else {
                abrpath.add(0);
            }
            if (geng.getreversepathln().get(selectnode) > 0) {
                abrpath.add(1 + random.nextInt(geng.getreversepathln().get(selectnode) - 1));
            }
            else {
                abrpath.add(0);
            }
        }
        return abrpath;
    }


    private static void getSchema(){
        StringBuilder sb = new StringBuilder();
        sb.append("The graph database consists of the following elements:\n");
        sb.append("In the tag section, label represents the name of the tag, count indicates the number of nodes with that tag, and attributes lists the properties of the tag. Among them, the name attribute follows the format tag_i_node_j, where i corresponds to the numeric value in the tag’s label, and j represents the node’s position within that tag, starting from 0.\n");

        sb.append("tags = [\n");
        for (int i = 0; i < geng.getNodenum().size(); i++) {
            sb.append("  {\n");
            sb.append(String.format("    \"label\": \"tag_%d\",\n", i));
            sb.append(String.format("    \"count\": %d,\n", geng.getNodenum().get(i)));

            Map<String, Integer> attrs = geng.getTagattribute().get(i);
            sb.append("    \"attributes\": {\n");
            if (attrs != null && !attrs.isEmpty()) {
                int j = 0;
                sb.append("      \"name\": \"String\"");
                for (Map.Entry<String, Integer> entry : attrs.entrySet()) {
                    sb.append(String.format("      \"%s\": \"%s\"", entry.getKey(), typeToString(entry.getValue())));
                    if (++j < attrs.size()) sb.append(",");
                    sb.append("\n");
                }
            }
            sb.append("    }\n");
            sb.append("  }");
            if (i < geng.getNodenum().size() - 1) sb.append(",");
            sb.append("\n");
        }
        sb.append("]\n\n");

        // 输出 relationships 数组
        sb.append("relationships = [\n");
        int relCount = 0;
        for (Map.Entry<ArrayList<Integer>, Map<String, Integer>> entry : geng.getRelattribute().entrySet()) {
            ArrayList<Integer> pair = entry.getKey();
            if (pair.size() != 2) continue;

            Map<String, Integer> attrs = entry.getValue();
            sb.append("  {\n");
            sb.append(String.format("    \"from\": \"tag_%d\",\n", pair.get(0)));
            sb.append(String.format("    \"to\": \"tag_%d\",\n", pair.get(1)));

            sb.append("    \"attributes\": {\n");
            if (attrs != null && !attrs.isEmpty()) {
                int j = 0;
                for (Map.Entry<String, Integer> attr : attrs.entrySet()) {
                    sb.append(String.format("      \"%s\": \"%s\"", attr.getKey(), typeToString(attr.getValue())));
                    if (++j < attrs.size()) sb.append(",");
                    sb.append("\n");
                }
            }
            sb.append("    }\n");
            sb.append("  }");
            if (++relCount < geng.getRelattribute().size()) sb.append(",");
            sb.append("\n");
        }
        sb.append("]\n");

        SchemaDescription = sb.toString();
    }

    private static String typeToString(int typeCode) {
        switch (typeCode) {
            case 0: return "Integer";
            case 1: return "Float";
            case 2: return "String";
            case 3: return "Boolean";
            case 4: return "Point";
            case 5: return "LocalDate";
            default: return "Unknown";
        }
    }

    private static String getTaskDescription(key keygen){
        String TaskDescription = "At least the following keywords should be used:";

        List<String> keylist = keygen.getNextKeyCombo();

        return TaskDescription + " " + String.join(", ", keylist);
    }

    private static String getpath(){
        ArrayList<Integer> pd = generatepath();

        Integer type = pd.get(0);
        String PathDescription = "";
        switch (type){
            case 0:
                PathDescription = "Please use the following tag as the query path:";
                return PathDescription + " tag_" + pd.get(1);
            case 1:
                PathDescription = "Please use the following node sequence as the query path:";
                for (int i = 1; i < pd.size(); i++) {
                    PathDescription += " tag_" + pd.get(i);
                    if (i < pd.size() - 1) {
                        PathDescription += "->";
                    }
                }
                return PathDescription;
            case 2:
                Random random = new Random();
                if ((random.nextBoolean() && pd.get(2) != 0) || pd.get(3) == 0 ) {
                    PathDescription = "Write a Cypher query that uses a variable-length path to match a path starting from a node with tag_" + pd.get(1) + " and traversing exactly " + pd.get(2) + " hops.";
                } else {
                    PathDescription = "Write a Cypher query that uses a variable-length path to match a path starting from a node with tag_" + pd.get(1) + " and traversing exactly " + pd.get(3) + " hops.";
                }
                return  PathDescription;
            default:
                PathDescription = "Please use the following tag as the query path:";
                return PathDescription + " tag_" + pd.get(1);
        }
    }

}