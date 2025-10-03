# Quick Start Guide

Get started with the Vietnamese Analysis Plugin for Neo4j in 5 minutes!

## Option 1: Docker (Recommended)

The easiest way to try the plugin:

```bash
# Clone the repository
git clone https://github.com/duydo/neo4j-analysis-vietnamese.git
cd neo4j-analysis-vietnamese

# Build and start Neo4j
docker compose build
docker compose up

# Wait for Neo4j to start, then open browser
# http://localhost:7474
# Login: neo4j / password
```

## Option 2: Manual Installation

### Prerequisites
```bash
# Install CocCoc tokenizer
git clone https://github.com/duydo/coccoc-tokenizer.git
cd coccoc-tokenizer && mkdir build && cd build
cmake -DBUILD_JAVA=1 ..
make install
sudo ln -sf /usr/local/lib/libcoccoc_tokenizer_jni.* /usr/lib/
```

### Build and Install
```bash
# Build the plugin
git clone https://github.com/duydo/neo4j-analysis-vietnamese.git
cd neo4j-analysis-vietnamese
mvn clean package -DskipTests

# Install to Neo4j
cp target/neo4j-analysis-vietnamese-5.26.0.jar $NEO4J_HOME/plugins/

# Restart Neo4j
neo4j restart
```

**Note:** Tests are skipped because they require the CocCoc tokenizer native library.

## Test the Plugin

Open Neo4j Browser (http://localhost:7474) and run:

### 1. Test Tokenization
```cypher
CALL vi.tokenize("Cộng hòa Xã hội chủ nghĩa Việt Nam") 
YIELD token, type, startPosition, endPosition
RETURN token, type, startPosition, endPosition
```

Expected output:
```
╒══════════════╤════════╤═══════════════╤═════════════╕
│ token        │ type   │ startPosition │ endPosition │
╞══════════════╪════════╪═══════════════╪═════════════╡
│ "cộng hòa"   │ "WORD" │ 0             │ 8           │
│ "xã hội"     │ "WORD" │ 9             │ 15          │
│ "chủ nghĩa"  │ "WORD" │ 16            │ 25          │
│ "việt nam"   │ "WORD" │ 26            │ 34          │
└──────────────┴────────┴───────────────┴─────────────┘
```

### 2. Create Sample Data
```cypher
CREATE (a1:Article {
  title: "Công nghệ thông tin Việt Nam",
  content: "Ngành công nghệ thông tin Việt Nam đang phát triển rất mạnh mẽ."
})
CREATE (a2:Article {
  title: "Trí tuệ nhân tạo",
  content: "Trí tuệ nhân tạo đang thay đổi cuộc sống của chúng ta."
})
CREATE (a3:Article {
  title: "An ninh mạng",
  content: "An ninh mạng là một vấn đề quan trọng trong công nghệ thông tin."
})
```

### 3. Create Full-Text Index
```cypher
CREATE FULLTEXT INDEX articleIndex 
FOR (n:Article) 
ON EACH [n.title, n.content]
OPTIONS {
  analyzer: 'vietnamese'
}
```

### 4. Search with Full-Text Index
```cypher
CALL db.index.fulltext.queryNodes('articleIndex', 'công nghệ thông tin')
YIELD node, score
RETURN node.title, node.content, score
ORDER BY score DESC
```

### 5. Extract Tokens
```cypher
MATCH (a:Article)
RETURN a.title, vi.tokens(a.content) AS tokens
LIMIT 5
```

## Common Use Cases

### Use Case 1: Document Search
```cypher
// Find all articles mentioning "trí tuệ nhân tạo"
CALL db.index.fulltext.queryNodes('articleIndex', 'trí tuệ nhân tạo')
YIELD node, score
RETURN node.title, score
ORDER BY score DESC
```

### Use Case 2: Token Analysis
```cypher
// Analyze which tokens appear most frequently
MATCH (a:Article)
UNWIND vi.tokens(a.content) AS token
RETURN token, count(*) AS frequency
ORDER BY frequency DESC
LIMIT 10
```

### Use Case 3: Build Token Graph
```cypher
// Create relationships based on shared tokens
MATCH (a:Article)
UNWIND vi.tokens(a.content) AS token
MERGE (t:Token {value: token})
MERGE (a)-[:CONTAINS]->(t)
```

```cypher
// Find related articles through shared tokens
MATCH (a1:Article)-[:CONTAINS]->(t:Token)<-[:CONTAINS]-(a2:Article)
WHERE a1 <> a2
RETURN a1.title, a2.title, collect(t.value) AS sharedTokens
LIMIT 10
```

### Use Case 4: Custom Configuration
```cypher
// Tokenize with custom settings
CALL vi.tokenize("Email: test@example.com", {
  keepPunctuation: true,
  splitURL: true
}) 
YIELD token, type
RETURN token, type
```

## Next Steps

- Read the [full documentation](README.md)
- Check [build instructions](BUILD_INSTRUCTIONS.md)
- Learn about [migration from Elasticsearch](MIGRATION.md)
- Explore [Neo4j Cypher documentation](https://neo4j.com/docs/cypher-manual/current/)

## Troubleshooting

### Error: Procedure not found
**Solution**: Ensure the plugin JAR is in the `plugins` directory and Neo4j has been restarted.

### Error: UnsatisfiedLinkError
**Solution**: Install the CocCoc tokenizer library:
```bash
sudo ln -sf /usr/local/lib/libcoccoc_tokenizer_jni.so /usr/lib/
```

### Docker: Container won't start
**Solution**: Check logs:
```bash
docker compose logs neo4j
```

## Resources

- [Neo4j Documentation](https://neo4j.com/docs/)
- [Cypher Query Language](https://neo4j.com/docs/cypher-manual/current/)
- [CocCoc Tokenizer](https://github.com/coccoc/coccoc-tokenizer)
- [GitHub Repository](https://github.com/duydo/neo4j-analysis-vietnamese)

## Support

- GitHub Issues: https://github.com/duydo/neo4j-analysis-vietnamese/issues
- Neo4j Community: https://community.neo4j.com/

