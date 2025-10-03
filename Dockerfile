FROM maven:3.9-eclipse-temurin-17 AS builder

# Install dependencies for building CocCoc tokenizer
RUN apt-get update && apt-get install -y \
    build-essential \
    cmake \
    git \
    && rm -rf /var/lib/apt/lists/*

# Build CocCoc tokenizer
WORKDIR /tmp
RUN git clone https://github.com/duydo/coccoc-tokenizer.git && \
    cd coccoc-tokenizer && \
    mkdir build && \
    cd build && \
    cmake -DBUILD_JAVA=1 .. && \
    make install

# Copy and build the Neo4j plugin
WORKDIR /plugin
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Final image
FROM neo4j:5.26.0-enterprise

# Copy the tokenizer library and dictionaries from builder
COPY --from=builder /usr/local/lib/libcoccoc_tokenizer_jni.so /usr/lib/
COPY --from=builder /usr/local/share/tokenizer/dicts /usr/local/share/tokenizer/dicts

# Copy the plugin
COPY --from=builder /plugin/target/neo4j-analysis-vietnamese-*.jar /plugins/

# Set environment variables
ENV NEO4J_ACCEPT_LICENSE_AGREEMENT=yes

# Expose Neo4j ports
EXPOSE 7474 7687

USER neo4j
