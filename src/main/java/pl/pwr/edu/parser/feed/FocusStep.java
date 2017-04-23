package pl.pwr.edu.parser.feed;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import pl.pwr.edu.parser.model.Article;
import pl.pwr.edu.parser.model.FocusArticle;
import pl.pwr.edu.parser.util.SchemaUtils;
import pl.pwr.edu.parser.util.TagUtils;

/**
 * Created by evelan on 19/04/2017.
 */
public class FocusStep implements Step {

  private static final String BASE_URL = "http://www.focus.pl";

  @Override
  public List<Article> parse() {
    int lastArticleId = 12000;
    return fetchArticles(lastArticleId);
  }

  private List<Article> fetchArticles(int lastArticleId) {
    List<Article> articles = Lists.newArrayList();
    int firstArticleId = 14397;
    for (int articleId = firstArticleId; articleId > lastArticleId; articleId--) {
      String articleUrl = getArticleUrl(articleId);
      Optional<Article> article = getArticle(articleUrl);
      article.ifPresent(articles::add);
    }
    return articles;
  }

  private String getArticleUrl(int articleId) {
    return BASE_URL + "/technologie/akcent-przez-twe-oczy-zielone-" + articleId;
  }

  private Optional<Article> getArticle(String articleUrl) {
    try {
      Document document = fetchArticle(articleUrl);
      Article article = extractArticle(document);
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

  private Document fetchArticle(String articleUrl) throws IOException {
    return Jsoup.connect(articleUrl).get();
  }

  private Article extractArticle(Document document) throws IOException {
    Article article = new FocusArticle();
    article.setSource(new URL(document.location()));
    article.setTitle(getTitle(document));
    article.setBody(getBody(document));
    article.setMetadata(getMetadata(document));
    article.setQuotes(Collections.emptyList());
    return article;
  }

  private HashMap<String, String> getMetadata(Document articleDocument) {
    HashMap<String, String> metaData = Maps.newHashMap();
    metaData.put("author", getAuthor(articleDocument));
    metaData.put("tags", getTags(articleDocument));
    metaData.put("date", getDate(articleDocument));
    metaData.put("category", getCategory(articleDocument));
    return metaData;
  }

  private String getTitle(Document articleDocument) {
    return SchemaUtils
        .getMetaValue("property", "og:title", articleDocument)
        .map(Strings::nullToEmpty)
        .map(String::trim)
        .orElseThrow(() -> new IllegalArgumentException("Cannot parse title"));
  }

  private String getAuthor(Document articleDocument) {
    return Optional
        .ofNullable(articleDocument)
        .map(document -> document.getElementsByClass("author_desc"))
        .map(Elements::text)
        .orElseThrow(() -> new IllegalArgumentException("Cannot parse author name"));
  }

  private String getTags(Document articleDocument) {
    return SchemaUtils
        .getMetaValue("name", "keywords", articleDocument)
        .map(TagUtils::getTrimedAndCommaSeparatedTags)
        .orElseThrow(() -> new IllegalArgumentException("Cannot parse tags"));
  }

  private String getBody(Document articleDocument) {
    return Optional
        .ofNullable(articleDocument)
        .map(document -> document.getElementsByClass("inner_article"))
        .map(Elements::first)
        .map(Element::text)
        .orElseThrow(() -> new IllegalArgumentException("Cannot parse body test"));
  }

  private String getCategory(Document articleDocument) {
    return getMetaSourceTagText(articleDocument, "a")
        .orElseThrow(() -> new IllegalArgumentException("Cannot parse category"));
  }

  private String getDate(Document articleDocument) {
    return getMetaSourceTagText(articleDocument, "time")
        .orElseThrow(() -> new IllegalArgumentException("Cannot parse date"));
  }

  private Optional<String> getMetaSourceTagText(Document articleDocument, String tagName) {
    Elements metaSourceElements = articleDocument.getElementsByClass("meta_source");
    return metaSourceElements
        .stream()
        .map(element -> element.getElementsByTag(tagName))
        .map(Elements::first)
        .map(Element::text)
        .findFirst();
  }

}

