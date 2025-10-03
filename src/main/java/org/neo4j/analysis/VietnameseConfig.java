package org.neo4j.analysis;

public class VietnameseConfig {
    public static final String DEFAULT_DICT_PATH = "/usr/local/share/tokenizer/dicts";
    public final String dictPath;
    public final Boolean keepPunctuation;
    public final Boolean splitHost;
    public final Boolean splitURL;

    public VietnameseConfig() {
        this(DEFAULT_DICT_PATH, false, false, false);
    }

    public VietnameseConfig(String dictPath, Boolean keepPunctuation, Boolean splitHost, Boolean splitURL) {
        this.dictPath = dictPath != null ? dictPath : DEFAULT_DICT_PATH;
        this.keepPunctuation = keepPunctuation != null ? keepPunctuation : Boolean.FALSE;
        this.splitHost = splitHost != null ? splitHost : Boolean.FALSE;
        this.splitURL = splitURL != null ? splitURL : Boolean.FALSE;
    }
}

