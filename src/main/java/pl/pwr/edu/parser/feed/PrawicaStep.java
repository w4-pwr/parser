package pl.pwr.edu.parser.feed;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import pl.pwr.edu.parser.model.Article;
import pl.pwr.edu.parser.model.PrawicaArticle;
import pl.pwr.edu.parser.model.RacjonalistaArticle;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class PrawicaStep implements Step {

    private static String baseUrl = "http://www.prawica.net";

    @Override
    public List<Article> parse() {
        Random rand = new Random();
        List<Article> articles = new ArrayList<>();


        //pasek postępu -> do usuniecia
        IntStream.range(0,409).forEach(i-> System.out.print("."));
        System.out.println("XX");

        List<String> links = getArticlesLinks(baseUrl,null);

        links.forEach(link -> {
            try {
                Article article = parseLink(link);
                articles.add(article);
                System.out.println(article);
                Thread.sleep(rand.nextInt(1000));
            } catch (InterruptedException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
            }
        });

        return articles;
    }

    private List<String> getArticlesLinks( String url, List<String> links) {
        if(links==null) {
            links = new ArrayList<>();
        }
        if (!url.contains(baseUrl)) {
            url = baseUrl + url;
        }
        try {
            Document doc = Jsoup.connect(url).get();
            links.addAll(doc.select("#content").first()
                    .select("article")
                    .stream()
                    .map(link ->link.select("a").first().attr("href"))
                    .collect(Collectors.toList()));
            Element nextPage = doc.select(".pager-next").first();
            if(nextPage.select("a").first()!=null){
                System.out.print(".");
                String link = nextPage.select("a").first().attr("href");
                links.addAll(getArticlesLinks(link,links));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return links;
    }

    private Article parseLink(String articleUrl) {
        Article article = new PrawicaArticle();
        try {
            Document doc = Jsoup.connect(baseUrl + articleUrl).userAgent("Mozilla/5.0").get();

            article.setTitle(doc.select("#page-title").first().text().trim());
            parseArticleMetaData(article, doc);
            article.setBody(parseArticleBody(doc));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return article;
    }

    private void parseArticleMetaData(Article article, Document doc) {
        HashMap<String, String> metaData = new HashMap<>();
//        Element category = doc.select(".linkdzial").last();
//        metaData.put("author", doc.select("a[rel=author]").text().trim());
//        metaData.put("category", category.getElementsByTag("b").text().trim());
//        metaData.put("date", Jsoup.parse(category.nextSibling().nextSibling().toString()).text().trim());
        metaData.put("keywords", doc.select("meta[name=keywords]").attr("content").trim());
        metaData.put("description", doc.select("meta[name=description]").attr("content").trim());

        article.setMetadata(metaData);
    }

    private String parseArticleBody(Document doc) throws IOException {


        return doc.select(".field-item").first().select("p").text().trim();
    }

    public static void main(String[] args) {

        //main do testów -> potem wywalic
        PrawicaStep prawicaStep = new PrawicaStep();
        prawicaStep.parse();
    }
}
