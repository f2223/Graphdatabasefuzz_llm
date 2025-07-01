# 🔍 GraphDatabaseFuzzLLM

基于大语言模型 (LLM) 的图数据库模糊测试工具，可自动生成高质量 Cypher 查询语句，覆盖更多图结构路径与逻辑分支，辅助发现图数据库在特定结构下的异常行为。

---

## 🛠️ 工具简介

本项目结合 LLM 自动生成结构化查询种子，利用 JaCoCo 插桩实现覆盖率监控，并基于图结构与语义路径策略对图数据库进行 Fuzz 测试。

---

## 📦 项目特性

- ✅ 基于 LLM 自动生成 Cypher 查询
- ✅ 多种路径模式（固定跳数、变长路径等）
- ✅ 支持 Neo4j 5.x 离线插桩（JaCoCo）
- ✅ 实时覆盖率反馈

---

## 📋 使用说明

### 1. 克隆 Neo4j 源码（以 5.26.0 为例）
```bash
eg: git clone --depth 1 --branch 5.26.0 https://github.com/neo4j/neo4j.git
```

### 2. 编译 Neo4j
```bash
cd neo4j
mvn clean compile -DskipTests
```

### 3. 克隆本项目，进入项目目录，修改build-instrumented.sh中的JACOCO_CLI_JAR（替换你的jacococli.jar路径），target_dir（替换想要离线插桩的代码部分，如果对neo4j完全插桩可使用以下路径），neo4j_dir（替换为neo4j项目所在路径）:
```bash
JACOCO_CLI_JAR=/home/jacoco/jar/lib/jacococli.jar
target_dir=/home/neo4j_ALL/neo4j/community
neo4j_dir=/home/neo4j_ALL/neo4j
```

### 4. 执行build-instrumented.sh，完成离线插桩和install：
```bash
./build-instrumented.sh
```

### 5. 进入打包好的图数据库jar包所在位置：/home/neo4j_ALL/neo4j/packaging/standalone/target（具体目录根据你的情况而定），解压打好的包：
```bash
tar -xzf neo4j-community-5.26.0-unix.tar.gz
jar包位置在/neo4j-community-5.26.0/lib/
```

### 6. 返回本项目目录，修改src/main/java/org/llmgdfuzz/llm/下的llm_call.java中的:
```bash
private static final String API_KEY = "sk-你的-API-Key";
private static final String OPENAI_API_URL = "https://api.openai.com/v1/chat/completions";
```

### 7. 编译本项目，根据你版本的neo4j在mvn仓库中的名称替换pom.xml中的以下内容：
```bash
<dependency>
    <groupId>org.neo4j</groupId>
    <artifactId>neo4j</artifactId>
    <version>5.26.0</version>
</dependency>
```

### 8. 返回本项目目录，执行：
```bash
mvn dependency:copy-dependencies
mkdir coverage-log
mkdir crashes
mkdir data
```

### 9. 启动fuzz程序，-cp 后的参数替换为你自己的对应目录：
```bash
 java -cp "/home/neo4j_ALL/neo4j/packaging/standalone/target/neo4j-community-5.26.0/lib/*:/home/gd_code/Graphdatabasefuzz_llm/target/dependency/*:/home/gd_code/Graphdatabasefuzz_llm/target/classes" org.llmgdfuzz.Main
```
