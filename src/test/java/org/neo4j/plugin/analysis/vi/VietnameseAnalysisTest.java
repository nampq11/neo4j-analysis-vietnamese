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
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.vi.VietnameseAnalyzer;
import org.junit.Test;
import org.neo4j.analysis.VietnameseConfig;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Unit tests for Vietnamese analyzer
 *
 * @author duydo
 */
public class VietnameseAnalysisTest {

    @Test
    public void testVietnameseAnalyzer() throws IOException {
        VietnameseConfig config = new VietnameseConfig();
        Analyzer analyzer = new VietnameseAnalyzer(config);

        String text = "Cộng hòa Xã hội chủ nghĩa Việt Nam";
        List<String> tokens = tokenize(analyzer, text);

        assertNotNull(tokens);
        assertEquals(4, tokens.size());
        assertTrue(tokens.contains("cộng hòa"));
        assertTrue(tokens.contains("xã hội"));
        assertTrue(tokens.contains("chủ nghĩa"));
        assertTrue(tokens.contains("việt nam"));

        analyzer.close();
    }

    @Test
    public void testVietnameseAnalyzerWithPunctuation() throws IOException {
        VietnameseConfig config = new VietnameseConfig(
                VietnameseConfig.DEFAULT_DICT_PATH, 
                true, 
                false, 
                false
        );
        Analyzer analyzer = new VietnameseAnalyzer(config);

        String text = "Công nghệ thông tin Việt Nam rất phát triển.";
        List<String> tokens = tokenize(analyzer, text);

        assertNotNull(tokens);
        assertTrue(tokens.size() > 0);
        assertTrue(tokens.contains("công nghệ"));
        assertTrue(tokens.contains("thông tin"));
        assertTrue(tokens.contains("việt nam"));

        analyzer.close();
    }

    @Test
    public void testEmptyText() throws IOException {
        VietnameseConfig config = new VietnameseConfig();
        Analyzer analyzer = new VietnameseAnalyzer(config);

        String text = "";
        List<String> tokens = tokenize(analyzer, text);

        assertNotNull(tokens);
        assertEquals(0, tokens.size());

        analyzer.close();
    }

    @Test
    public void testSpecialCharacters() throws IOException {
        VietnameseConfig config = new VietnameseConfig();
        Analyzer analyzer = new VietnameseAnalyzer(config);

        String text = "Email: test@example.com, Phone: 0123456789";
        List<String> tokens = tokenize(analyzer, text);

        assertNotNull(tokens);
        assertTrue(tokens.size() > 0);

        analyzer.close();
    }

    private List<String> tokenize(Analyzer analyzer, String text) throws IOException {
        List<String> tokens = new ArrayList<>();
        try (TokenStream tokenStream = analyzer.tokenStream("test", new StringReader(text))) {
            CharTermAttribute charTermAttribute = tokenStream.addAttribute(CharTermAttribute.class);
            tokenStream.reset();
            while (tokenStream.incrementToken()) {
                tokens.add(charTermAttribute.toString());
            }
            tokenStream.end();
        }
        return tokens;
    }
}

