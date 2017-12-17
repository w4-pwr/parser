package pl.pwr.edu.parser.writer;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Optional;
import java.util.StringJoiner;
import org.jetbrains.annotations.NotNull;
import pl.pwr.edu.parser.domain.Article;
import pl.pwr.edu.parser.writer.path.PathByArticleResolver;
import pl.pwr.edu.parser.writer.path.PathResolver;

/**
 * Created by Jakub on 10.04.2017.
 */
public final class TxtWriter implements ArticleWriter {

	private final String BASE_WRITE_PATH;
	private PathResolver pathResolver;
	private Charset charset;

	private TxtWriter(String path) {
		this.BASE_WRITE_PATH = path;
		this.pathResolver = new PathByArticleResolver();
	}

	public static ArticleWriter getInstance(String path) {
		return new TxtWriter(path);
	}

	@Override
	public void write(Article article) throws IOException {
		String relativePath = pathResolver.resolveRelativePath(article);
		String absolutePath = BASE_WRITE_PATH + File.separator + relativePath;
		Files.createDirectories(Paths.get(absolutePath));
		String fileName = pathResolver.resolveFileName(article) + ".txt";
		String pathWithFileName = absolutePath + File.separator + fileName;

		String textToSave = getTextToSave(article);
		Path absolutePathToFile = Paths.get(pathWithFileName);
		if (Files.exists(absolutePathToFile)) {
			Files.write(absolutePathToFile, textToSave.getBytes(charset), StandardOpenOption.APPEND);
		} else {
			Files.write(absolutePathToFile, textToSave.getBytes(charset));
		}
	}

	private String getTextToSave(Article article) {
		Optional<Article> optionalArticle = Optional.ofNullable(article);
		String textBody = optionalArticle
				.map(Article::getBody)
				.orElse("");

		String author = optionalArticle
				.map(Article::getMetadata)
				.map(stringStringHashMap -> stringStringHashMap.get("author"))
				.orElse("");

		StringJoiner stringJoiner = new StringJoiner(",");
		stringJoiner.add(textBody);
		stringJoiner.add(author);
		return stringJoiner.toString();
	}

	@Override
	public void setPathResolver(@NotNull PathResolver strategy) {
		this.pathResolver = strategy;
	}

	@Override
	public void setCharset(Charset charset) {
		this.charset = charset;
	}

}
