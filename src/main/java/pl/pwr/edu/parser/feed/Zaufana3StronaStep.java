package pl.pwr.edu.parser.feed;

import com.google.common.collect.Sets;
import java.util.Objects;
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
@Order(40)
public class Zaufana3StronaStep extends ParserTemplateStep {

	private final static String pageLink = "https://zaufanatrzeciastrona.pl/page/";
	private final static int SLEEP_TIME = 500;

	@Override
	public void parse() {
		int page = 1;
		while (page < 100) {
			Set<String> linksOnPage = tryGetArticleLinksOnPage(page);
			linksOnPage.stream()
					.map(this::parseLink)
					.filter(Objects::nonNull)
					.forEach(this::writeArticle);

			page++;
		}
	}


	private Set<String> tryGetArticleLinksOnPage(int page) {
		try {
			Document doc = JsoupConnector.connectThrowable(pageLink + page, SLEEP_TIME);
			return doc.select(".entry-title")
					.stream()
					.map(e -> e.select("a"))
					.map(e -> e.attr("href"))
					.collect(Collectors.toSet());
		} catch (Exception e) {
			return Sets.newHashSet();
		}
	}

	private Article parseLink(String articleUrl) {
		try {
			Article article = new Article(articleUrl);
			Document doc = JsoupConnector.connectThrowable(articleUrl, SLEEP_TIME);
			article.setTitle(doc.select(".postcontent").select("h1").text());
			article.getMetadata().put("author", getAuthor(doc));
			article.getMetadata().put("keywords", getKeywords(doc));
			article.getMetadata().put("date", getDate(doc));
			article.setBody(getBody(doc));

			return article;
		} catch (Exception e) {
			return null;
		}
	}

	private String getBody(Document doc) {
		doc.select("blockquote").remove();
		return doc.select(".postcontent").select("p").text();
	}

	private String getDate(Document doc) {
		String postmeta = doc.select(".dolna-ramka").select("span").text();
		Pattern pattern = Pattern.compile("(.+) w kategorii");
		Matcher matcher = pattern.matcher(postmeta);
		if (matcher.find()) {
			return matcher.group(1);
		}
		return null;
	}

	private String getKeywords(Document doc) {
		StringBuilder keywords = new StringBuilder();
		doc.select("a[rel=tag]").forEach(a -> keywords.append(a.text()).append(", "));
		return keywords.toString().trim();
	}

	private String getAuthor(Document doc) {
		return doc.select("a[rel=author]").text();
	}


	private String retrievePostUrl(Element post) {
		String lookForAuthor = post.select(".postmeta").toString();
		if (lookForAuthor.contains("Autor: redakcja")) {
			return null;
		}

		return post.select(".title").select("h2").select("a").attr("href");
	}

}
