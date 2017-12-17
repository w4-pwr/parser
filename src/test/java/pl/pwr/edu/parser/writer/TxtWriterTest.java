package pl.pwr.edu.parser.writer;

import static pl.pwr.edu.parser.writer.path.AllInOneFilePathResolver.OUTPUT_FILE_NAME;

import java.io.File;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.pwr.edu.parser.domain.Article;
import pl.pwr.edu.parser.writer.path.AllInOneFilePathResolver;

/**
 * @author Jakub Pomykala on 12/1/17.
 * @project parser
 */
class TxtWriterTest {

	private ArticleWriter writer;
	private String userDirectory;

	@BeforeEach
	void setUp() {
		userDirectory = System.getProperty("user.dir") + File.separator + "test_output";
		writer = TxtWriter.getInstance(userDirectory);
		writer.setPathResolver(new AllInOneFilePathResolver());
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
		writer.write(article);

		//then
		String pathToSavedFile = userDirectory + File.separator + OUTPUT_FILE_NAME + ".txt";
		File savedFile = new File(pathToSavedFile);
		Assertions.assertThat(savedFile).exists();
		Assertions.assertThat(savedFile).hasName(OUTPUT_FILE_NAME + ".txt");
		Assertions.assertThat(savedFile).isFile();
		Assertions.assertThat(savedFile).isAbsolute();
		Assertions.assertThat(savedFile.length()).isNotZero();
	}
}