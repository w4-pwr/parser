package pl.pwr.edu.parser.shell;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;
import pl.pwr.edu.parser.feed.chain.ParserChain;

/**
 * @author Jakub Pomykala on 11/30/17.
 */
@ShellComponent
public class WritingOptionCommands {

	@Autowired
	private ParserChain parserChain;

	@ShellMethod("Uruchom parser z domyślne parametrami")
	public void start(
			@ShellOption(defaultValue = "json") String output,
			@ShellOption(defaultValue = "1") int option
	) {

		System.err.println("Options ignored, not implemented");
		parserChain.invoke();
	}

}
