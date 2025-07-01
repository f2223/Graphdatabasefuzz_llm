package org.llmgdfuzz.key;

import java.util.*;

public class Neo4jKey implements key{
    private final List<String> keywords = Arrays.asList(
            "AND", "ANY", "ASC", "ASCENDING", "AT", "ALL", "ALL_SHORTEST_PATHS",
            "ALTER", "ALIAS", "ALIASES", "ACTIVE", "BY", "CALL", "CASCADE", "CASE",
            "COLLECT", "COMPOSITE", "CONCURRENT", "CONSTRAINT", "CONSTRAINTS", "CONTAINS",
            "COPY", "COUNT", "CREATE", "CSV", "CURRENT", "DATABASES", "DATE", "DATETIME",
            "DEFAULT", "DELETE", "DESC", "DESCENDING", "DETACH", "DISTINCT", "DURATION",
            "ELSE", "END", "ENDS", "EXISTENCE", "EXISTS", "FIELDTERMINATOR", "FLOAT",
            "FOR", "FOREACH", "FROM", "FULLTEXT", "FUNCTIONS", "GRAPH", "GRAPHS", "HEADERS",
            "ID", "IMMUTABLE", "IN", "INDEX", "INDEXES", "INF", "INTEGER", "IS", "LOAD",
            "LOCAL", "LOOKUP", "MATCH", "MERGE", "NODE", "NODES", "NONE", "NOT", "null",
            "OFFSET", "ON", "OPTIONAL", "OPTIONS", "OR", "ORDER", "POINT", "POPULATED",
            "PRIVILEGES", "PROCEDURES", "PROPERTIES", "RANGE", "REDUCE", "RELATIONSHIPS",
            "REMOVE", "REPLACE", "REPORT", "REQUIRE", "RETURN", "ROLES", "ROW", "ROWS",
            "SECONDS", "SET", "SETTINGS", "SHORTEST", "SHORTEST_PATH", "SHOW", "SINGLE",
            "SKIPROWS", "STARTS", "STATUS", "STRING", "TEXT", "THEN", "TIME", "TIMESTAMP",
            "TIMEZONE", "TRANSACTIONS", "TRIM", "TYPE", "UNION", "UNIQUE", "UNWIND",
            "USERS", "USING", "VECTOR", "WHEN", "WHERE", "WITH", "WITHOUT", "XOR", "YIELD",
            "ZONE"
    );

    private int currentComboSize = 2;
    private int currentIndex = 0;

    public List<String> getNextKeyCombo() {
        int totalCombos = combinationCount(keywords.size(), currentComboSize);
        if (currentIndex >= totalCombos) {
            currentComboSize++;
            currentIndex = 0;
            if (currentComboSize > keywords.size()) {
                currentComboSize = 2;
            }
            totalCombos = combinationCount(keywords.size(), currentComboSize);
        }

        List<String> combo = getCombinationByIndex(keywords, currentComboSize, currentIndex);
        currentIndex++;
        return combo;
    }

    // 返回第 index 个大小为 k 的组合
    private List<String> getCombinationByIndex(List<String> input, int k, int index) {
        List<String> result = new ArrayList<>();
        int n = input.size();
        int a = 0;

        for (int i = 0; i < k; i++) {
            for (int j = a; j < n; j++) {
                int count = combinationCount(n - j - 1, k - i - 1);
                if (index < count) {
                    result.add(input.get(j));
                    a = j + 1;
                    break;
                } else {
                    index -= count;
                }
            }
        }

        return result;
    }

    // 计算 C(n, k)
    private int combinationCount(int n, int k) {
        if (k > n || k < 0) return 0;
        if (k == 0 || k == n) return 1;
        long res = 1;
        for (int i = 1; i <= k; i++) {
            res = res * (n - i + 1) / i;
        }
        return (int) res;
    }

    public static void main(String[] args) {
        Neo4jKey nk = new Neo4jKey();
        List<String> combo;
        while ((combo = nk.getNextKeyCombo()) != null) {
            System.out.println(combo);
        }
    }
}
