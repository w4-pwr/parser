package pl.pwr.edu.parser.util;

import static com.google.common.base.Strings.isNullOrEmpty;

import java.io.File;
import pl.pwr.edu.parser.domain.Article;

public class FileHelper {

	public static File createArticleFile(Article article, String dir, String ext) {
		File directory = createArticleDirectory(dir, article);

		String fileName =
				directory.getAbsolutePath() + File.separator + article.hashCode() + (ext != null ? "." + ext : "");

		return new File(fileName);
	}

	private static File createArticleDirectory(String directory, Article article) {
		String author = article.getMetadata().get("author");
		String dirName = isNullOrEmpty(author) ? "Uncategorized" : author;

		File dir = new File(directory + File.separator + dirName);
		dir.mkdirs();

		return dir;
	}
}
