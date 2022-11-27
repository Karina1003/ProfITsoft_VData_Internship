import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public class MainTest {
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

    
}
