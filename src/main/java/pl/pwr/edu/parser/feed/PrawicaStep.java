package pl.pwr.edu.parser.feed;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import pl.pwr.edu.parser.connector.DocumentReader;
import pl.pwr.edu.parser.log.LoadingBar;
import pl.pwr.edu.parser.model.Article;
import pl.pwr.edu.parser.model.PrawicaArticle;
import pl.pwr.edu.parser.xml.XmlWriter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class PrawicaStep implements Step {

    private static final String BASE_URL = "http://www.prawica.net";
    Pattern dateRegex = Pattern.compile("@ (.*), k");


    private static String dir = System.getProperty("user.home") + "\\Desktop\\Prawica\\";
    private static final int MAX_CONNECTION = 5;
    private static int SLEEP_TIME = 5500;
    private int parsedArticles = 0;


    @Override
    public List<Article> parse() {
        List<String> links = getArticlesLinks();
        System.out.println("www.prawica.net links amount: " + links.size());
        LoadingBar loadingBar = new LoadingBar();
        loadingBar.setHorizontalMaxNumber(links.size());
        links.parallelStream()
                .map(this::parseLink)
                .filter(a -> a != null)
                .peek(a -> XmlWriter.writeArticleToFile(dir, a))
                .peek(a -> loadingBar.indicateHorizontalLoading(parsedArticles))
                .count();
        return new ArrayList<>();
    }

    private List<String> getArticlesLinks() {
        List<String> links = Lists.newArrayList();
        try {
            Document doc = Jsoup.connect(BASE_URL).get();
            int pages = numberOfPages(doc);
            LoadingBar loadingBar = new LoadingBar();
            loadingBar.createVerticalLoadingBar(pages);
            return IntStream.range(0, pages)
                    .parallel()
                    .mapToObj(i -> getArticlesLinks(BASE_URL + "/?page=" + i))
                    .peek(a -> loadingBar.indicateVerticalLoading())
                    .flatMap(List::stream).collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return links;
    }

    private List<String> getArticlesLinks(String url) {
        if (!url.contains(BASE_URL)) {
            url = BASE_URL + url;
        }
        Document doc = DocumentReader.getDocument(url, MAX_CONNECTION, SLEEP_TIME);
        if (doc == null)
            return new ArrayList<>();
        else
            return doc.select("#content").first()
                    .select("article")
                    .stream()
                    .map(link -> link.select("a").first().attr("href"))
                    .collect(Collectors.toList());


    }

    private Article parseLink(String articleUrl) {
        parsedArticles++;
        Article article = new PrawicaArticle();
        try {

            Document doc = DocumentReader.getDocument(BASE_URL + articleUrl, MAX_CONNECTION, SLEEP_TIME);
            article.setTitle(doc.select("#page-title").first().text().trim());
            parseArticleMetaData(article, doc);
            if (article.getMetadata() == null)
                return null;
            article.setBody(parseArticleBody(doc));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return article;
    }

    private void parseArticleMetaData(Article article, Document doc) {
        HashMap<String, String> metaData = Maps.newHashMap();
        if (doc.select(".submitted-by") == null || doc.select(".submitted-by").isEmpty()) {
            return;
        }
        metaData.put("author", doc.select(".username").text().trim());
        metaData.put("date", getDate(doc.select(".submitted-by").first().text()));
        metaData.put("keywords", doc.select("meta[name=keywords]").attr("content").trim().replace("| prawica.net",""));
        metaData.put("description", doc.select("meta[name=description]").attr("content").trim());
        article.setMetadata(metaData);
    }

    private String getDate(String text) {
        Matcher matcher = dateRegex.matcher(text);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return "";
    }

    private String parseArticleBody(Document doc) throws IOException {
        return doc.select(".field-item").first().select("p").text().trim();
    }

    private int numberOfPages(Document doc) {
        String[] pages = doc.select(".pager-current").first().text().trim().split(" z ");
        return new Integer(pages[1]);
    }

}
