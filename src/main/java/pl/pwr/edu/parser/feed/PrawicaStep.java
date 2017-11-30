package pl.pwr.edu.parser.feed;

import static java.util.stream.Collectors.toList;
import static pl.pwr.edu.parser.util.JsoupConnector.connect;

import com.google.common.collect.Maps;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;
import pl.pwr.edu.parser.domain.Article;
import pl.pwr.edu.parser.log.LoadingBar;
import pl.pwr.edu.parser.util.JsoupConnector;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

@Component
public class PrawicaStep implements Step {

	private static final String BASE_URL = "http://www.prawica.net";
	private Pattern dateRegex = Pattern.compile("@ (.*), k");
	private static String dir = System.getProperty("user.home") + "\\Desktop\\Prawica\\";
	private static int SLEEP_TIME = 5500;
	private int parsedArticles = 0;


	@Override
	public List<Article> parse() {
		List<String> links = getArticlesLinks();
		System.out.println("www.prawica.net links amount: " + links.size());
		LoadingBar loadingBar = new LoadingBar();
		loadingBar.setHorizontalMaxNumber(links.size());
		links.parallelStream()
				.map(this::parseLink)
				.filter(Objects::nonNull)
				.peek(this::writeArticle)
				.forEach(a -> loadingBar.indicateHorizontalLoading(parsedArticles));
		return new ArrayList<>();
	}

	private void writeArticle(Article article) {
		throw new NotImplementedException();
	}

	private List<String> getArticlesLinks() {
		Document doc = connect(BASE_URL, SLEEP_TIME);
		int pages = numberOfPages(doc);

		LoadingBar loadingBar = new LoadingBar();
		loadingBar.createVerticalLoadingBar(pages);

		return IntStream.range(0, pages)
				.parallel()
				.mapToObj(i -> getArticlesLinks(BASE_URL + "/?page=" + i))
				.peek(a -> loadingBar.indicateVerticalLoading())
				.flatMap(List::stream)
				.collect(toList());
	}

	private List<String> getArticlesLinks(String url) {
		if (!url.contains(BASE_URL)) {
			url = BASE_URL + url;
		}
		Document doc = JsoupConnector.connect(url, SLEEP_TIME);
		if (doc == null) {
			return new ArrayList<>();
		} else {
			return doc.select("#content").first()
					.select("article")
					.stream()
					.map(link -> link.select("a").first().attr("href"))
					.collect(toList());
		}
	}

	private Article parseLink(String articleUrl) {
		parsedArticles++;
		Article article = new Article(articleUrl);
		Document doc = JsoupConnector.connect(BASE_URL + articleUrl, SLEEP_TIME);
		article.setTitle(doc.select("#page-title").first().text().trim());
		parseArticleMetaData(article, doc);
		if (article.getMetadata() == null) {
			return null;
		}
		article.setBody(parseArticleBody(doc));

		return article;
	}

	private void parseArticleMetaData(Article article, Document doc) {
		HashMap<String, String> metaData = Maps.newHashMap();
		if (doc.select(".submitted-by") == null || doc.select(".submitted-by").isEmpty()) {
			return;
		}
		metaData.put("author", doc.select(".username").text().trim());
		metaData.put("date", getDate(doc.select(".submitted-by").first().text()));
		metaData.put("keywords", doc.select("meta[name=keywords]").attr("content").trim().replace("| prawica.net", ""));
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

	private String parseArticleBody(Document doc) {
		return doc.select(".field-item").first().select("p").text().trim();
	}

	private int numberOfPages(Document doc) {
		String[] pages = doc.select(".pager-current").first().text().trim().split(" z ");
		return new Integer(pages[1]);
	}

}
