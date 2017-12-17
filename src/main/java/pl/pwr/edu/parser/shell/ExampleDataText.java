package pl.pwr.edu.parser.shell;

import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import pl.pwr.edu.parser.domain.Foo;

/**
 * @author Jakub Pomykala on 12/6/17.
 * @project parser
 */
@ShellComponent
public class ExampleDataText {

	@Autowired
	private MongoTemplate mongoTemplate;

	@ShellMethod("Test zapisywania")
	public void mongotest() {
		String collectionName = "test";

		IntStream.range(0, 1000)
				.peek(f -> this.sleep())
				.mapToObj(f -> this.createRandom())
				.peek(f -> System.out.printf("Writing %s\n", f.toString()))
				.forEach(f -> mongoTemplate.insert(f, collectionName));
	}

	private void sleep() {
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private Foo createRandom() {
		int randomInt = ThreadLocalRandom.current().nextInt();
		return Foo.create("Test-Unit-" + randomInt);
	}
}
