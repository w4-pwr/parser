package pl.pwr.edu.parser.domain;

import lombok.NoArgsConstructor;
import lombok.ToString;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * @author Jakub Pomykala on 12/6/17.
 * @project parser
 */

@Document
@NoArgsConstructor
@ToString(of = {"data"})
public class Foo {

	@Id
	private ObjectId id;

	private String data;

	private Foo(String data) {
		this.data = data;
	}

	public static Foo create(String data) {
		return new Foo(data);
	}
}
