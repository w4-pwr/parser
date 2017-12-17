package pl.pwr.edu.parser.writer.path;

import pl.pwr.edu.parser.domain.Article;

/**
 * @author Jakub Pomykala on 12/1/17.
 * @project parser
 */
public final class AllInOneFilePathResolver implements PathResolver {

	public final static String OUTPUT_FILE_NAME = "output";

	@Override
	public String resolveRelativePath(Article article) {
		return "";
	}

	@Override
	public String resolveFileName(Article article) {
		return OUTPUT_FILE_NAME;
	}
}
