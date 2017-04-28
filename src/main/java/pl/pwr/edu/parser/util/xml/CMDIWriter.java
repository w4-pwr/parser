package pl.pwr.edu.parser.util.xml;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import pl.pwr.edu.parser.model.Article;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Arrays;

public class CMDIWriter {

    public static void writeArticleToFile(String path, Article article)
            throws ParserConfigurationException, TransformerException {

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
                        .get("author")
                        .split(",")
        ).forEach(author -> createAuthorNode(doc, authors, author));

        Element recourceContent = doc.createElement("ResourceContent");
        root.appendChild(recourceContent);

        createChildTextNode(doc, recourceContent, "Subject", article.getMetadata().get("category"), "pol");
        createChildTextNode(doc, recourceContent, "originalSource", article.getSource(), "und");

        Arrays.asList(
                article.getMetadata()
                        .get("keywords")
                        .split(",")
        ).forEach(keyword -> createChildTextNode(doc, recourceContent, "keyword", keyword, "pol"));

        Element descriptions = doc.createElement("Descriptions");
        recourceContent.appendChild(descriptions);

        Element description = createChildTextNode(doc, descriptions, "Description",
                article.getMetadata().get("description"), "pol");
        description.setAttribute("type", "short");

        prettyPrint(doc);
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

    private static void prettyPrint(Document xml) throws TransformerException {
        Transformer tf = TransformerFactory.newInstance().newTransformer();
        tf.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        tf.setOutputProperty(OutputKeys.INDENT, "yes");
        Writer out = new StringWriter();
        tf.transform(new DOMSource(xml), new StreamResult(out));
    }

}