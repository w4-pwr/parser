package pl.pwr.edu.parser.shell;

import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

/**
 * @author Jakub Pomykala on 11/30/17.
 * @project parser
 */
@ShellComponent
public class WritingOptionCommands {

	@ShellMethod("Uruchom parser z domyślne parametrami")
	public void start(
			@ShellOption(defaultValue = "json") String output,
			@ShellOption(defaultValue = "1") int option
	) {
		System.out.println(output);
		System.out.println(option);
	}

}
