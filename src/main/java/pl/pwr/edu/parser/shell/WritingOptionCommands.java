package pl.pwr.edu.parser.shell;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;
import pl.pwr.edu.parser.feed.chain.ParserChain;
import pl.pwr.edu.parser.shell.arguments.OutputEnum;
import pl.pwr.edu.parser.shell.arguments.PathResolveEnum;
import pl.pwr.edu.parser.writer.ArticleWriter;
import pl.pwr.edu.parser.writer.JsonWriter;
import pl.pwr.edu.parser.writer.MongoDBWriter;
import pl.pwr.edu.parser.writer.TxtWriter;
import pl.pwr.edu.parser.writer.path.AllInOneFilePathResolver;
import pl.pwr.edu.parser.writer.path.PathByArticleResolver;
import pl.pwr.edu.parser.writer.path.PathBySourceResolver;
import pl.pwr.edu.parser.writer.path.PathResolver;

/**
 * @author Jakub Pomykala on 11/30/17.
 */
@ShellComponent
public class WritingOptionCommands {

	@Autowired
	private ParserChain parserChain;

	private final String DEFAULT_OUTPUT = "MONGO";
	@Autowired
	private MongoDBWriter mongoDBWriter;
	private final String DEFAULT_PATH_RESOLVE = "ARTIST";
	private String userDirectory = System.getProperty("user.dir");

	@ShellMethod("Uruchom parser z domyślne parametrami")
	public void start(
			@ShellOption(defaultValue = DEFAULT_OUTPUT) OutputEnum outputOption,
			@ShellOption(defaultValue = DEFAULT_PATH_RESOLVE) PathResolveEnum resolveBy
	) {
		PathResolver pathResolver = lookupForPathResolver(resolveBy);
		ArticleWriter articleWriter = lookupForArticleWriter(outputOption);
		try {
			articleWriter.setPathResolver(pathResolver);
		} catch (UnsupportedOperationException uoe) {

		}

		parserChain.invoke(articleWriter);
	}

	private PathResolver lookupForPathResolver(PathResolveEnum option) {
		switch (option) {
			case ALL:
				return new AllInOneFilePathResolver();
			case ARTIST:
				return new PathByArticleResolver();
			case SOURCE:
				return new PathBySourceResolver();
			default:
				System.err.printf("Path resolving option %s not match to any cases. Using %s ",
						option.name(),
						PathByArticleResolver.class.getName());
				return new PathByArticleResolver();
		}
	}


	private ArticleWriter lookupForArticleWriter(OutputEnum outputOption) {
		switch (outputOption) {
			case TXT:
				return TxtWriter.getInstance(userDirectory);
			case JSON:
				return JsonWriter.getInstance(userDirectory);

			case MONGO:
				return mongoDBWriter;
			default:
				System.err.printf("Output option %s not match to any cases. Using %s ",
						outputOption.name(),
						JsonWriter.class.getName());
				return JsonWriter.getInstance(userDirectory);
		}
	}

}
