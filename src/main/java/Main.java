import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.Writer;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args) {
        //Task 1
        try {
            concatNameSurnameToXml(new File("./in/persons.xml"),
                                   new File ("./out/persons_new.xml"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Task 2
        try {
            Map<String, Double> mapOfFines = calculateFinesByTypeJson(new File("./in/fines/"));
            createXmlOfFines(mapOfFines, new File("./out/totalFines.xml"));
        } catch (IOException | ParserConfigurationException | TransformerException e) {
            e.printStackTrace();
        }
    }


    //Task 1
    /**
     * This method reads file line by line, concatenates name and surname preserving the structure
     * of original document and writes into new document
     * @param fileIn - file to read
     * @param fileOut - file to write
     * @throws IOException - an exception to be handled
     */
    public static void concatNameSurnameToXml (File fileIn, File fileOut) throws IOException {
        try(InputStream is = new FileInputStream(fileIn);
            Scanner scanner = new Scanner(is, StandardCharsets.UTF_8).useDelimiter("(?<=>)");
            Writer writer = new FileWriter(fileOut)){
            //pattern searches for a 'name = "Name"' with or without whitespaces between words and '='
            Pattern patternName = Pattern.compile("(\\Wname\\s*=\\s*\"\\S+)(\")");
            //pattern searches for a 'surname = "Surname"' to delete the expression
            Pattern patternSurname = Pattern.compile("(\\Wsurname\\s*=\\s*\")(\\S+)(\")");
            while (scanner.hasNext()) {
                String row = scanner.next();
                Matcher matcherName = patternName.matcher(row);
                Matcher matcherSurname = patternSurname.matcher(row);
                if ( matcherName.find() && matcherSurname.find() ) {
                    String surname = matcherSurname.group(2);
                    row = row
                            .replaceAll(matcherName.group(1), matcherName.group(1)+" "+surname)
                            .replaceAll(matcherSurname.group(1)+matcherSurname.group(2)+matcherSurname.group(3), "");
                }
                writer.write(row);
            }
        }
    }


    //Task 2
    /**
     * This method checks a directory of JSON files or a single JSON file and
     * sums violations from all files by type
     * @param jsonToParse - a file or a directory to look through and parse
     * @return fineAmountMap - a map of calculated and sorted in reverse order violations
     * @throws IOException - an exception that can be thrown by invalid or missing file "jsonToParse"
     */
    public static Map<String,Double> calculateFinesByTypeJson(File jsonToParse) throws IOException {
        JsonFactory jsonFactory = new JsonFactory();
        Map<String,Double> fineAmountMap = new HashMap<>();
        File[] fileList;

        if (jsonToParse.isDirectory()) {
            fileList = jsonToParse.listFiles();
        } else {
            fileList = new File[]{jsonToParse};
        }
        if (fileList != null) {
            for (File fileJson : fileList) {
                if (fileJson != null && fileJson.length() != 0 && fileJson.getName().endsWith(".json")) {
                    try (JsonParser jsonParser = jsonFactory.createParser(fileJson)) {
                        while (jsonParser.nextToken() != JsonToken.END_ARRAY) {
                            String name = jsonParser.getCurrentName();
                            String type;
                            if ("type".equals(name)) {
                                jsonParser.nextToken();
                                type = jsonParser.getValueAsString();//.replaceAll(" ", "_");
                                jsonParser.nextToken();
                                name = jsonParser.getCurrentName();
                                if ("fine_amount".equals(name)) {
                                    jsonParser.nextToken();
                                    if (!fineAmountMap.containsKey(type)) {
                                        fineAmountMap.put(type, jsonParser.getDoubleValue());
                                    } else {
                                        fineAmountMap.put(type, fineAmountMap.get(type) + jsonParser.getDoubleValue());
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return fineAmountMap.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .collect(Collectors.toMap(x -> x.getKey(), x -> x.getValue(),(n, m)->n, LinkedHashMap<String,Double>::new));
    }

    /**
     * This method creates an XML-file of fines based on data in the Map parameter
     * @param mapOfFines - map of calculated fines sorted in descending order
     * @param fileXml - XML file to be created
     * @throws ParserConfigurationException - an exception that can be thrown
     * by newDocumentBuilder() method of DocumentBuilderFactory class
     * @throws TransformerException - an exception that can be thrown by writeXml() method
     */
    public static void createXmlOfFines (Map<String, Double> mapOfFines, File fileXml) throws ParserConfigurationException, TransformerException {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.newDocument();
        // root element
        Element rootElement = doc.createElement("violations");
        doc.appendChild(rootElement);
        // add xml elements
        if (mapOfFines != null) {
            Set<Map.Entry<String, Double>> fineEntrySet = mapOfFines.entrySet();
            for (Map.Entry<String, Double> fineEntry : fineEntrySet) {
                Element violation = doc.createElement("violation");
                rootElement.appendChild(violation);
                Element type = doc.createElement("type");
                type.setTextContent(fineEntry.getKey());
                violation.appendChild(type);
                Element totalAmount = doc.createElement("total_amount");
                totalAmount.setTextContent(fineEntry.getValue().toString());
                violation.appendChild(totalAmount);
            }
        }
        writeXml(doc, fileXml);
    }

    /**
     * This method writes an XML file of structure defined in doc parameter to a given file
     * @param doc - represents an XML document of a certain structure
     * @param fileXml - XML-file to be created or updated
     * @throws TransformerException - an exception that can be thrown
     * by transform() method of Transformer class
     */
    private static void writeXml(Document doc, File fileXml) throws TransformerException {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");

        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(fileXml);

        transformer.transform(source, result);
    }
}
