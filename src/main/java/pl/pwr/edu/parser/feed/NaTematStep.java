package pl.pwr.edu.parser.feed;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import pl.pwr.edu.parser.model.Article;
import pl.pwr.edu.parser.model.NaTematArticle;
import pl.pwr.edu.parser.model.Quote;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class NaTematStep implements Step {

    private String yearToCheck = "";
    private static String baseUrl = "http://natemat.pl";
    private static String articleListUrl = "http://natemat.pl/posts-map/";


    @Override
    public List<Article> parse() {
        Random rand = new Random();
        List<Article> articles = new ArrayList<>();

        List<String> links = getArticlesLinks();
        links.stream().parallel().forEach(link -> {
            Article article = parseLink(link);
            if(article!=null)
                articles.add(article);
        });

        return articles;
    }

    private List<String> getSubcategoriesLinks() {
        List<String> links = new ArrayList<>();

        try {
            Document doc = Jsoup.connect(articleListUrl).get();
            links.addAll(doc.select("#main").first()
                    .select("a")
                    .stream()
                    .map(link -> link.attr("href"))
                    .filter(l->isFromYear(l))
                    .collect(Collectors.toList()));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return links;
    }

    private List<String> getArticlesLinks() {
        List<String> links = new ArrayList<>();

        List<String> subcategoriesLinks = getSubcategoriesLinks();
        System.out.println("Subcategories pages to parse : " + subcategoriesLinks.size());

        //Drawing loading bar ->  to be removed
        IntStream.range(0, subcategoriesLinks.size()).forEach(i -> System.out.print("."));
        System.out.println("XX");

        links.addAll(subcategoriesLinks.stream().parallel().map(s -> {
            System.out.print(".");
            return getArticlesForSubcategory(s);
        }).flatMap(List::stream).collect(Collectors.toList()));

        System.out.println("\n number of pages to parse : " + links.size());
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
        Article article = new NaTematArticle();
        try {
            Document doc = Jsoup.connect(articleUrl).userAgent("Mozilla/5.0").get();

            //kiedy artykuł jest złożony z wielu artykułów lub jest reklamą
            //bardzo żadko ale morze się zdarzyć
            //przykład http://natemat.pl/199073,witaminy-dla-dzieci-aspiryna-srodek-na-niestrawnosci-najlepsze-kosmetyki-jakie-mialas-leza-w-twojej-apteczce
            if(doc.select(".art__title").first()==null){
                return null;
            }
            article.setTitle(doc.select(".art__title").first().text().trim());
            parseArticleMetaData(article, doc);
            article.setBody(parseArticleBody(doc, article.getMetadata()));
            article.setQuotes(parseArticleQuotes(doc));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return article;
    }

    private void parseArticleMetaData(Article article, Document doc) {
        HashMap<String, String> metaData = new HashMap<>();
        metaData.put("author", doc.select(".art__author__name").first().text().trim());
        metaData.put("category", getCategory(doc));
        metaData.put("date", doc.select(".art__date").first().text().trim());
        metaData.put("keywords", findTopics(doc));
        metaData.put("description", doc.select("meta[property=og:description]").attr("content").trim());

        article.setMetadata(metaData);
    }

    private String getCategory(Document doc) {
        String category = doc.select(".art__progress__category").first().text().trim();
        if (category.isEmpty()){
            category = doc.getElementsByAttribute("data-category").first().attributes().get("data-category");
        }
        return category;
    }

    private String findTopics(Document doc) {
        Element topics = doc.select(".art__topics__list").first();
        if(topics==null)
            return "";
        return topics.select("li").stream().filter(e->!e.hasClass("art__topics__header")).map(e->e.text().trim()).collect(Collectors.joining(","));
    }

    private String parseArticleBody(Document doc, HashMap<String, String> metaData) throws IOException {

        String page = doc.select(".art__body").first().text();
        return page.replaceFirst(metaData.get("author"), "").replaceFirst(metaData.get("date"), "").replace("(http.*)\\/","");
    }

    private List<Quote> parseArticleQuotes(Document doc) throws IOException {

        List<Quote> quotes = new ArrayList<>();
        getQuotesFromBlock(doc, quotes);
        getQuotesFromTweets(doc, quotes);
        return quotes;
    }

    private void getQuotesFromBlock(Document doc, List<Quote> quotes) {
        doc.select(".art__body").first().select("blockquote").forEach(s -> {
            Quote quote = new Quote();
            Element author = s.select(".author-about .name").first();
            quote.setAuthor(author != null ? author.text().trim() : "");
            Element body = s.select("p").first();
            if (body != null) {
                quote.setBody(body.text().trim());
                quotes.add(quote);
            }

        });
    }

    private void getQuotesFromTweets(Document doc, List<Quote> quotes) {
        doc.select(".art__body").first().select(".EmbeddedTweet-tweet").forEach(s -> {
            Quote quote = new Quote();
            Element author = s.select(".TweetAuthor-name").first();
            quote.setAuthor(author != null ? author.text().trim() : "");
            Element body = s.select("p").first();
            if (body != null) {
                quote.setBody(body.text().trim());
                quotes.add(quote);
            }

        });
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
