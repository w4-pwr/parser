package pl.pwr.edu.parser.feed;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import pl.pwr.edu.parser.log.LoadingBar;
import pl.pwr.edu.parser.model.Article;
import pl.pwr.edu.parser.model.Quote;
import pl.pwr.edu.parser.util.JsoupConnector;
import pl.pwr.edu.parser.util.xml.XMLWriter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class NaTematStep implements Step {

    private static final int MAX_CONNECTION = 5;
    private String yearToCheck = "";
    private static String baseUrl = "http://natemat.pl";
    private static String articleListUrl = "http://natemat.pl/posts-map/";
    private static String dir = System.getProperty("user.home") + "\\Desktop\\NaTemat\\";

    private static int SLEEP_TIME = 5500;
    private int parsedArticles = 0;

    @Override
    public List<Article> parse() {
        List<String> links = getArticlesLinks();
        int size = links.size();
        LoadingBar loadingBar = new LoadingBar();
        loadingBar.setHorizontalMaxNumber(size);
        links.parallelStream().peek(a -> loadingBar.indicateHorizontalLoading(parsedArticles)).forEach(link -> parse(link));
        return new ArrayList<>();
    }

    private void parse(String link) {
        Article article = parseLink(link);
        parsedArticles++;
        if (article != null) {
            //articles.add(article);
            XMLWriter.writeArticleToFile(dir, article);
        }
    }


    private List<String> getSubcategoriesLinks() {
        List<String> links = new ArrayList<>();

        try {
            Document doc = Jsoup.connect(articleListUrl).get();
            links.addAll(doc.select("#main").first()
                    .select("a")
                    .stream()
                    .map(link -> link.attr("href"))
                    .filter(this::isFromYear)
                    .collect(Collectors.toList()));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return links;
    }

    private List<String> getArticlesLinks() {
        LoadingBar loadingBar = new LoadingBar();

        List<String> subcategoriesLinks = getSubcategoriesLinks();
        loadingBar.createVerticalLoadingBar(subcategoriesLinks.size());

        List<String> links = subcategoriesLinks.parallelStream().
                map(this::getArticlesForSubcategory)
                .peek(s -> loadingBar.indicateVerticalLoading())
                .flatMap(List::stream).collect(Collectors.toList());

        return links;
    }

    private List<String> getArticlesForSubcategory(String s) {
        List<String> links = new ArrayList<>();
        try {
            if (!s.contains(baseUrl))
                s = baseUrl + s;
            Document doc = Jsoup.connect(s).get();
            links.addAll(doc.select("#main").first()
                    .select("li a")
                    .stream()
                    .map(link -> link.attr("href"))
                    .collect(Collectors.toList()));
            if (doc.select(".pages .pg_next").first() != null) {
                links.addAll(getArticlesForSubcategory(baseUrl + doc.select(".pages .pg_next").first().attr("href")));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return links;
    }

    private Article parseLink(String articleUrl) {
        Article article = new Article(articleUrl);

        Document doc = JsoupConnector.connect(articleUrl, SLEEP_TIME);

        Elements titleElement = doc.select(".art__title");
        if (doc == null || titleElement == null || titleElement.isEmpty())
            return null;

        article.setTitle(titleElement.first().text().trim());
        parseArticleMetaData(article, doc);
        if (!article.getMetadata().containsKey("author")) {
            return null;
        }
        article.setQuotes(parseArticleQuotes(doc));
        removeFootNotes(doc);
        article.setBody(parseArticleBody(doc, article.getMetadata()));


        return article;
    }


    private void parseArticleMetaData(Article article, Document doc) {
        HashMap<String, String> metaData = new HashMap<>();

        String author = getAuthor(doc);
        if (author == null) return;
        metaData.put("author", author);
        metaData.put("category", getCategory(doc));
        metaData.put("date", getDate(doc));
        metaData.put("keywords", findTopics(doc));
        metaData.put("description", doc.select("meta[property=og:description]").attr("content").trim());

        article.setMetadata(metaData);
    }

    private String getDate(Document doc) {
        Element dateElement = doc.select(".art__date").first();
        dateElement.remove();
        return dateElement.text().trim();
    }

    private String getAuthor(Document doc) {
        Element authorElement = doc.select(".art__author__name").first();
        String author = authorElement.text().trim();
        if (author.contains("Partnerem"))
            return null;
        authorElement.remove();
        return author;
    }

    private String getCategory(Document doc) {
        String category = doc.select(".art__progress__category").first().text().trim();
        if (category.isEmpty()) {
            category = doc.getElementsByAttribute("data-category").first().attributes().get("data-category");
        }
        return category;
    }

    private String findTopics(Document doc) {
        doc.select(".art__header__photo__caption").remove();
        Element topics = doc.select(".art__topics__list").first();
        if (topics == null)
            return "";
        return topics.select("li").stream().filter(e -> !e.hasClass("art__topics__header")).map(e -> e.text().trim()).collect(Collectors.joining(","));
    }

    private String parseArticleBody(Document doc, HashMap<String, String> metaData) {

        String page = doc.select(".art__body").first().text().trim();
        return page.replaceFirst(metaData.get("author"), "").replace("http.?://\\S+", "");
    }

    private List<Quote> parseArticleQuotes(Document doc) {

        List<Quote> quotes = new ArrayList<>();
        getQuotes(doc, quotes, "blockquote", ".author-about .name");
        getQuotes(doc, quotes, ".EmbeddedTweet-tweet", ".TweetAuthor-name");
        return quotes;
    }


    private void getQuotes(Document doc, List<Quote> quotes, String blockSelector, String authorSelector) {
        doc.select(".art__body").first().select(blockSelector).forEach(s -> {
            Quote quote = new Quote();
            Element author = s.select(authorSelector).first();
            quote.setDescription(author != null ? author.text().trim() : "");
            Element body = s.select("p").first();
            if (body != null) {
                quote.setBody(body.text().trim());
                quotes.add(quote);
            }
            s.remove();


        });
    }

    private void removeFootNotes(Document doc) {
        doc.select(".art__body__photo").remove();
        doc.select("em").remove();
    }

    private boolean isFromYear(String link) {
        String[] splited = link.split(",");
        if (splited.length > 1 && splited[1].startsWith(yearToCheck))
            return true;
        return false;
    }

    @Override
    public void setYearToCheck(String yearToCheck) {
        this.yearToCheck = yearToCheck;
    }


}
