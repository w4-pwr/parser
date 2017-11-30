package pl.pwr.edu.parser.feed;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;
import pl.pwr.edu.parser.domain.Article;
import pl.pwr.edu.parser.util.JsoupConnector;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

@Component
public class Zaufana3StronaStep implements Step {

	private final static String pageLink = "https://zaufanatrzeciastrona.pl/page/";
	private final static int SLEEP_TIME = 3500;

	@Override
	public List<Article> parse() {
		List<String> links = getLinks();
		List<Article> articles = links.stream()
				.map(this::parseLink)
				.filter(Objects::nonNull)
				.collect(Collectors.toList());

		articles.forEach(this::writeArticle);
		return articles;
	}

	private void writeArticle(Article article) {
		throw new NotImplementedException();
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

	private List<String> getLinks() {
		List<String> links = new ArrayList<>();
		try {
			for (int pageNumber = 1; ; pageNumber++) {
				Document doc = JsoupConnector.connectThrowable(pageLink + pageNumber, SLEEP_TIME);
				doc.select(".entry-title").forEach(title -> {
					links.add(title.select("a").attr("href"));
				});
			}
		} catch (Exception e) {
			return links;
		}
	}

	private String retrievePostUrl(Element post) {
		String lookForAuthor = post.select(".postmeta").toString();
		if (lookForAuthor.contains("Autor: redakcja")) {
			return null;
		}

		return post.select(".title").select("h2").select("a").attr("href");
	}

}
