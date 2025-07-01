package org.llmgdfuzz.llm;

import java.net.URI;
import java.net.ProxySelector;
import java.net.InetSocketAddress;
import java.net.http.*;
import java.util.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

public class llm_call {

    private static final String API_KEY = "sk-你的-API-Key";
    private static final String OPENAI_API_URL = "https://api.openai.com/v1/chat/completions";

    private static final String MODEL = "gpt-4o";

    private static final ObjectMapper mapper = new ObjectMapper();

    //private static ProxySelector proxy = ProxySelector.of(new InetSocketAddress("127.0.0.1", 7890));

    // 构建 HttpClient，并设置代理

    /**
     * 根据自然语言任务描述生成多个 Cypher 查询语句
     *
     * @param taskDescription 自然语言查询需求
     * @param schema 可选图数据库 schema 提示（如标签/关系）
     * @param diversityLevel 生成几个不同风格的查询（建议 1~5）
     * @return 查询语句列表
     * @throws Exception on request failure
     */
    public static List<String> generateCypherQueries(String taskDescription, String schema, String cpath, int diversityLevel) throws Exception {
        String systemPrompt = "You are an expert in Neo4j Cypher. Given a natural language task, generate accurate and semantically diverse Cypher queries.";
        String userPrompt = "Task: " + taskDescription;
        if (schema != null && !schema.isEmpty()) {
            userPrompt += "\nGraph schema: " + schema;
        }
        if (cpath != null && !cpath.isEmpty()) {
            userPrompt += "\nMath path: " + cpath;
        }
        userPrompt += "\nPlease output " + diversityLevel + " diverse Cypher queries.只输出cypher语句，中间使用```cypher分割";

        // 构造请求体
        Map<String, Object> requestBody = Map.of(
                "model", MODEL,
                "messages", List.of(
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content", userPrompt)
                ),
                "temperature", 0.9,
                "n", 1 // 一次调用，返回多个查询在 response 内容中
        );

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(OPENAI_API_URL))
                .header("Authorization", "Bearer " + API_KEY)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(requestBody)))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("OpenAI API 调用失败: " + response.body());
        }

        JsonNode root = mapper.readTree(response.body());
        String content = root.path("choices").get(0).path("message").path("content").asText();

        // 解析多个 Cypher 查询（假设 LLM 用 1. 2. 3. 或 ```cypher 分隔）
        return extractCypherQueries(content, diversityLevel);
    }

    private static List<String> extractCypherQueries(String rawText, int expectedCount) {
        List<String> queries = new ArrayList<>();
        String[] lines = rawText.split("\\r?\\n");
        StringBuilder current = new StringBuilder();
        for (String line : lines) {
            if (line.matches("^\\s*\\d+\\..*") || line.startsWith("```cypher")) {
                if (current.length() > 0) {
                    String candidate = cleanQuery(current.toString());
                    if (!candidate.isBlank()) {
                        queries.add(candidate);
                    }
                    current.setLength(0);
                }
            } else {
                current.append(line).append("\n");
            }
        }
        if (current.length() > 0) {
            String candidate = cleanQuery(current.toString());
            if (!candidate.isBlank()) {
                queries.add(candidate);
            }
        }

        // 截取最多 expectedCount 个结果
        return queries.subList(0, Math.min(queries.size(), expectedCount));
    }

    private static String cleanQuery(String raw) {
        String cleaned = raw.trim();
        // 移除尾部 ``` 或 ```cypher
        while (cleaned.endsWith("```") || cleaned.endsWith("```cypher")) {
            int index = cleaned.lastIndexOf("```");
            cleaned = cleaned.substring(0, index).trim();
        }
        return cleaned;
    }
}

