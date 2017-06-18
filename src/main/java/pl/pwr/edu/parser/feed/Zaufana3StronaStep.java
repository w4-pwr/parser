package pl.pwr.edu.parser.feed;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import pl.pwr.edu.parser.model.Article;
import pl.pwr.edu.parser.util.JsoupConnector;
import pl.pwr.edu.parser.util.xml.CMDIWriter;
import pl.pwr.edu.parser.util.xml.XMLWriter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Zaufana3StronaStep implements Step {

    private final static String pageLink = "https://zaufanatrzeciastrona.pl/page/";
    private final static int SLEEP_TIME = 3500;
    private final static String dir = System.getProperty("user.home") + File.separator + "studia" + File.separator + "zaufana3strona" + File.separator;

    @Override
    public List<Article> parse() {
        List <String> links = getLinks();
        return links.stream().map(this::parseLink)
                .peek(a -> {
                    if (a != null)
                        XMLWriter.writeArticleToFile(a, dir);
                })
                .peek(a -> {
                    if (a != null)
                        CMDIWriter.writeArticleToFile(a, dir);
                })
                .collect(Collectors.toList());
    }

    private Article parseLink(String articleUrl) {
        try {
            Article article = new Article(articleUrl);
            Document doc = JsoupConnector.connectThrowable(articleUrl, SLEEP_TIME);
            article.setTitle(doc.select(".postcontent").select("h1").text());
            article.getMetadata().put("author", getAuthor(doc));
            article.getMetadata().put("keywords", getKeywords(doc));
            article.getMetadata().put("date", getDate(doc));
            article.setBody(getBody(doc));

            return article;
        } catch (Exception e) {
            return null;
        }
    }

    private String getBody(Document doc) {
        doc.select("blockquote").remove();

        StringBuilder keywords = new StringBuilder();
        String fullText = doc.select(".postcontent").select("p").text();

        return fullText;
    }

    private String getDate(Document doc) {
        String postmeta = doc.select(".dolna-ramka").select("span").text();
        Pattern pattern = Pattern.compile("(.+) w kategorii");
        Matcher matcher = pattern.matcher(postmeta);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    private String getKeywords(Document doc) {
        StringBuilder keywords = new StringBuilder();
        doc.select("a[rel=tag]").forEach(a -> keywords.append(a.text() + ", "));
        return keywords.toString().trim();
    }

    private String getAuthor(Document doc) {
        return doc.select("a[rel=author]").text();
    }

    public List<String> getLinks() {
        List<String> links = new ArrayList<>();
        try {
            for (int pageNumber = 1; ; pageNumber++) {
                Document doc = JsoupConnector.connectThrowable(pageLink + pageNumber, SLEEP_TIME);
                doc.select(".entry-title").forEach(title -> {
                    links.add(title.select("a").attr("href"));
                });
            }
        } catch (Exception e) {
            return links;
        }
    }

    private String retrievePostUrl(Element post) {
        String lookForAuthor = post.select(".postmeta").toString();
        if (lookForAuthor.contains("Autor: redakcja")) {
            return null;
        }

        return post.select(".title").select("h2").select("a").attr("href");
    }

}
