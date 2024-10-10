import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;


import java.io.*;
import java.net.Socket;
import java.util.Map;

public class ContentServer {

    private static LamportClock clock = new LamportClock();

    public static void main(String[] args) {
        try {
            if (args.length < 2) {
                System.err.println("Missing Unique Content Server ID.");
                System.err.println("OR");
                System.err.println("Missing Server Name and Port Number.");
                System.err.println("OR");
                System.err.println("Text Files which contains data is missing.");
                System.exit(1);
            }

            String uniqueContentServerId = args[0];
            Object[] serverAndPort = Utils.handleServerNameAndPort(args[1]);
            String[] allTextFiles = Utils.handleContentServerTextFileInputs(args);

            String serverName = (String) serverAndPort[0];
            int portNumber = (int) serverAndPort[1];

            ArrayNode arr = CreateJSON.readWeatherData(allTextFiles);

            File BackupJsonFile = new File("Content_server_backup_files/backup" + uniqueContentServerId +".json");

            if(!BackupJsonFile.exists()) {
                if (BackupJsonFile.createNewFile()) {
                    long initialSize = 1024 * 1024; // 1 MB
                    RandomAccessFile raf = new RandomAccessFile(BackupJsonFile, "rw");
                    raf.setLength(initialSize);
                    raf.close();
                    Utils.updateContentServerBackUpFile(arr, uniqueContentServerId);
                }
            }else{
                arr = (ArrayNode) new ObjectMapper().readTree(BackupJsonFile).get("data");
            }

            ObjectMapper mapper = new ObjectMapper();

            while (arr.size() > 0) {
                boolean responseStatus = false;
                while (!responseStatus) {

                    try (Socket soc = new Socket(serverName, portNumber)) {


                        String request = "GET HTTP/1.1\r\n" +
                                "Lamport-Clock: " + clock.getCurrentTime() + "\r\n" +
                                "CS-ID: " + uniqueContentServerId + "\r\n\r\n";

                        OutputStream outputStream = soc.getOutputStream();
                        outputStream.write(request.getBytes());
                        outputStream.flush();

                        BufferedReader reader = new BufferedReader(new InputStreamReader(soc.getInputStream()));
                        Object[] headerAndBody = Utils.handleHeaderAndBody(reader);

                        Map<String, String> headersMap = (Map<String, String>) headerAndBody[0];

                        clock.update(Integer.parseInt(headersMap.get("Lamport-Clock")));

                        reader.close();
                        outputStream.close();
                        soc.close();

                        Socket socPut= new Socket(serverName, portNumber);

                        String mainArrayString = mapper.writeValueAsString(arr.get(0));
                        request = "PUT /weather.json HTTP/1.1\r\n" +
                                "User-Agent: ContentServer/\r\n" +
                                "CS-ID: " + uniqueContentServerId + "\r\n" +
                                "Content-Type: application/json\r\n" +
                                "Lamport-Clock: " + clock.getCurrentTime() + "\r\n" +
                                "Content-Length: " + mainArrayString.length() + "\r\n\r\n" +
                                mainArrayString;

                        OutputStream outputStream1 = socPut.getOutputStream();
                        outputStream1.write(request.getBytes());
                        outputStream1.flush();

                        BufferedReader reader1 = new BufferedReader(new InputStreamReader(socPut.getInputStream()));
                        headerAndBody = Utils.handleHeaderAndBody(reader1);
                        headersMap = (Map<String, String>) headerAndBody[0];

                        clock.update(Integer.parseInt(headersMap.get("Lamport-Clock")));

                        if ("HTTP/1.1 201 - HTTP_CREATED".equals(headersMap.get("method")) || "HTTP/1.1 200 OK".equals(headersMap.get("method")) || "HTTP/1.1 204 No Content".equals(headersMap.get("method"))) {
                            System.out.println(headersMap.get("method"));
                            responseStatus = true;
                            arr.remove(0);
                            if (arr.size() == 0){
                                BackupJsonFile.delete();
                            }else{
                                Utils.updateContentServerBackUpFile(arr, uniqueContentServerId);
                            }
                        }else{
                            System.out.println("Trying to send data again........");
                        }

                        reader1.close();
                        outputStream1.close();
                        socPut.close();

                    } catch (Exception e) {
                        e.printStackTrace();
                        System.err.println("The server is not active, either we received some other status code when we sent data");
                        System.exit(1);
                    }

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
            System.err.println("You might have entered wrong port number or domain name of the server.");
            System.exit(1);
        }
    }
}

