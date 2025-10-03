/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.neo4j.plugin.analysis.vi;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.vi.VietnameseAnalyzer;
import org.neo4j.analysis.VietnameseConfig;
import org.neo4j.annotations.service.ServiceProvider;
import org.neo4j.graphdb.schema.AnalyzerProvider;

/**
 * Vietnamese Analysis Plugin for Neo4j.
 * Provides a custom analyzer provider for full-text indexes.
 *
 * @author duydo
 */
@ServiceProvider
public class VietnameseAnalysisPlugin extends AnalyzerProvider {
    
    public VietnameseAnalysisPlugin() {
        super("vietnamese");
    }

    @Override
    public Analyzer createAnalyzer() {
        VietnameseConfig vietnameseConfig = new VietnameseConfig();
        return new VietnameseAnalyzer(vietnameseConfig);
    }

    @Override
    public String getName() {
        return "vietnamese";
    }

    public String getDescription() {
        return "Vietnamese language analyzer using CocCoc tokenizer";
    }
}

