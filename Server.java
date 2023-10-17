import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class Server {
    public static void main(String[] args) throws IOException {
        try {
            ServerSocket socket = new ServerSocket(4040);

            while (true) {
                Socket client = socket.accept();

                new Thread(() -> {
                    try {
                        InputStream in = client.getInputStream();
                        OutputStream out = client.getOutputStream();
                        ByteArrayOutputStream req = new ByteArrayOutputStream();
                        transfer(req, in);
                        HttpRequest httpRequest = parseMetaData(new ByteArrayInputStream(req.toByteArray()));

                        handleGetRequest(httpRequest, out);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    } finally {
                        try {
                            client.close();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void transfer(OutputStream out, InputStream in) throws IOException {
        int bytes;
        byte[] buffer = new byte[2048];
        while ((bytes = in.read(buffer, 0, Math.min(in.available(), 2048))) > 0) {
            out.write(buffer, 0, bytes);
        }
    }

    public static HttpRequest parseMetaData(InputStream data) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(data));
        Map<String,String> header = new HashMap<>();
        String firstLine = reader.readLine();
        String url = firstLine.split(" ")[1];
        String method = firstLine.split(" ")[0];
        String line;

        while((line = reader.readLine()) != null) {
            if(line.trim().isEmpty()) {
                break;
            }

            String key = line.split(":\\s")[0];
            String value = line.split(":\\s")[1];

            header.put(key, value);
        }

        return new HttpRequest(method, url, header);
    }

    public static void handleGetRequest(HttpRequest request, OutputStream out) throws IOException {
        String fileName = request.getUrl();
        String filePath = Paths.get("").toAbsolutePath().toString();
        File file = new File(filePath + fileName);

        if(!file.exists()) {
            try {
                out.write("HTTP/1.1 404 Not Found\r\n\r\n<h1>File not found!</h1>".getBytes());
                return;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        StringBuilder sb = new StringBuilder();
        sb.append("HTTP/1.1 200 OK \r\n");
        sb.append(String.format("Content-Type: %s\r\n", probeContentType(fileName)));

        InputStream is = new FileInputStream(filePath + fileName);
        sb.append(String.format("Content-Length: %d\r\n", is.available()));
        sb.append("\r\n");

        out.write(sb.toString().getBytes());
        is.transferTo(out);
    }

    public static String probeContentType(String fileName) {
        String[] tokens = fileName.split("\\.");
        String extension = tokens[tokens.length - 1];

        return switch (extension) {
            case "html" -> "text/html";
            case "gif" -> "image/gif";
            case "txt" -> "image/plain";
            case "jpg" -> "image/jpg";
            case "jpeg" -> "image/jpeg";
            case "png" -> "image/png";
            default -> "text/plain";
        };
    }
}
