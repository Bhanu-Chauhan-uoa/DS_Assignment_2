import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class CreateJSON {

    public static ArrayNode readWeatherData(String[] textFileNames) {
        ObjectMapper mapper = new ObjectMapper();
        ArrayNode resultArray = mapper.createArrayNode();

        for (String textFileName : textFileNames) {
            ArrayNode jsonArray = mapper.createArrayNode();
            ObjectNode json = mapper.createObjectNode();
            String path = "WeatherData/" + textFileName;

            try (Scanner myReader = new Scanner(new File(path))) {
                boolean isFirstEntry = true;

                while (myReader.hasNextLine()) {
                    String currentLine = myReader.nextLine();
                    int colonIndex = currentLine.indexOf(":");

                    if (colonIndex != -1) {
                        String key = currentLine.substring(0, colonIndex).trim();
                        String value = currentLine.substring(colonIndex + 1).trim();

                        // Check for the first entry to manage JSON object addition
                        if ("id".equals(key)) {
                            if (!isFirstEntry) {
                                jsonArray.add(json);
                                json = mapper.createObjectNode();
                            }
                            isFirstEntry = false; // Mark that the first entry has been processed
                        }

                        // Add the key-value pair to the JSON object
                        if (isNumeric(value)) {
                            json.put(key, Double.parseDouble(value));
                        } else {
                            json.put(key, value);
                        }
                    } else {
                        break; // Exit loop if the line doesn't contain a valid key-value pair
                    }
                }

                // Add the last JSON object to the array if it exists
                if (!isFirstEntry) {
                    jsonArray.add(json);
                }
                resultArray.add(jsonArray);
                
            } catch (FileNotFoundException e) {
                System.err.println("500 - Internal server error: File not found: " + path);
                System.exit(1);
            }
        }
        return resultArray;
    }

    private static boolean isNumeric(String str) {
        // Check if the string can be parsed as a number
        return str != null && str.matches("-?\\d+(\\.\\d+)?");
    }
}