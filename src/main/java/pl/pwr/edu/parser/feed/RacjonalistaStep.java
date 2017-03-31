package pl.pwr.edu.parser.feed;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import pl.pwr.edu.parser.model.Article;
import pl.pwr.edu.parser.model.RacjonalistaArticle;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class RacjonalistaStep implements Step {

    private static String baseUrl = "http://www.racjonalista.pl";
    private static String articleListUrl = "http://www.racjonalista.pl/index.php/s,27";

    @Override
    public List<Article> parse() {
        Random rand = new Random();
        List<Article> articles = new ArrayList<>();

        List<String> links = getArticlesLinks();
        links.forEach(link -> {
            try {
                Article article = parseLink(link);
                articles.add(article);
                System.out.println(article);
                Thread.sleep(rand.nextInt(1000) + 700);
            } catch (InterruptedException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
            }
        });

        return articles;
    }

    private List<String> getArticlesLinks() {
        List<String> links = new ArrayList<>();

        try {
            Document doc = Jsoup.connect(articleListUrl).get();
            links.addAll(doc.select("#oTxt").first()
                    .select(".linkart")
                    .stream()
                    .map(link -> link.attr("href"))
                    .collect(Collectors.toList()));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return links;
    }

    private Article parseLink(String articleUrl) {
        RacjonalistaArticle article = new RacjonalistaArticle();
        try {
            Document doc = Jsoup.connect(baseUrl + articleUrl).userAgent("Mozilla/5.0").get();

            article.setTitle(doc.select("meta[property=og:title]").attr("content"));
            parseArticleMetaData(article, doc);
            article.setBody(String.join("\n", parseArticleBody(doc, null)));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return article;
    }

    private void parseArticleMetaData(RacjonalistaArticle article, Document doc) {
        HashMap<String, String> metaData = new HashMap<>();
        Element category = doc.select(".linkdzial").last();
        metaData.put("author", doc.select("a[rel=author]").text().trim());
        metaData.put("category", category.getElementsByTag("b").text().trim());
        metaData.put("date", Jsoup.parse(category.nextSibling().nextSibling().toString()).text().trim());
        metaData.put("keywords", doc.select("meta[name=keywords]").attr("content").trim());
        metaData.put("description", doc.select("meta[name=description]").attr("content").trim());

        article.setMetadata(metaData);
    }

    private List<String> parseArticleBody(Document doc, List<String> pages) throws IOException {
        if (pages == null)
            pages = new ArrayList<>();

        pages.add(doc.select("#oTxt").first().select("p").text().trim());

        Element nextPageButton = doc.select(".pg").last();
        if (nextPageButton != null && nextPageButton.childNode(0) instanceof TextNode) {
            String nextPageLink = nextPageButton.attr("href");
            Document newPageDoc = Jsoup.connect(baseUrl + nextPageLink).userAgent("Mozilla/5.0").get();
            parseArticleBody(newPageDoc, pages);
        }

        return pages;
    }
}
