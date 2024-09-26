import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class CreateJSON {
    public static ArrayNode readWeatherData(String[] filename) {
        ObjectMapper mapper = new ObjectMapper();
        ArrayNode mainArray = mapper.createArrayNode();

        try {
            int i=0;
            while(i<filename.length){
                boolean firstTime = true;
                ObjectNode json = mapper.createObjectNode();
                ArrayNode jsonArray = mapper.createArrayNode();
                String path = "WeatherData/" + filename[i];
                File obj = new File(path);
                Scanner sc = new Scanner(obj);

                while (sc.hasNextLine()) {
                    String currentString = sc.nextLine();
                    int colonIndex = currentString.indexOf(":");

                    if (colonIndex != -1) {
                        String key = currentString.substring(0, colonIndex);
                        String value = currentString.substring(colonIndex + 1);

                        if ("id".equals(key)) {
                            if (firstTime) {
                                firstTime = false;
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
                sc.close();
                mainArray.add(jsonArray);
            }
            return mainArray;
        } catch (FileNotFoundException e) {
            System.err.println("500 Internal Server Error");
            System.exit(1);
        }
        return null;
    }
}
