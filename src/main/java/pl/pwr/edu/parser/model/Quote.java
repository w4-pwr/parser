package pl.pwr.edu.parser.model;

/**
 * Created by Mateusz Manka on 01.04.2017.
 */
public class Quote {
    private String author;
    private String body;

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    @Override
    public String toString() {
        return "Quote{" +
                "author='" + author + '\'' +
                ", body='" + body + '\'' +
                '}';
    }
}
