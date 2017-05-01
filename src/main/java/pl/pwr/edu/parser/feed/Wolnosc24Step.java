package pl.pwr.edu.parser.feed;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import pl.pwr.edu.parser.log.LoadingBar;
import pl.pwr.edu.parser.model.Article;
import pl.pwr.edu.parser.util.xml.CMDIWriter;
import pl.pwr.edu.parser.util.xml.XMLWriter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import static com.google.common.collect.Lists.newArrayList;

/**
 * Created by panajot on 4/27/17.
 */
public class Wolnosc24Step implements Step {
    private static String baseUrl = "http://wolnosc24.pl/";
    private static String dir = System.getProperty("user.home") + "\\Desktop\\Wolnosc24\\";
    private int parsedArticles = 1;

    @Override
    public List<Article> parse() {

        List<String> allArticlesLinks = getArticlesLink();
//        for(String str : allArticlesLinks)
//            System.out.println(str);
//        System.out.println(allArticlesLinks.size());

        System.out.println(baseUrl);
        LoadingBar loadingBar = new LoadingBar();
        loadingBar.setHorizontalMaxNumber(allArticlesLinks.size());
        allArticlesLinks.stream().peek(a -> loadingBar.indicateHorizontalLoading(parsedArticles)).forEach(link -> parseLink(link));
//        allArticlesLinks.forEach(link -> parseLink(link));
        System.out.println("Finished!");

        return newArrayList();
    }

    void parse(Article article)
    {
//        try {
//        System.out.println(article.getTitle());
        XMLWriter.writeArticleToFile(dir, article);
        parsedArticles++;
//            CMDIWriter.writeArticleToFile(dir, articles.get(0));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

    private Article parseLink(String articleUrl) {
        Article article = new Article(articleUrl);

        try {
            Document doc = Jsoup.connect(articleUrl).get();
            Element el = doc.getElementsByTag("article").first();

            article.setTitle(el
                    .select("h1[class='entry-title']")
                    .text()
            );

            parseArticleMetaData(article, el);
            parseBody(article, el);

        } catch (IOException e) {
            e.printStackTrace();
        }

        parse(article);
        return article;
    }

    private void parseBody(Article article, Element el) {
        String text = el
                .getElementsByTag("p")
                .text();
//        System.out.println(text);
//        text = text.replaceAll("„(.*)”"," ");
//        System.out.println(text);
        article.setBody(text);
    }

    private void parseArticleMetaData(Article article, Element el) {
        HashMap<String, String> metaData = new HashMap<>();

        metaData.put("author", el
                .getElementsByClass("td-module-meta-info")
                .first()
                .select("a[href]")
                .text()
        );
        metaData.put("date", el
                .getElementsByClass("td-module-meta-info")
                .first()
                .select("time[dateTime]")
                .text()
        );

        article.setMetadata(metaData);
    }

    private List<String> getArticlesLink() {
        List<String> links = new ArrayList<>();
        links.addAll(getArticlesFromYear(2016));
        links.addAll(getArticlesFromYear(2017));
        return links;
    }

    private List<String> getArticlesFromYear(int year) {
        List<String> links = new ArrayList<>();
        String url = baseUrl + year + "/page/";

        try {
            Document doc = Jsoup.connect(baseUrl+year).get();
            String pagesStr = doc.getElementsByClass("page-nav")
                    .select("a[class='last']")
                    .attr("title")
                    .toString();
            int pages = Integer.parseInt(pagesStr);
            System.out.println("Pages: " + pages);
            for(int i = 1; i <= pages; ++i)
                links.addAll(getArticlesLinkFromPage(url, i));

        } catch (IOException e) {
            e.printStackTrace();
        }


        return links;
    }

    private List<String> getArticlesLinkFromPage(String url, int page) {
        List<String> links = new ArrayList<>();
        String basePageUrl = url + page;

        try {
            Document doc = Jsoup.connect(basePageUrl).get();

            links.addAll(doc.getElementsByClass("td-container ")
                    .select("div[class='item-details']")
                    .select("a")
                    .stream()
                    .map(link -> link.attr("href"))
                    .collect(Collectors.toList()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return links;
    }
}
