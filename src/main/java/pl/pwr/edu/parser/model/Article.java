package pl.pwr.edu.parser.model;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public abstract class Article {
    private URL source;
    private String title;
    private String body;
    private HashMap<String, String> metadata = new HashMap<>();
    private List<Quote> quotes = new ArrayList<>();

    public Article(URL url) {
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

    @Override
    public String toString() {
        return "Article{" +
                "source=" + source +
                ", title='" + title + '\'' +
                ", body='" + body + '\'' +
                ", metadata=" + metadata +
                ", quotes=" + quotes +
                '}';
    }
}
