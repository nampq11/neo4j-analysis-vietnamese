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

import com.coccoc.Token;
import com.coccoc.Tokenizer;
import org.neo4j.analysis.VietnameseConfig;
import org.neo4j.procedure.*;

import java.util.List;
import java.util.stream.Stream;

/**
 * User-defined procedures for Vietnamese text analysis.
 * Provides Cypher functions to tokenize Vietnamese text.
 *
 * @author duydo
 */
public class VietnameseTextAnalysisProcedures {

    /**
     * Output class for tokenization results
     */
    public static class TokenResult {
        public final String token;
        public final String type;
        public final long startPosition;
        public final long endPosition;

        public TokenResult(String token, String type, long startPosition, long endPosition) {
            this.token = token;
            this.type = type;
            this.startPosition = startPosition;
            this.endPosition = endPosition;
        }
    }

    /**
     * Procedure to tokenize Vietnamese text
     *
     * @param text The Vietnamese text to tokenize
     * @return Stream of tokens with their positions and types
     */
    @Procedure(name = "vi.tokenize", mode = Mode.READ)
    @Description("Tokenize Vietnamese text using CocCoc tokenizer")
    public Stream<TokenResult> tokenize(
            @Name("text") String text,
            @Name(value = "config", defaultValue = "{}") java.util.Map<String, Object> config
    ) {
        if (text == null || text.isEmpty()) {
            return Stream.empty();
        }

        String dictPath = (String) config.getOrDefault("dictPath", VietnameseConfig.DEFAULT_DICT_PATH);
        Boolean keepPunctuation = (Boolean) config.getOrDefault("keepPunctuation", false);
        Boolean splitHost = (Boolean) config.getOrDefault("splitHost", false);
        Boolean splitURL = (Boolean) config.getOrDefault("splitURL", false);

        Tokenizer tokenizer = Tokenizer.getInstance(dictPath);
        Tokenizer.TokenizeOption option = splitURL ? Tokenizer.TokenizeOption.URL :
                (splitHost ? Tokenizer.TokenizeOption.HOST : Tokenizer.TokenizeOption.NORMAL);

        List<Token> tokens = tokenizer.segment(text, option, keepPunctuation);

        return tokens.stream()
                .map(token -> new TokenResult(
                        token.getText(),
                        token.getType().name(),
                        token.getPos(),
                        token.getEndPos()
                ));
    }

    /**
     * Function to extract tokens from Vietnamese text as a list
     *
     * @param text The Vietnamese text to tokenize
     * @return List of token strings
     */
    @UserFunction(name = "vi.tokens")
    @Description("Extract Vietnamese tokens from text as a list of strings")
    public List<String> tokens(
            @Name("text") String text,
            @Name(value = "config", defaultValue = "{}") java.util.Map<String, Object> config
    ) {
        if (text == null || text.isEmpty()) {
            return java.util.Collections.emptyList();
        }

        String dictPath = (String) config.getOrDefault("dictPath", VietnameseConfig.DEFAULT_DICT_PATH);
        Boolean keepPunctuation = (Boolean) config.getOrDefault("keepPunctuation", false);
        Boolean splitHost = (Boolean) config.getOrDefault("splitHost", false);
        Boolean splitURL = (Boolean) config.getOrDefault("splitURL", false);

        Tokenizer tokenizer = Tokenizer.getInstance(dictPath);
        Tokenizer.TokenizeOption option = splitURL ? Tokenizer.TokenizeOption.URL :
                (splitHost ? Tokenizer.TokenizeOption.HOST : Tokenizer.TokenizeOption.NORMAL);

        List<Token> tokens = tokenizer.segment(text, option, keepPunctuation);

        return tokens.stream()
                .map(Token::getText)
                .collect(java.util.stream.Collectors.toList());
    }
}

