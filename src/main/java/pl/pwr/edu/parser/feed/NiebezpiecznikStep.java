package pl.pwr.edu.parser.feed;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import pl.pwr.edu.parser.domain.Article;
import pl.pwr.edu.parser.util.JsoupConnector;

@Component
@Order(20)
public class NiebezpiecznikStep extends ParserTemplateStep {

	private final static String pageLink = "https://niebezpiecznik.pl/page/";
	private final static int SLEEP_TIME = 500;

	@Override
	public void parse() {
		try {
			tryParse();
		} catch (Exception ignored) {
		}
	}

	private void tryParse() {
		int pageNumber = 1;
		while (pageNumber < 100) {
			Set<String> linksOnPage = getLinksOnPage(pageNumber);
			parseLinks(linksOnPage);
			pageNumber++;
		}
	}

	private Set<String> getLinksOnPage(int page) {
		Document doc = JsoupConnector.connect(pageLink + page, SLEEP_TIME);
		return doc.select(".post")
				.stream()
				.map(this::retrievePostUrl)
				.filter(Objects::nonNull)
				.collect(Collectors.toSet());
	}

	private void parseLinks(Set<String> linksOnPage) {
		linksOnPage.stream()
				.map(this::parseLink)
				.filter(Optional::isPresent)
				.map(Optional::get)
				.forEach(this::writeArticle);
	}

	private Optional<Article> parseLink(String articleUrl) {
		try {
			Article article = new Article(articleUrl);
			Document doc = JsoupConnector.connectThrowable(articleUrl, SLEEP_TIME);
			article.setTitle(doc.select(".title").select("a[rel=bookmark]").text());
			article.getMetadata().put("author", getAuthor(doc));
			article.getMetadata().put("keywords", getKeywords(doc));
			article.getMetadata().put("date", getDate(doc));
			article.setBody(getBody(doc));

			return Optional.of(article);
		} catch (Exception e) {
			return Optional.empty();
		}
	}

	private String getBody(Document doc) {
		doc.select("blockquote").remove();
		doc.select(".wp-caption").remove();
		String fullText = doc.select(".entry").text();
		String[] stringi = fullText.split("Przeczytaj także:");
		if (stringi.length > 0) {
			return stringi[0];
		}
		return fullText;
	}

	private String getDate(Document doc) {
		return doc.select(".date").text();
	}

	private String getKeywords(Document doc) {
		StringBuilder keywords = new StringBuilder();
		doc.select("a[rel=tag]").forEach(a -> keywords.append(a.text()).append(", "));
		return keywords.toString().trim();
	}

	private String getAuthor(Document doc) {
		String postmeta = doc.select(".postmeta").text();
		Pattern pattern = Pattern.compile("Autor: (.+) \\|");
		Matcher matcher = pattern.matcher(postmeta);
		if (matcher.find()) {
			return matcher.group(1);
		}
		return null;
	}

	private String retrievePostUrl(Element post) {
		String lookForAuthor = post.select(".postmeta").toString();
		if (lookForAuthor.contains("Autor: redakcja")) {
			return null;
		}
		try {
			return post.select(".title").select("h2").select("a").attr("href");
		} catch (Exception e) {
			return null;
		}
	}

}
