package org.llmgdfuzz.Cypher;

import org.llmgdfuzz.initgd.Randomgraph;

import org.neo4j.graphdb.*;
import org.neo4j.dbms.api.*;
import org.neo4j.values.storable.CoordinateReferenceSystem;
import org.neo4j.values.storable.Value;
import org.neo4j.values.storable.Values;

import java.util.*;
import java.time.LocalDate;

public class neo4j_cypher implements Cypher{

    public void database_creat(Randomgraph c_graph, GraphDatabaseService db){
        Random rand = new Random();

        ArrayList<Integer> nodenum = c_graph.getNodenum();
        ArrayList<Map<String, Integer>> tagattribute = c_graph.getTagattribute();
        Map<ArrayList<Integer>, Map<String, Integer>> relattribute = c_graph.getRelattribute();

        // 存储每个标签生成的节点引用，方便后续建边
        List<List<Node>> allNodes = new ArrayList<>();

        try (Transaction tx = db.beginTx()) {

            // 1. 创建节点
            for (int i = 0; i < nodenum.size(); i++) {
                List<Node> nodeList = new ArrayList<>();
                String tag = "tag_" + i;
                int count = nodenum.get(i);
                Map<String, Integer> attrMap = tagattribute.get(i);

                for (int j = 0; j < count; j++) {
                    Node node = tx.createNode(Label.label(tag));
                    String name = tag + "_node_" + j;
                    node.setProperty("name", name);

                    // 为每个节点添加属性
                    for (Map.Entry<String, Integer> entry : attrMap.entrySet()) {
                        String key = entry.getKey();
                        int type = entry.getValue();

                        // 数据类型简单模拟
                        switch (type) {
                            case 0: node.setProperty(key, rand.nextInt(10000)); break; // 整数
                            case 1: node.setProperty(key, rand.nextDouble()); break; // 浮点数
                            case 2: node.setProperty(key, generateRandomString(rand.nextInt(17)+3)); break; // 字符串
                            case 3: node.setProperty(key, rand.nextBoolean()); break;
                            case 4:
                                double lat = -90 + rand.nextDouble() * 180;
                                double lon = -180 + rand.nextDouble() * 360;

                                // 使用 Neo4j 的 storable.Value 类型来构造 Point（注意顺序是经度、纬度）
                                Value point = Values.pointValue(CoordinateReferenceSystem.WGS_84, lon, lat);
                                node.setProperty(key, point);
                                break;
                            case 5:
                                // 日期，使用 LocalDate 类型
                                LocalDate date = LocalDate.of(
                                        2000 + rand.nextInt(30),  // year: 2000~2029
                                        1 + rand.nextInt(12),     // month: 1~12
                                        1 + rand.nextInt(28)      // day: 1~28 (避免非法日期)
                                );
                                node.setProperty(key, date);
                                break;
                            default: node.setProperty(key, rand.nextInt(10000)); break;
                        }
                    }
                    nodeList.add(node);
                }
                allNodes.add(nodeList);
            }

            // 2. 创建关系
            int relId = 0;
            for (Map.Entry<ArrayList<Integer>, Map<String, Integer>> entry : relattribute.entrySet()) {
                ArrayList<Integer> link = entry.getKey();
                int fromTag = link.get(0);
                int toTag = link.get(1);

                // 随机挑选一对节点建立关系
                List<Node> fromList = allNodes.get(fromTag);
                List<Node> toList = allNodes.get(toTag);
                for (Node fromNode : fromList ){

                    //Node fromNode = fromList.get(rand.nextInt(fromList.size()));
                    Node toNode = toList.get(rand.nextInt(toList.size()));

                    Relationship rel = fromNode.createRelationshipTo(toNode, RelationshipType.withName("rel_" + relId));

                    // 添加关系属性
                    Map<String, Integer> relAttr = entry.getValue();
                    for (Map.Entry<String, Integer> attr : relAttr.entrySet()) {
                        String key = attr.getKey();
                        int type = attr.getValue();

                        switch (type) {
                            case 0:
                                rel.setProperty(key, rand.nextInt(100));
                                break;
                            case 1:
                                rel.setProperty(key, rand.nextDouble());
                                break;
                            case 2:
                                rel.setProperty(key, generateRandomString(rand.nextInt(17) + 3));
                                break;
                            case 3:
                                rel.setProperty(key, rand.nextBoolean());
                                break;
                            case 4:
                                double lat = -90 + rand.nextDouble() * 180;
                                double lon = -180 + rand.nextDouble() * 360;

                                // 使用 Neo4j 的 storable.Value 类型来构造 Point（注意顺序是经度、纬度）
                                Value point = Values.pointValue(CoordinateReferenceSystem.WGS_84, lon, lat);
                                rel.setProperty(key, point);
                                break;
                            case 5:
                                // 日期，使用 LocalDate 类型
                                LocalDate date = LocalDate.of(
                                        2000 + rand.nextInt(30),  // year: 2000~2029
                                        1 + rand.nextInt(12),     // month: 1~12
                                        1 + rand.nextInt(28)      // day: 1~28 (避免非法日期)
                                );
                                rel.setProperty(key, date);
                                break;
                            default:
                                rel.setProperty(key, rand.nextInt(10000));
                                break;
                        }
                    }
                }
                relId++;
            }

            tx.commit();
            System.out.println("[INFO] Graph created successfully.");
        } catch (Exception e) {
            System.err.println("[ERROR] Graph creation failed:");
            e.printStackTrace();
        }
    }

    private static String generateRandomString(int length) {
        String characters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_";
        StringBuilder sb = new StringBuilder(length);
        Random random = new Random();
        sb.append(characters.charAt(random.nextInt(52)));
        for (int i = 0; i < length-1; i++) {
            sb.append(characters.charAt(random.nextInt(characters.length())));
        }
        return sb.toString();
    }

    public void database_clean(GraphDatabaseService db){
        try (Transaction tx = db.beginTx()) {
            // 执行清库的 Cypher 查询
            Result result = tx.execute("MATCH (n) DETACH DELETE n");
            tx.commit();
            System.out.println("[INFO] Database cleaned successfully.");
        } catch (Exception e) {
            System.err.println("[ERROR] Failed to clean database.");
            e.printStackTrace();
        }
    }

}
