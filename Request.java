import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class Request implements Runnable {
    private final Socket client;

    public Request(Socket client) {
        this.client = client;
    }

    @Override
    public void run() {

    }


}