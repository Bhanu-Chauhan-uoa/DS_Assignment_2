import java.io.*;
import java.net.Socket;
import java.util.Map;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

public class ContentServer {
    private static LamportClock lampClock = new LamportClock();
    public static void main(String[] args) {
        try {
            if (args.length < 2) {
                System.err.println("Please Try Again!\nMissing required args");
                System.exit(1);
            }

            String[] dataFiles = Utils.handleCSFileInputs(args);
            String uniqueCSId = args[0];
            Object[] serverAndPort = Utils.serverAndPort(args[1]);
            String serverName = (String) serverAndPort[0];
            int portNumber = (int)serverAndPort[1];
            ArrayNode arr = CreateJSON.readWeatherData(dataFiles);
            File JSONBackupFile = new File("CS_Backup/backup" + uniqueCSId +".json");

            if(!JSONBackupFile.exists()) {
                if (JSONBackupFile.createNewFile()) {
                    long initialSize = 1024 * 1024; // 1 MB
                    RandomAccessFile raf = new RandomAccessFile(JSONBackupFile, "rw");
                    raf.setLength(initialSize);
                    raf.close();
                    Utils.updateContentServerBackUpFile(arr, uniqueCSId);
                }
            }else{
                arr = (ArrayNode) new ObjectMapper().readTree(JSONBackupFile).get("data");
            }

            ObjectMapper mapper = new ObjectMapper();

            for (; !arr.isEmpty(); ) {
                boolean resStatus = false;
            
                for (; !resStatus; ) {
                    try (Socket socket = new Socket(serverName, portNumber)) {
                        // Prepare the GET request
                        String lamportClockHeader = "Lamport-Clock: " + lampClock.getCurrTime() + "\r\n";
                        String contentServerText = "CS-ID: " + uniqueCSId + "\r\n\r\n";
                        String request = "GET / HTTP/1.1\r\n" + lamportClockHeader + contentServerText;
            
                        // Send GET request
                        OutputStream outputStream = socket.getOutputStream();
                        outputStream.write(request.getBytes());
                        outputStream.flush();
            
                        // Handle the response for GET
                        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                        Object[] headerAndBody = Utils.handleHeaderAndBody(reader);
                        Map<String, String> headersMap = (Map<String, String>) headerAndBody[0];
            
                        // Update the Lamport clock
                        lampClock.updateTime(Integer.parseInt(headersMap.get("Lamport-Clock")));
            
                        // Close resources for GET
                        reader.close();
                        outputStream.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.err.println("Your Server is not active. Please try again.");
                        System.exit(1);
                    }
            
                    try (Socket socket = new Socket(serverName, portNumber)) {
                        // Prepare the PUT request
                        String mainArrString = mapper.writeValueAsString(arr.get(0));
                        String contentServerText = "CS-ID: " + uniqueCSId + "\r\n";
                        String contentType = "Content-Type: application/json\r\n";
                        String lamportClockHeader = "Lamport-Clock: " + lampClock.getCurrTime() + "\r\n";
                        String contentLength = "Content-Length: " + mainArrString.length() + "\r\n\r\n";
                        String request = "PUT /weather.json HTTP/1.1\r\n" +
                                "User-Agent: ContentServer/\r\n" + contentServerText + contentType +
                                lamportClockHeader + contentLength + mainArrString;
            
                        // Send PUT request
                        OutputStream outputStream = socket.getOutputStream();
                        outputStream.write(request.getBytes());
                        outputStream.flush();
            
                        // Handle the response for PUT
                        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                        Object[] headerAndBody = Utils.handleHeaderAndBody(reader);
                        Map<String, String> headersMap = (Map<String, String>) headerAndBody[0];
            
                        // Update Lamport clock based on the response
                        lampClock.updateTime(Integer.parseInt(headersMap.get("Lamport-Clock")));
            
                        // Check the response status
                        String method = headersMap.get("method");
                        if ("HTTP/1.1 200 OK".equals(method) || 
                            "HTTP/1.1 201 - HTTP_CREATED".equals(method) || 
                            "HTTP/1.1 204 No Content".equals(method)) {
                            
                            System.out.println(method);
                            resStatus = true;
                            arr.remove(0);  // Remove the processed entry
            
                            // Handle backup file after successful update
                            if (arr.isEmpty()) {
                                JSONBackupFile.delete();
                            } else {
                                Utils.updateContentServerBackUpFile(arr, uniqueCSId);
                            }
                        } else {
                            System.out.println("Sending data again...");
                        }
            
                        // Close resources for PUT
                        reader.close();
                        outputStream.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.err.println("Your Server is not active. Please try again.");
                        System.exit(1);
                    }
            
                    // Wait before retrying
                    try {
                        Thread.sleep(27000);
                    } catch (InterruptedException e) {
                        System.err.println("Something went wrong!!!");
                        System.exit(1);
                    }
                }
            }            

        }
        catch(Exception e) {
            System.err.println("Wrong port number or domain name entered");
            System.exit(1);
        }
    }
}