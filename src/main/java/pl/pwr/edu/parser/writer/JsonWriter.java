package pl.pwr.edu.parser.writer;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.jetbrains.annotations.NotNull;
import pl.pwr.edu.parser.domain.Article;
import pl.pwr.edu.parser.domain.ArticleAdapter;
import pl.pwr.edu.parser.writer.path.PathByArticleResolver;
import pl.pwr.edu.parser.writer.path.PathResolver;

/**
 * Created by Jakub on 10.04.2017.
 */
public final class JsonWriter implements ArticleWriter {

	private final String BASE_WRITE_PATH;
	private ObjectMapper objectMapper;
	private PathResolver pathResolver;

	private JsonWriter(String path) {
		this.BASE_WRITE_PATH = path;
		objectMapper = new ObjectMapper();
		pathResolver = new PathByArticleResolver();
	}

	public static ArticleWriter getInstance(String path) {
		return new JsonWriter(path);
	}

	@Override
	public void write(Article article) throws IOException {
		writeAndGetPath(article);
	}

	@Override
	public Path writeAndGetPath(Article article) throws IOException {
		Path path = getPath(article);
		Files.createDirectories(path);
		String fileName = getFileName(article);
		String pathWithFileName = path + File.separator + fileName;
		objectMapper.writeValue(new File(pathWithFileName), article);
		return Paths.get(pathWithFileName);
	}

	@Override
	public void setPathResolver(@NotNull PathResolver pathResolver) {
		this.pathResolver = pathResolver;
	}

	@NotNull
	private Path getPath(Article article) {
		final String directoryPath = pathResolver.resolvePath(article);
		return Paths.get(BASE_WRITE_PATH, File.separator, directoryPath);
	}

	@NotNull
	private String getFileName(Article article) {
		ArticleAdapter adapter = ArticleAdapter.of(article);
		final String fileName = adapter.getCleanTitle();
		final String JSON_EXTENSION = ".json";
		return File.separator +
				fileName +
				JSON_EXTENSION;
	}
}
