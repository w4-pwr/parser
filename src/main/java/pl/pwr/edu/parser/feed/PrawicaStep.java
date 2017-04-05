package pl.pwr.edu.parser.feed;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import pl.pwr.edu.parser.model.Article;
import pl.pwr.edu.parser.model.PrawicaArticle;

public class PrawicaStep implements Step {

  private static final String BASE_URL = "http://www.prawica.net";

  public static void main(String[] args) {
    //main do testów -> potem wywalic
    PrawicaStep prawicaStep = new PrawicaStep();
    prawicaStep.parse();
  }

  @Override
  public List<Article> parse() {
    List<String> links = getArticlesLinks();
    System.out.println("www.prawica.net links amount: " + links.size());
    return links
        .stream()
        .distinct()
        .map(this::parseLink)
        .peek(System.out::println)
        .collect(Collectors.toList());
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
      article.setBody(parseArticleBody(doc));
    } catch (IOException e) {
      e.printStackTrace();
    }

    return article;
  }

  private void parseArticleMetaData(Article article, Document doc) {
    HashMap<String, String> metaData = Maps.newHashMap();
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

  private int numberOfPages(Document doc) {
    String[] pages = doc.select(".pager-current").first().text().trim().split(" z ");
    return new Integer(pages[1]);
  }

}
