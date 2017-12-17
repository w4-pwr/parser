package pl.pwr.edu.parser.writer;

import java.nio.charset.Charset;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import pl.pwr.edu.parser.domain.Article;
import pl.pwr.edu.parser.writer.path.MongoPathResolver;
import pl.pwr.edu.parser.writer.path.PathResolver;

/**
 * @author Jakub Pomykala on 12/1/17.
 * @project parser
 */
@Component
public final class MongoDBWriter implements ArticleWriter {

	@Autowired
	private MongoTemplate mongoTemplate;
	private PathResolver pathResolver = new MongoPathResolver();

	@Override
	public void write(Article article) {
		Assert.notNull(article, "Article cannot be null");
		String collectionName = pathResolver.resolveRelativePath(article);
		mongoTemplate.insert(article, collectionName);
	}

	@Override
	public void setPathResolver(PathResolver strategy) {
		String exceptionMessage = String.format(
				"Writing to Mongo database does not support resolving paths. To edit collection name look for %s class",
				MongoPathResolver.class.getName());
		throw new UnsupportedOperationException(exceptionMessage);
	}

	@Override
	public void setCharset(Charset charset) {
		throw new UnsupportedOperationException("Changing charset is not supported");
	}
}
