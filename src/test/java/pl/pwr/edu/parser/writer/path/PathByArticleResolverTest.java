package pl.pwr.edu.parser.writer.path;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.pwr.edu.parser.domain.Article;

/**
 * @author Jakub Pomykala on 12/1/17.
 * @project parser
 */
class PathByArticleResolverTest {

	private PathByArticleResolver pathByArticleResolver;

	@BeforeEach
	void setUp() {
		pathByArticleResolver = new PathByArticleResolver();
	}

	@Test
	void resolvePath_articleHasTitle() {
		//given
		Article article = Article.builder().title("fajny tytuł").build();

		//when
		String resolvedPath = pathByArticleResolver.resolvePath(article);

		//then
		Assertions.assertThat(resolvedPath).isEqualTo("fajny-tytu");
	}

	@Test
	void resolvePath_articleIsNull() {
		//given
		Article article = null;

		//when
		String resolvedPath = pathByArticleResolver.resolvePath(article);

		//then
		Assertions.assertThat(resolvedPath).startsWith("no-title-");
	}
}