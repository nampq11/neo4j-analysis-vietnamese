# Vietnamese Analysis Plugin for Neo4j

Vietnamese Analysis plugin integrates Vietnamese language analysis into Neo4j. It uses [C++ tokenizer for Vietnamese](https://github.com/coccoc/coccoc-tokenizer) library developed by CocCoc team for their Search Engine and Ads systems.

The plugin provides:
- **`vietnamese` analyzer** for full-text indexes
- **`vi.tokenize()` procedure** for tokenizing Vietnamese text in Cypher queries
- **`vi.tokens()` function** for extracting tokens as a list

## Features

### 1. Vietnamese Analyzer for Full-Text Indexes

Use the `vietnamese` analyzer when creating full-text indexes:

```cypher
CREATE FULLTEXT INDEX articleIndex 
FOR (n:Article) 
ON EACH [n.title, n.content]
OPTIONS {
  analyzer: 'vietnamese'
}
```

### 2. Text Analysis Procedures and Functions

#### Tokenize Procedure

Returns detailed token information:

```cypher
CALL vi.tokenize("Cộng hòa Xã hội chủ nghĩa Việt Nam") 
YIELD token, type, startPosition, endPosition
RETURN token, type, startPosition, endPosition
```

Result:
```
╒══════════════╤════════╤═══════════════╤═════════════╕
│ token        │ type   │ startPosition │ endPosition │
╞══════════════╪════════╪═══════════════╪═════════════╡
│ "cộng hòa"   │ "WORD" │ 0             │ 8           │
├──────────────┼────────┼───────────────┼─────────────┤
│ "xã hội"     │ "WORD" │ 9             │ 15          │
├──────────────┼────────┼───────────────┼─────────────┤
│ "chủ nghĩa"  │ "WORD" │ 16            │ 25          │
├──────────────┼────────┼───────────────┼─────────────┤
│ "việt nam"   │ "WORD" │ 26            │ 34          │
└──────────────┴────────┴───────────────┴─────────────┘
```

#### Tokens Function

Returns a simple list of tokens:

```cypher
RETURN vi.tokens("Công nghệ thông tin Việt Nam rất phát triển") AS tokens
```

Result:
```
╒════════════════════════════════════════════════════════════════╕
│ tokens                                                         │
╞════════════════════════════════════════════════════════════════╡
│ ["công nghệ", "thông tin", "việt nam", "phát triển", "trong"] │
└────────────────────────────────────────────────────────────────┘
```

## Configuration

Both the procedure and function accept an optional configuration map:

```cypher
CALL vi.tokenize("Cộng hòa Xã hội chủ nghĩa Việt Nam", {
  dictPath: '/usr/local/share/tokenizer/dicts',
  keepPunctuation: true,
  splitHost: false,
  splitURL: false
}) YIELD token, type
RETURN token, type
```

Configuration options:
- **`dictPath`** - Path to tokenizer dictionary on system. Defaults to `/usr/local/share/tokenizer/dicts`.
- **`keepPunctuation`** - Keep punctuation marks as tokens. Defaults to `false`.
- **`splitURL`** - If enabled, a domain `duydo.me` is split into `["duy", "do", "me"]`. If disabled, `duydo.me` is split into `["duydo", "me"]`. Defaults to `false`.
- **`splitHost`** - Similar to splitURL but for host names. Defaults to `false`.

## Use Docker

Make sure you have installed both Docker & docker-compose

### Build the image with Docker Compose

```sh
# Build and start Neo4j with the plugin
docker compose build
docker compose up
```

### Verify

```sh
# Test the tokenization function
curl -X POST http://neo4j:password@localhost:7474/db/neo4j/tx/commit \
  -H "Content-Type: application/json" \
  -d '{
    "statements": [{
      "statement": "RETURN vi.tokens(\"Cộng hòa Xã hội chủ nghĩa Việt Nam\") AS tokens"
    }]
  }'
```

## Build from Source

### Step 1: Build C++ tokenizer for Vietnamese library

```sh
git clone https://github.com/duydo/coccoc-tokenizer.git
cd coccoc-tokenizer && mkdir build && cd build
cmake -DBUILD_JAVA=1 ..
make install
# Link the coccoc shared lib to /usr/lib
sudo ln -sf /usr/local/lib/libcoccoc_tokenizer_jni.* /usr/lib/
```

By default, the `make install` installs:
- The lib commands `tokenizer`, `dict_compiler` and `vn_lang_tool` under `/usr/local/bin`
- The dynamic lib `libcoccoc_tokenizer_jni.so` under `/usr/local/lib/`. The plugin uses this lib directly.
- The dictionary files under `/usr/local/share/tokenizer/dicts`. The plugin uses this path for `dictPath` by default.

Verify:
```sh
/usr/local/bin/tokenizer "Cộng hòa Xã hội chủ nghĩa Việt Nam"
# cộng hòa	xã hội	chủ nghĩa	việt nam
```

Refer [the repo](https://github.com/duydo/coccoc-tokenizer) for more information to build the library.

### Step 2: Build the plugin

Clone the plugin's source code:

```sh
git clone https://github.com/duydo/neo4j-analysis-vietnamese.git
cd neo4j-analysis-vietnamese
```

Build the plugin:
```sh
mvn clean package -DskipTests
```

**Note:** Use `-DskipTests` flag since tests require the CocCoc tokenizer native library to be installed on your system.

### Step 3: Install the plugin on Neo4j

```sh
# Copy the plugin JAR to Neo4j plugins directory
cp target/neo4j-analysis-vietnamese-5.26.0.jar /path/to/neo4j/plugins/

# Restart Neo4j
neo4j restart
```

## Example Usage

### Creating a Full-Text Search Application

```cypher
// Create some Vietnamese articles
CREATE (a1:Article {
  title: "Công nghệ thông tin Việt Nam",
  content: "Ngành công nghệ thông tin Việt Nam đang phát triển rất mạnh mẽ trong những năm gần đây."
})
CREATE (a2:Article {
  title: "Trí tuệ nhân tạo",
  content: "Trí tuệ nhân tạo đang thay đổi cuộc sống của chúng ta mỗi ngày."
})

// Create full-text index with Vietnamese analyzer
CREATE FULLTEXT INDEX articleIndex 
FOR (n:Article) 
ON EACH [n.title, n.content]
OPTIONS {
  analyzer: 'vietnamese'
}

// Search using the full-text index
CALL db.index.fulltext.queryNodes('articleIndex', 'công nghệ')
YIELD node, score
RETURN node.title, node.content, score
ORDER BY score DESC
```

### Text Analysis in Query Processing

```cypher
// Tokenize text and create relationships
MATCH (a:Article)
UNWIND vi.tokens(a.content) AS token
MERGE (t:Token {value: token})
MERGE (a)-[:CONTAINS]->(t)
```

## Compatible Versions

| Vietnamese Analysis Plugin | Neo4j    |
|---------------------------|----------|
| 5.26.0                    | 5.26.x   |

## Troubleshooting

### Error: java.lang.UnsatisfiedLinkError: no libcoccoc_tokenizer_jni in java.library.path

This happens because the JVM cannot find the dynamic lib `libcoccoc_tokenizer_jni` in `java.library.path`. Try one of:

1. Append `/usr/local/lib` to `LD_LIBRARY_PATH`:
```sh
export LD_LIBRARY_PATH=/usr/local/lib:$LD_LIBRARY_PATH
```

2. Make a symbolic link to `/usr/lib`:
```sh
ln -sf /usr/local/lib/libcoccoc_tokenizer_jni.so /usr/lib/libcoccoc_tokenizer_jni.so
```

### Error: Cannot initialize Tokenizer: /usr/local/share/tokenizer/dicts

Ensure the path `/usr/local/share/tokenizer/dicts` exists and includes these files:
- alphabetic
- i_and_y.txt
- nontone_pair_freq_map.dump
- syllable_trie.dump
- d_and_gi.txt
- multiterm_trie.dump
- numeric

If not, rebuild the C++ tokenizer (Step 1) again.

## References

- [Neo4j Full-Text Indexes Documentation](https://neo4j.com/docs/cypher-manual/current/indexes-for-full-text-search/)
- [Neo4j Extending Guide](https://neo4j.com/docs/java-reference/current/extending-neo4j/)
- [CocCoc Tokenizer](https://github.com/coccoc/coccoc-tokenizer)

## Thanks to

- [JetBrains](https://www.jetbrains.com) has provided a free license for [IntelliJ IDEA](https://www.jetbrains.com/idea).
- [CocCoc team](https://coccoc.com) has provided their C++ Vietnamese tokenizer library as open source.

## License
    
    This software is licensed under the Apache 2 license, quoted below.

    Copyright by Duy Do

    Licensed under the Apache License, Version 2.0 (the "License"); you may not
    use this file except in compliance with the License. You may obtain a copy of
    the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
    WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
    License for the specific language governing permissions and limitations under
    the License.
