# Changelog

All notable changes to this project will be documented in this file.

## [5.26.0] - 2025-10-03

### Major Changes
- **BREAKING**: Converted from Elasticsearch plugin to Neo4j plugin
- **BREAKING**: Removed Elasticsearch-specific APIs and dependencies
- **BREAKING**: Changed package structure from `org.elasticsearch` to `org.neo4j`

### Added
- Neo4j 5.26.0 support
- `vietnamese` analyzer for Neo4j full-text indexes
- `vi.tokenize()` procedure for detailed text analysis in Cypher
- `vi.tokens()` function for simple token extraction
- Full-text index integration with Neo4j
- Service provider interface for analyzer registration
- Docker support with automated build
- Comprehensive documentation:
  - Migration guide from Elasticsearch to Neo4j
  - Build instructions
  - Usage examples with Cypher queries

### Changed
- Updated to Java 17 (from Java 8)
- Replaced Elasticsearch dependencies with Neo4j dependencies
- Replaced Guava's `CharStreams` with custom implementation
- Updated Lucene version to 9.11.1
- Simplified configuration (removed Elasticsearch Settings dependency)
- Maven Shade plugin for JAR packaging (replaced Assembly plugin)

### Removed
- Elasticsearch dependencies and APIs
- Elasticsearch plugin descriptors (`plugin-descriptor.properties`, `es-plugin.properties`)
- Elasticsearch-specific factories:
  - `VietnameseTokenizerFactory`
  - `VietnameseAnalyzerProvider`
  - `VietnameseStopTokenFilterFactory`
- Elasticsearch test infrastructure
- Assembly plugin configuration

### Fixed
- Security manager compatibility by removing Guava dependency
- Library path issues documented in troubleshooting

## [7.17.10] - Previous Elasticsearch Version

### Features
- Vietnamese analyzer for Elasticsearch
- Vietnamese tokenizer using CocCoc library
- Stop words filtering
- Configurable tokenization options
- Elasticsearch 7.17.x support

---

## Migration Path

Users of the Elasticsearch version should refer to [MIGRATION.md](MIGRATION.md) for detailed migration instructions to the Neo4j version.

## Version Numbering

Starting with version 5.26.0, version numbers match the target Neo4j version for compatibility clarity.

