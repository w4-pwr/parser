package pl.pwr.edu.parser.writers;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import pl.pwr.edu.parser.domain.Article;

/**
 * Created by Jakub on 10.04.2017.
 */
public final class JsonWriter implements ArticleWriter {

	private final String WRITE_PATH = System.getProperty("user.home");
	private ObjectMapper objectMapper;

	private JsonWriter() {
		objectMapper = new ObjectMapper();
	}

	@Override
	public void write(Article article) throws IOException {
		ArticleAdapter adapter = ArticleAdapter.createAdapter(article);
		String pathWithFileName = WRITE_PATH + File.pathSeparator + adapter.getTitleWithoutSpaces() + ".json";
		objectMapper.writeValue(new File(pathWithFileName), article);
	}
}
