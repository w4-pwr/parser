package pl.pwr.edu.parser.writer.path;

import java.util.Optional;
import java.util.UUID;
import org.jetbrains.annotations.NotNull;
import pl.pwr.edu.parser.domain.Article;
import pl.pwr.edu.parser.util.StringUtils;

/**
 * @author Jakub Pomykala on 12/1/17.
 * @project parser
 */
public final class PathBySourceResolver implements PathResolver {

	@Override
	public String resolvePath(Article article) {
		String sourceName = Optional.ofNullable(article)
				.map(Article::getSource)
				.orElseGet(this::getDefaultDirectoryName);
		return StringUtils.replaceWhitespacesWithDash(sourceName);
	}

	@NotNull
	private String getDefaultDirectoryName() {
		String noSourcePrefix = "no-source-";
		String randomUUID = UUID.randomUUID().toString();
		return noSourcePrefix + randomUUID;
	}

}
