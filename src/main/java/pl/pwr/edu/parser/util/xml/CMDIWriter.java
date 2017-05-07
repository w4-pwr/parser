package pl.pwr.edu.parser.util.xml;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import pl.pwr.edu.parser.model.Article;
import pl.pwr.edu.parser.util.FileHelper;

public class CMDIWriter {

    public static void writeArticleToFile(Article article, String path) {

        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = dbf.newDocumentBuilder();
            Document doc = builder.newDocument();

            Element root = doc.createElement("ResourceBasicInformation");
            doc.appendChild(root);
            root.setAttribute("xmlns", "http://www.clarin.eu/cmd/");
            root.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
            root.setAttribute("CMDVersion", "1.1");
            root.setAttribute("xsi:schemaLocation", "http://www.clarin.eu/cmd/ ResourceBasicInformation.xsd ");


            Element bibliography = doc.createElement("TextBibliographic");
            root.appendChild(bibliography);

            createChildTextNode(doc, bibliography, "Title", article.getTitle(), null);
            createChildTextNode(doc, bibliography, "PublicationDate", article.getMetadata().get("date"), null);
            createChildTextNode(doc, bibliography, "PublicationPlace", "unknown", null);

            Element authors = doc.createElement("Authors");
            bibliography.appendChild(authors);

            Arrays.asList(
                    article.getMetadata()
                        .getOrDefault("author", "")
                            .split(",")
            ).forEach(author -> createAuthorNode(doc, authors, author));

            Element recourceContent = doc.createElement("ResourceContent");
            root.appendChild(recourceContent);

            createChildTextNode(doc, recourceContent, "Subject", article.getMetadata().get("category"), "pol");
            createChildTextNode(doc, recourceContent, "originalSource", article.getSource(), "und");

            Arrays.stream(
                    article.getMetadata()
                        .getOrDefault("keywords", "")
                            .split(","))
                    .map(String::trim)
                    .filter(k -> !k.isEmpty())
                    .forEach(k -> createChildTextNode(doc, recourceContent, "keyword", k, "pol"));

            Element descriptions = doc.createElement("Descriptions");
            recourceContent.appendChild(descriptions);

            Element description = createChildTextNode(doc, descriptions, "Description",
                    article.getMetadata().get("description"), "pol");
            description.setAttribute("type", "short");

            prettyPrint(path, doc, article);
        } catch (ParserConfigurationException | TransformerException | IOException e) {
            throw new RuntimeException("CMDI");
        }
    }

    private static Element createChildTextNode(Document doc, Node parent, String nodeName, String content, String lang) {
        Element newNode = doc.createElement(nodeName);
        parent.appendChild(newNode);
        newNode.setTextContent(content);

        if (lang != null)
            newNode.setAttribute("xml:lang", lang);

        return newNode;
    }

    private static Element createAuthorNode(Document doc, Node parent, String content) {
        Element author = doc.createElement("author");
        parent.appendChild(author);
        createChildTextNode(doc, author, "lastName", content, null);

        return author;
    }

    private static void prettyPrint(String path, Document xml, Article article) throws TransformerException, IOException {
        Transformer tf = TransformerFactory.newInstance().newTransformer();
        tf.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        tf.setOutputProperty(OutputKeys.INDENT, "yes");

        FileWriter fw = new FileWriter(FileHelper.createArticleFile(article, path, "cmdi"));
        tf.transform(new DOMSource(xml), new StreamResult(fw));

        File articleBody = FileHelper.createArticleFile(article, path, null);
        Files.write(articleBody.toPath(), article.getBody().getBytes(), StandardOpenOption.CREATE);
    }

}