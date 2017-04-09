package pl.pwr.edu.parser.feed;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import pl.pwr.edu.parser.model.Article;
import pl.pwr.edu.parser.model.PrawicaArticle;

import javax.xml.bind.JAXB;

public class PrawicaStep implements Step {

    private static final String BASE_URL = "http://www.prawica.net";
    Pattern dateRegex = Pattern.compile("@ (.*), k");

    @Override
    public List<Article> parse() {
        List<String> links = getArticlesLinks();
        System.out.println("www.prawica.net links amount: " + links.size());
        return links
                .stream()
                .distinct()
                .map(this::parseLink)
 //               .peek(a -> createXMLFile(a))
                .collect(Collectors.toList());
    }

    private void createXMLFile(Article a) {
        File dir = new File(System.getProperty("user.home")
                + "\\Desktop\\Prawica\\");
        dir.mkdir();
        JAXB.marshal(a,
                dir.getAbsolutePath() + "\\" + a.hashCode() + ".xml");
    }

    private List<String> getArticlesLinks() {
        List<String> links = Lists.newArrayList();
        try {
            Document doc = Jsoup.connect(BASE_URL).get();
            int pages = numberOfPages(doc);
            IntStream.range(0, pages).forEach(i -> System.out.print("."));
            System.out.println("END");
            IntStream.range(0, pages).forEach(i -> getArticlesLinks(BASE_URL + "/?page=" + i, links));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return links;
    }

    private void getArticlesLinks(String url, List<String> links) {

        if (!url.contains(BASE_URL)) {
            url = BASE_URL + url;
        }
        try {
            Document doc = Jsoup.connect(url).get();
            links.addAll(doc.select("#content").first()
                    .select("article")
                    .stream()
                    .map(link -> link.select("a").first().attr("href"))
                    .collect(Collectors.toList()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.print(".");
    }

    private Article parseLink(String articleUrl) {
        Article article = new PrawicaArticle();
        try {
            Document doc = Jsoup.connect(BASE_URL + articleUrl).userAgent("Mozilla/5.0").get();

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
        metaData.put("keywords", doc.select("meta[name=keywords]").attr("content").trim());
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
