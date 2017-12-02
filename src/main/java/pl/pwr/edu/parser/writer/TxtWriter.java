package pl.pwr.edu.parser.writer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import pl.pwr.edu.parser.domain.Article;
import pl.pwr.edu.parser.domain.ArticleAdapter;
import pl.pwr.edu.parser.writer.path.PathByArticleResolver;
import pl.pwr.edu.parser.writer.path.PathResolver;

/**
 * Created by Jakub on 10.04.2017.
 */
public final class TxtWriter implements ArticleWriter {

	private final String BASE_WRITE_PATH;
	private PathResolver pathResolver;

	private TxtWriter(String path) {
		this.BASE_WRITE_PATH = path;
		this.pathResolver = new PathByArticleResolver();
	}

	public static ArticleWriter getInstance(String path) {
		return new TxtWriter(path);
	}

	@Override
	public void write(Article article) throws IOException {
		writeAndGetPath(article);
	}

	@Override
	public Path writeAndGetPath(Article article) throws IOException {
		String pathWithFileName = getPathWithFileName(article);
		String textBody = Optional.ofNullable(article.getBody()).orElse("");
		Files.write(Paths.get(pathWithFileName), textBody.getBytes());
		return Paths.get(pathWithFileName);
	}

	@Override
	public void setPathResolver(@NotNull PathResolver strategy) {
		this.pathResolver = strategy;
	}

	@NotNull
	private String getPathWithFileName(Article article) {
		ArticleAdapter articleAdapter = ArticleAdapter.of(article);
		final String directoryName = pathResolver.resolvePath(article);
		final String fileName = articleAdapter.getCleanTitle();
		final String TXT_EXTENSION = ".txt";
		return BASE_WRITE_PATH +
				File.separator +
				directoryName +
				File.separator +
				fileName + TXT_EXTENSION;
	}
}
