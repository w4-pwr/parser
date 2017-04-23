package pl.pwr.edu.parser.feed;

import static java.util.stream.Collectors.toList;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import pl.pwr.edu.parser.model.Article;
import pl.pwr.edu.parser.model.PurePcArticle;
import pl.pwr.edu.parser.util.SchemaUtils;
import pl.pwr.edu.parser.util.TagUtils;

/**
 * Created by evelan on 19/04/2017.
 */
public class PurePcStep implements Step {

  private static final String BASE_URL = "https://www.purepc.pl";

  @Override
  public List<Article> parse() {
    int page = 0;
    return getPageArticles(page);
  }

  private List<Article> getPageArticles(int page) {
    String articlePageUrl = getArticlePageUrl(page);
    List<String> allArticleUrls = getAllArticleUrlsFromUrl(articlePageUrl);
    return allArticleUrls
        .stream()
        .map(this::getArticle)
        .filter(Optional::isPresent)
        .map(Optional::get)
        .collect(toList());
  }

  private String getArticlePageUrl(int page) {
    return BASE_URL + "/artykuly?page=" + page;
  }

  private List<String> getAllArticleUrlsFromUrl(String url) {
    try {
      Document document = Jsoup.connect(url).get();
      return extractAllArticleLinks(document);
    } catch (IOException e) {
      System.err.printf("Cannot fetch article urls, " + e.getMessage());
      e.printStackTrace();
      return Collections.emptyList();
    }
  }

  private List<String> extractAllArticleLinks(Document document) {
    Elements articleElements = document.getElementsByClass("nl_item");
    return articleElements
        .stream()
        .map(this::extractArticleUrl)
        .filter(Optional::isPresent)
        .map(Optional::get)
        .collect(Collectors.toList());
  }

  private Optional<String> extractArticleUrl(Element articleElement) {
    Elements linkElement = articleElement.getElementsByTag("a");
    return Optional
        .ofNullable(linkElement.first())
        .map(element -> element.attr("href"))
        .map(link -> BASE_URL + link);
  }

  private Optional<Article> getArticle(String articleUrl) {
    try {
      Document articleDocument = fetchArticleDocument(articleUrl);
      Article article = extractArticle(articleDocument);
      System.out.println(article);
      return Optional.of(article);
    } catch (IOException e) {
      e.printStackTrace();
    } catch (IllegalArgumentException iae) {
      System.err.println("Cannot fetch article: " + articleUrl + " -> " + iae.getMessage());
    } catch (Exception e) {
      e.printStackTrace();
    }
    return Optional.empty();
  }

  private Document fetchArticleDocument(String articleUrl) throws IOException {
    return Jsoup.connect(articleUrl).get();
  }

  private Article extractArticle(Document articleDocument) throws IOException {
    Article article = new PurePcArticle();
    article.setSource(new URL(articleDocument.location()));
    article.setTitle(getTitle(articleDocument));
    article.setBody(getBody(articleDocument));
    article.setMetadata(getMetadata(articleDocument));
    article.setQuotes(Lists.newArrayList());
    return article;
  }

  private String getTitle(Document articleDocument) {
    return SchemaUtils
        .getItemPropValue("name", articleDocument)
        .orElseThrow(() -> new IllegalArgumentException("Cannot parse title"));
  }

  private String getBody(Document articleDocument) {
    return Optional
        .ofNullable(articleDocument)
        .map(document -> document.getElementsByClass("content"))
        .map(Elements::first)
        .map(document -> document.select(".PageMenuList").remove())
        .map(Elements::text)
        .orElseThrow(() -> new IllegalArgumentException("Cannot parse body text"));
  }

  private HashMap<String, String> getMetadata(Document articleDocument) {
    HashMap<String, String> metaData = Maps.newHashMap();
    metaData.put("author", getAuthor(articleDocument));
    metaData.put("date", getDate(articleDocument));
    metaData.put("tags", getTags(articleDocument));
    metaData.put("category", getCategory(articleDocument));
    return metaData;
  }

  private String getAuthor(Document articleDocument) {
    return SchemaUtils
        .getItemPropValue("author", articleDocument)
        .orElseThrow(() -> new IllegalArgumentException("Cannot parse author"));
  }

  private String getDate(Document articleDocument) {
    return SchemaUtils
        .getItemPropValue("datePublished", articleDocument)
        .orElseThrow(() -> new IllegalArgumentException("Cannot parse date"));
  }

  private String getTags(Document articleDocument) {
    return SchemaUtils
        .getMetaValue("name", "keywords", articleDocument)
        .map(TagUtils::getTrimedAndCommaSeparatedTags)
        .orElseThrow(() -> new IllegalArgumentException("Cannot parse tags"));
  }

  private String getCategory(Document articleDocument) {
    Elements categoryElements = articleDocument.getElementsByClass("bc_target");
    return Optional
        .ofNullable(categoryElements)
        .map(Elements::text)
        .orElseThrow(() -> new IllegalArgumentException("Cannot parse category"));
  }
}