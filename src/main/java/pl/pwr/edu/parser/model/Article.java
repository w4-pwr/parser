package pl.pwr.edu.parser.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Article {
    private String source;
    private String title;
    private String body;
    private HashMap<String, String> metadata = new HashMap<>();
    private List<Quote> quotes = new ArrayList<>();

    public Article(String url) {
        this.source = url;
    }

    public String getTitle() {
        return title;
    }

    public Article setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getBody() {
        return body;
    }

    public Article setBody(String body) {
        this.body = body;
        return this;
    }

    public HashMap<String, String> getMetadata() {
        return metadata;
    }

    public Article setMetadata(HashMap<String, String> metadata) {
        this.metadata = metadata;
        return this;
    }

    public List<Quote> getQuotes() {
        return quotes;
    }

    public Article setQuotes(List<Quote> quotes) {
        this.quotes = quotes;
        return this;
    }

    public String getSource() {
        return source;
    }

    public Article setSource(String source) {
        this.source = source;
        return this;
    }

    @Override
    public String toString() {
        return "Article{" +
                "source=" + source +
                ", title='" + title + '\'' +
                ", metadata=" + metadata +
                ", quotes=" + quotes +
                ", body='" + body + '\'' +
                '}';
    }
}
