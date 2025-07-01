#!/bin/bash
set -e


JACOCO_CLI_JAR=/home/jacoco/jar/lib/jacococli.jar
target_dir=/home/neo4j_ALL/neo4j/community
neo4j_dir=/home/neo4j_ALL/neo4j

if [[ ! -f "$JACOCO_CLI_JAR" ]]; then
  echo "no dir:$JACOCO_CLI_JAR"
  exit 1
fi


echo "instrument..."

find $target_dir -type d -path "*/target/classes" | while read classes_dir; do
  echo "instrument:$classes_dir"

  instr_dir="$(dirname "$classes_dir")/instrumented-classes"

  
  java -jar "$JACOCO_CLI_JAR" instrument "$classes_dir" --dest "$instr_dir"

  
  mv "$classes_dir" "${classes_dir}.bak"
  
  mv "$instr_dir" "$classes_dir"
done

echo "install..."
cd $neo4j_dir
mvn install -Dmaven.test.skip=true -Dmaven.compiler.skip=true

echo "finish"