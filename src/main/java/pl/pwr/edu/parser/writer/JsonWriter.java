package pl.pwr.edu.parser.writer;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import org.jetbrains.annotations.NotNull;
import pl.pwr.edu.parser.domain.Article;
import pl.pwr.edu.parser.writer.path.PathByArticleResolver;
import pl.pwr.edu.parser.writer.path.PathResolver;

/**
 * Created by Jakub on 10.04.2017.
 */
public final class JsonWriter implements ArticleWriter {

	private final String BASE_WRITE_PATH;
	private ObjectMapper objectMapper;
	private PathResolver pathResolver;
	private Charset charset;

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
		String relativePath = pathResolver.resolveRelativePath(article);
		String absolutePath = BASE_WRITE_PATH + File.separator + relativePath;
		Files.createDirectories(Paths.get(absolutePath));
		String fileName = pathResolver.resolveFileName(article) + ".json";
		String pathWithFileName = absolutePath + File.separator + fileName;

		String newLine = "\n";
		String text = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(article) + newLine;
		Path absolutePathToFile = Paths.get(pathWithFileName);
		if (Files.exists(absolutePathToFile)) {
			Files.write(absolutePathToFile, text.getBytes(charset), StandardOpenOption.APPEND);
		} else {
			Files.write(absolutePathToFile, text.getBytes(charset));
		}

	}

	@Override
	public void setPathResolver(@NotNull PathResolver pathResolver) {
		this.pathResolver = pathResolver;
	}

	@Override
	public void setCharset(Charset charset) {
		this.charset = charset;
	}

}
