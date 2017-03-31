package pl.pwr.edu.parser.feed;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import pl.pwr.edu.parser.model.Article;
import pl.pwr.edu.parser.model.RacjonalistaArticle;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class RacjonalistaStep implements Step {

    private static String baseUrl = "http://www.racjonalista.pl";
    private static String articleListUrl = "http://www.racjonalista.pl/index.php/s,27";

    @Override
    public List<Article> parse() {
        List<Article> articles = new ArrayList<>();

        try {
            List<String> links = getArticlesLinks();
            links.forEach(link -> articles.add(parseLink(link)));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return articles;
    }

    private List<String> getArticlesLinks() throws IOException {
        Document doc = Jsoup.connect(articleListUrl).get();

        return doc.select("#oTxt").first()
                .select(".linkart")
                .stream()
                .map(link -> link.attr("href"))
                .collect(Collectors.toList());
    }

    private Article parseLink(String articleUrl) {
        RacjonalistaArticle article = new RacjonalistaArticle();
        try {
            Document doc = Jsoup.connect(baseUrl + articleUrl).get();
            article.setTitle(doc.select("meta[property=og:title]").attr("content"));

            HashMap<String, String> metaData = new HashMap<>();
            metaData.put("author", doc.select("a[rel=author]").text());
            metaData.put("category", doc.select(".linkdzial").last().getElementsByTag("b").text());
            metaData.put("keywords", doc.select("meta[name=keywords]").attr("content"));
            metaData.put("description", doc.select("meta[name=description]").attr("content"));


            article.setMetadata(metaData);
            article.setBody(doc.select("#oTxt").first().select("p").text().trim());
        } catch (IOException e) {
            e.printStackTrace();
        }


        return article;
    }
}
