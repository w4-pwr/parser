package pl.pwr.edu.parser.shell;

import java.io.File;
import java.nio.charset.Charset;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;
import pl.pwr.edu.parser.feed.chain.ParserChain;
import pl.pwr.edu.parser.shell.arguments.OutputEnum;
import pl.pwr.edu.parser.shell.arguments.PathResolveEnum;
import pl.pwr.edu.parser.shell.arguments.provider.CharsetValueProvider;
import pl.pwr.edu.parser.shell.arguments.provider.OutputValueProvider;
import pl.pwr.edu.parser.shell.arguments.provider.PathResolveValueProvider;
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

	@Autowired
	private MongoDBWriter mongoDBWriter;

	private final String DEFAULT_OUTPUT = "TXT";
	private final String DEFAULT_PATH_RESOLVE = "SOURCE";
	private final String DEFAULT_ENCODING = "UTF-8";
	private String userDirectory = System.getProperty("user.dir") + File.separator + "output";

	@ShellMethod("Uruchom parser z domyślne parametrami")
	public void start(
			@ShellOption(defaultValue = DEFAULT_OUTPUT, valueProvider = OutputValueProvider.class) OutputEnum output,
			@ShellOption(defaultValue = DEFAULT_PATH_RESOLVE, valueProvider = PathResolveValueProvider.class) PathResolveEnum resolve,
			@ShellOption(defaultValue = DEFAULT_ENCODING, valueProvider = CharsetValueProvider.class) String encoding
	) {
		PathResolver pathResolver = lookupForPathResolver(resolve);
		ArticleWriter articleWriter = lookupForArticleWriter(output);
		Charset charset = Charset.forName(encoding);
		try {
			articleWriter.setPathResolver(pathResolver);
			articleWriter.setCharset(charset);
		} catch (UnsupportedOperationException ignored) {
		}

		parserChain.invoke(articleWriter);
	}

	private PathResolver lookupForPathResolver(PathResolveEnum option) {
		switch (option) {
			case ALL:
				return new AllInOneFilePathResolver();
			case ARTICLE:
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
//			case MONGO:
//				return mongoDBWriter;
			default:
				System.err.printf("Output option %s not match to any cases. Using %s ",
						outputOption.name(),
						JsonWriter.class.getName());
				return JsonWriter.getInstance(userDirectory);
		}
	}

}
