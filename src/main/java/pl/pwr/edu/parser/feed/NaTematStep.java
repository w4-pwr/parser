package pl.pwr.edu.parser.feed;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import pl.pwr.edu.parser.domain.Article;
import pl.pwr.edu.parser.domain.Quote;
import pl.pwr.edu.parser.util.JsoupConnector;

@Component
@Order(100)
public class NaTematStep extends ParserTemplateStep {

	private String yearToCheck = "";
	private static String baseUrl = "http://natemat.pl";
	private static String articleListUrl = "http://natemat.pl/posts-map/";

	private static int SLEEP_TIME = 500;

	@Override
	public void parse() {
		List<String> links = getArticlesLinks();
		links.parallelStream()
				.forEach(this::parse);
	}

	private void parse(String link) {
		Optional<Article> article = Optional.ofNullable(parseLink(link));
		article.ifPresent(this::writeArticle);
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
		List<String> subcategoriesLinks = getSubcategoriesLinks();
		return subcategoriesLinks.parallelStream()
				.map(this::getArticlesForSubcategory)
				.flatMap(List::stream)
				.collect(Collectors.toList());
	}

	private List<String> getArticlesForSubcategory(String s) {
		List<String> links = new ArrayList<>();
		try {
			if (!s.contains(baseUrl)) {
				s = baseUrl + s;
			}
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
		if (titleElement == null || titleElement.isEmpty()) {
			return null;
		}

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
		if (author == null) {
			return;
		}
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
		if (author.contains("Partnerem")) {
			return null;
		}
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
		if (topics == null) {
			return "";
		}
		return topics.select("li").stream().filter(e -> !e.hasClass("art__topics__header")).map(e -> e.text().trim())
				.collect(Collectors.joining(","));
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
		return splited.length > 1 && splited[1].startsWith(yearToCheck);
	}


}
