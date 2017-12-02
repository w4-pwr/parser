package pl.pwr.edu.parser.writer;

import java.io.File;
import java.nio.file.Path;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.pwr.edu.parser.domain.Article;

/**
 * @author Jakub Pomykala on 12/1/17.
 * @project parser
 */
class JsonWriterTest {

	private ArticleWriter writer;

	@BeforeEach
	void setUp() {
		String userDirectory = System.getProperty("user.dir") + File.separator + "test_output";
		writer = JsonWriter.getInstance(userDirectory);
	}

	@Test
	void writeArticleWithAllData_writeSuccess() throws Exception {
		//given
		Article article = Article.builder()
				.title("Czy masz raka?")
				.body("tak")
				.source("http://wykop.pl")
				.build();

		//when
		Path path = writer.writeAndGetPath(article);

		//then
		File savedFile = new File(path.toUri());
		Assertions.assertThat(savedFile).exists();
		Assertions.assertThat(savedFile).hasName("czy-masz-raka.json");
		Assertions.assertThat(savedFile).isFile();
		Assertions.assertThat(savedFile).isAbsolute();
		Assertions.assertThat(savedFile.length()).isNotZero();
	}
}