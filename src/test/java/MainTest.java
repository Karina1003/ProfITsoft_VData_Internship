import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

public class MainTest {

    //Task 1
    @Test
    void testConcatNameSurnameToXml() throws IOException {
        File fileIn = new File("./in/test/testPersonToParse.xml");
        File fileOut = new File("./out/test/testPersonAfterConcat.xml");
        Main.concatNameSurnameToXml(fileIn, fileOut);
        File fileExpected = new File("./in/test/expectedPerson.xml");
        Assertions.assertTrue(FileUtils.contentEquals(fileOut, fileExpected));
    }

    //Task 2
    @Test
    void testCalculateFinesByTypeJson() {
        Map<String,Double> mapToTest = new LinkedHashMap<>();
        try {
            mapToTest = Main.calculateFinesByTypeJson(new File("in/fines/2017.json"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        Map<String,Double> mapExpected = new LinkedHashMap<>();
        mapExpected.put("BREAKING SIGNALS", 600.00);
        mapExpected.put("SPEEDING", 350.00);
        Assertions.assertEquals(mapExpected, mapToTest);
    }

    @Test
    void testCalculateFinesByTypeNotJson() {
        Map<String,Double> mapToTest = new LinkedHashMap<>();
        try {
            mapToTest = Main.calculateFinesByTypeJson(new File("in/persons.xml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        Map<String,Double> mapExpected = new LinkedHashMap<>();
        Assertions.assertEquals(mapExpected, mapToTest);
    }

    @Test
    void testCreateXmlOfFines() {
        try {
            Map<String, Double> mapOfFines = Main.calculateFinesByTypeJson(new File("./in/test/testFinesToParse.json"));
            File fileOut = new File("./out/test/testTotalFines.xml");
            Main.createXmlOfFines(mapOfFines, fileOut);
            File fileExpected = new File("./in/test/expectedFines.xml");
            Assertions.assertTrue(FileUtils.contentEquals(fileOut, fileExpected));
        } catch (IOException | ParserConfigurationException | TransformerException e) {
            e.printStackTrace();
        }


    }

    
}
