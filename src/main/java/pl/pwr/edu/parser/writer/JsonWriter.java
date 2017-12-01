package pl.pwr.edu.parser.writer;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
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
		String pathWithFileName = getPathWithFileName(article);
		objectMapper.writeValue(new File(pathWithFileName), article);
	}

	@Override
	public void setPathResolver(@NotNull PathResolver pathResolver) {
		this.pathResolver = pathResolver;
	}

	@NotNull
	private String getPathWithFileName(Article article) {
		ArticleAdapter adapter = ArticleAdapter.createAdapter(article);
		final String directoryPath = pathResolver.resolvePath(article);
		final String fileName = adapter.getTitleWithoutSpaces();
		final String JSON_EXTENSION = ".json";
		return BASE_WRITE_PATH +
				File.pathSeparator +
				directoryPath +
				fileName +
				JSON_EXTENSION;
	}
}
