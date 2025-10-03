# Migration Guide: From Elasticsearch to Neo4j

This document explains how to migrate from the Elasticsearch Vietnamese Analysis Plugin to the Neo4j Vietnamese Analysis Plugin.

## Overview

The plugin has been converted from an Elasticsearch plugin to a Neo4j plugin. While the core Vietnamese tokenization functionality remains the same (using CocCoc tokenizer), the integration points and usage patterns have changed significantly.

## Key Differences

### 1. Platform Change

| Aspect | Elasticsearch | Neo4j |
|--------|--------------|-------|
| Platform | Elasticsearch 7.x/8.x | Neo4j 5.x |
| Query Language | Elasticsearch Query DSL | Cypher |
| Index Type | Inverted Index | Full-text Index |
| Integration | Analysis Plugin | Analyzer Provider + Procedures |

### 2. Feature Mapping

#### Analyzer

**Elasticsearch:**
```json
PUT /my-index
{
  "settings": {
    "analysis": {
      "analyzer": {
        "my_vi_analyzer": {
          "type": "vi_analyzer",
          "keep_punctuation": true
        }
      }
    }
  }
}
```

**Neo4j:**
```cypher
CREATE FULLTEXT INDEX myIndex 
FOR (n:Document) 
ON EACH [n.content]
OPTIONS {
  analyzer: 'vietnamese'
}
```

#### Text Analysis

**Elasticsearch:**
```json
GET /_analyze
{
  "analyzer": "vi_analyzer",
  "text": "Cộng hòa Xã hội chủ nghĩa Việt Nam"
}
```

**Neo4j:**
```cypher
CALL vi.tokenize("Cộng hòa Xã hội chủ nghĩa Việt Nam") 
YIELD token, type, startPosition, endPosition
RETURN token, type
```

#### Searching

**Elasticsearch:**
```json
GET /my-index/_search
{
  "query": {
    "match": {
      "content": "công nghệ"
    }
  }
}
```

**Neo4j:**
```cypher
CALL db.index.fulltext.queryNodes('myIndex', 'công nghệ')
YIELD node, score
RETURN node, score
ORDER BY score DESC
```

## Migration Steps

### 1. Data Migration

Since Elasticsearch and Neo4j are fundamentally different databases, you'll need to:

1. **Extract data from Elasticsearch:**
   ```bash
   # Export Elasticsearch data
   elasticdump \
     --input=http://localhost:9200/my-index \
     --output=data.json \
     --type=data
   ```

2. **Transform data for Neo4j:**
   - Convert JSON documents to Cypher CREATE statements
   - Model relationships (if applicable)
   - Map document fields to node properties

3. **Import into Neo4j:**
   ```cypher
   // Using LOAD CSV or similar
   LOAD CSV WITH HEADERS FROM 'file:///data.csv' AS row
   CREATE (d:Document {
     id: row.id,
     content: row.content,
     title: row.title
   })
   ```

4. **Create full-text indexes:**
   ```cypher
   CREATE FULLTEXT INDEX documentIndex 
   FOR (n:Document) 
   ON EACH [n.title, n.content]
   OPTIONS {
     analyzer: 'vietnamese'
   }
   ```

### 2. Application Code Changes

#### Search Queries

**Before (Elasticsearch):**
```python
from elasticsearch import Elasticsearch

es = Elasticsearch(['localhost:9200'])
response = es.search(
    index='my-index',
    body={
        'query': {
            'match': {
                'content': 'công nghệ'
            }
        }
    }
)
results = response['hits']['hits']
```

**After (Neo4j):**
```python
from neo4j import GraphDatabase

driver = GraphDatabase.driver('bolt://localhost:7687', 
                              auth=('neo4j', 'password'))

with driver.session() as session:
    result = session.run("""
        CALL db.index.fulltext.queryNodes('documentIndex', $query)
        YIELD node, score
        RETURN node, score
        ORDER BY score DESC
        LIMIT 10
    """, query='công nghệ')
    
    for record in result:
        print(record['node'], record['score'])
```

#### Text Tokenization

**Before (Elasticsearch):**
```python
response = es.indices.analyze(
    body={
        'analyzer': 'vi_analyzer',
        'text': 'Cộng hòa Xã hội chủ nghĩa Việt Nam'
    }
)
tokens = [token['token'] for token in response['tokens']]
```

**After (Neo4j):**
```python
result = session.run("""
    CALL vi.tokenize($text) 
    YIELD token
    RETURN collect(token) AS tokens
""", text='Cộng hòa Xã hội chủ nghĩa Việt Nam')

tokens = result.single()['tokens']
```

### 3. Configuration Changes

**Elasticsearch plugin descriptor** (`plugin-descriptor.properties`):
```properties
description=Vietnamese Analysis Plugin
version=7.17.10
name=analysis-vietnamese
classname=org.elasticsearch.plugin.analysis.vi.AnalysisVietnamesePlugin
java.version=1.8
elasticsearch.version=7.17.10
```

**Neo4j service provider** (`META-INF/services/org.neo4j.graphdb.schema.AnalyzerProvider`):
```
org.neo4j.plugin.analysis.vi.VietnameseAnalysisPlugin
```

## New Features in Neo4j Version

### 1. User-Defined Procedures

The Neo4j version adds Cypher procedures for text analysis:

```cypher
// Detailed tokenization
CALL vi.tokenize("Text to analyze") 
YIELD token, type, startPosition, endPosition

// Simple token extraction
RETURN vi.tokens("Text to analyze") AS tokens
```

### 2. Graph Integration

Unlike Elasticsearch, Neo4j allows you to directly work with relationships:

```cypher
// Create token relationships
MATCH (d:Document)
UNWIND vi.tokens(d.content) AS token
MERGE (t:Token {value: token})
MERGE (d)-[:CONTAINS]->(t)

// Find documents sharing tokens
MATCH (d1:Document)-[:CONTAINS]->(t:Token)<-[:CONTAINS]-(d2:Document)
WHERE d1 <> d2
RETURN d1, d2, collect(t.value) AS sharedTokens
```

### 3. Combined Graph and Text Search

```cypher
// Full-text search with graph traversal
CALL db.index.fulltext.queryNodes('documentIndex', 'công nghệ')
YIELD node AS doc
MATCH (doc)-[:AUTHORED_BY]->(author:Author)
RETURN doc.title, author.name, author.email
```

## Performance Considerations

### Elasticsearch
- Optimized for full-text search at scale
- Horizontal scaling through sharding
- Near real-time indexing

### Neo4j
- Optimized for graph traversals and relationships
- Full-text search is supplementary to graph queries
- Better for combining text search with relationship queries

## Recommendations

1. **Use Neo4j if:**
   - Your data has significant relationships
   - You need to combine text search with graph traversals
   - Your application benefits from graph analytics

2. **Consider keeping Elasticsearch if:**
   - Your primary use case is pure text search
   - You need to scale text search to billions of documents
   - You don't have significant relationship data

3. **Use both (Dual-write pattern):**
   - Write data to both Neo4j (for graphs) and Elasticsearch (for text search)
   - Use each database for its strengths
   - Keep data synchronized

## Support

For migration questions:
- Neo4j Community Forum: https://community.neo4j.com/
- GitHub Issues: https://github.com/duydo/neo4j-analysis-vietnamese/issues

