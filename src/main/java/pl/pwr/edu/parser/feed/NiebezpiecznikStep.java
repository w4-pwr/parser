package pl.pwr.edu.parser.feed;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;
import pl.pwr.edu.parser.domain.Article;
import pl.pwr.edu.parser.util.JsoupConnector;

@Component
public final class NiebezpiecznikStep extends ParserTemplateStep {

	private final static String pageLink = "https://niebezpiecznik.pl/page/";
	private final static int SLEEP_TIME = 4500;
	private final static String dir =
			System.getProperty("user.home") + File.separator + "studia" + File.separator + "niebezpiecznik" + File.separator;

	@Override
	public List<Article> parse() {
		List<String> links = getLinks();
		List<Article> articles = links.stream().map(this::parseLink)
				.filter(Objects::nonNull)
				.collect(Collectors.toList());

		articles.forEach(this::writeArticle);
		return articles;
	}

	private Article parseLink(String articleUrl) {
		try {
			Article article = new Article(articleUrl);
			Thread.sleep(SLEEP_TIME + (new Random()).nextInt(Integer.MAX_VALUE) % 4000);
			Document doc = JsoupConnector.connectThrowable(articleUrl, SLEEP_TIME);
			article.setTitle(doc.select(".title").select("a[rel=bookmark]").text());
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

	private List<String> getLinks() {
		List<String> links = new ArrayList<>();
		try {
			for (int pageNumber = 1; ; pageNumber++) {
				Document doc = JsoupConnector
						.connect(pageLink + pageNumber, (new Random()).nextInt(Integer.MAX_VALUE) % 10000 + SLEEP_TIME);
				doc.select(".post").forEach(post -> {
					String url = retrievePostUrl(post);
					if (url != null) {
						links.add(url);
					}
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
		try {
			return post.select(".title").select("h2").select("a").attr("href");
		} catch (Exception e) {
			return null;
		}
	}

}
