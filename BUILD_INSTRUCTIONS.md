# Build Instructions

## Prerequisites

1. **Java 17 or higher**
   ```sh
   java -version
   ```

2. **Maven 3.6 or higher**
   ```sh
   mvn -version
   ```

3. **CocCoc Vietnamese Tokenizer Library**
   
   Follow instructions at: https://github.com/coccoc/coccoc-tokenizer
   
   Quick build:
   ```sh
   git clone https://github.com/duydo/coccoc-tokenizer.git
   cd coccoc-tokenizer && mkdir build && cd build
   cmake -DBUILD_JAVA=1 ..
   make install
   sudo ln -sf /usr/local/lib/libcoccoc_tokenizer_jni.* /usr/lib/
   ```

## Building the Plugin

### 1. Clone the Repository

```sh
git clone https://github.com/duydo/neo4j-analysis-vietnamese.git
cd neo4j-analysis-vietnamese
```

### 2. Build with Maven

```sh
# Build with skipping tests (tests require CocCoc tokenizer to be installed)
mvn clean package -DskipTests
```

This will create:
- `target/neo4j-analysis-vietnamese-5.26.0.jar` - The plugin JAR file

**Note:** Tests are skipped by default because they require the CocCoc tokenizer native library to be installed. To run tests, first install the CocCoc tokenizer (see prerequisites).

### 3. Install to Neo4j

```sh
# Copy to Neo4j plugins directory
cp target/neo4j-analysis-vietnamese-5.26.0.jar /path/to/neo4j/plugins/

# Restart Neo4j
neo4j restart
```

## Building with Docker

If you prefer to use Docker:

```sh
# Build the Docker image
docker compose build

# Start Neo4j with the plugin
docker compose up
```

The Docker build process will:
1. Build the CocCoc tokenizer from source
2. Compile the Neo4j plugin
3. Install everything into a Neo4j container

## Running Tests

```sh
mvn test
```

Note: Tests require the CocCoc tokenizer library to be installed on your system.

## Troubleshooting Build Issues

### Issue: Tests fail with UnsatisfiedLinkError

**Solution**: Ensure `libcoccoc_tokenizer_jni.so` is in your library path:

```sh
export LD_LIBRARY_PATH=/usr/local/lib:$LD_LIBRARY_PATH
```

### Issue: Dictionary not found during tests

**Solution**: Ensure dictionary files are installed at `/usr/local/share/tokenizer/dicts`:

```sh
ls -la /usr/local/share/tokenizer/dicts
```

If missing, rebuild and reinstall the CocCoc tokenizer.

### Issue: Maven dependency resolution fails

**Solution**: Clear Maven cache and retry:

```sh
rm -rf ~/.m2/repository/org/neo4j
mvn clean install -U
```

## Development

### Project Structure

```
.
├── pom.xml                          # Maven build configuration
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   ├── com/coccoc/          # CocCoc tokenizer JNI wrapper
│   │   │   ├── org/neo4j/
│   │   │   │   ├── analysis/        # Configuration classes
│   │   │   │   └── plugin/analysis/vi/  # Plugin implementation
│   │   │   └── org/apache/lucene/analysis/vi/  # Lucene analyzer
│   │   └── resources/
│   │       ├── META-INF/services/   # Neo4j service providers
│   │       └── org/apache/lucene/analysis/vi/  # Stopwords
│   └── test/
│       └── java/                    # Unit tests
├── Dockerfile                       # Docker build configuration
└── docker-compose.yaml              # Docker Compose setup
```

### Making Changes

1. Make your changes to the source code
2. Run tests: `mvn test`
3. Build: `mvn clean package`
4. Install and test in Neo4j

### Adding New Features

- **New procedures/functions**: Add to `VietnameseTextAnalysisProcedures.java`
- **Analyzer modifications**: Update `VietnameseAnalyzer.java` or `VietnameseTokenizer.java`
- **Configuration options**: Extend `VietnameseConfig.java`

## Publishing

### Creating a Release

```sh
# Update version in pom.xml
mvn versions:set -DnewVersion=5.27.0

# Build and create JAR
mvn clean package

# Create GitHub release and attach the JAR file
```

## Support

For build issues or questions:
- GitHub Issues: https://github.com/duydo/neo4j-analysis-vietnamese/issues
- Neo4j Community Forum: https://community.neo4j.com/

