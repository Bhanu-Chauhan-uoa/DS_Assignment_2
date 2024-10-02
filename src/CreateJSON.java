import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class CreateJSON {
    public static ArrayNode readWeatherData(String[] textFileName) {
        ObjectMapper mapper = new ObjectMapper();
        ArrayNode resultArray = mapper.createArrayNode();

        try {
            int i=0;
            while(i<textFileName.length) {
                ArrayNode jsonArray = mapper.createArrayNode();
                ObjectNode json = mapper.createObjectNode();
                String path = "WeatherData/" + textFileName[i];
                File myObj = new File(path);
                Scanner myReader = new Scanner(myObj);

                boolean isFirstTime = true;
                
                while (myReader.hasNextLine()) {
                    String currentString = myReader.nextLine();
                    int colonIndex = currentString.indexOf(":");

                    if (colonIndex != -1) {
                        String key = currentString.substring(0, colonIndex);
                        String value = currentString.substring(colonIndex + 1);

                        if ("id".equals(key)) {
                            if (isFirstTime) {
                                isFirstTime = false;
                            } else {
                                jsonArray.add(json);
                                json = mapper.createObjectNode();
                            }
                        }

                        if (value.matches("-?\\d+(\\.\\d+)?")) {
                            json.put(key, Double.parseDouble(value));
                        } else {
                            json.put(key, value);
                        }
                    } else {
                        break;
                    }
                }

                jsonArray.add(json);
                myReader.close();
                resultArray.add(jsonArray);
                i++;
            }
            return resultArray;

        } catch (FileNotFoundException e) {
            System.err.println("500 - Internal server error.");
            System.exit(1);
        }
        return null;
    }
}
