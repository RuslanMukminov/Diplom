import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server {
    protected int port;
    protected BooleanSearchEngine engine;

    public Server(int port, BooleanSearchEngine engine) {
        this.port = port;
        this.engine = engine;
    }

    public void start() throws IOException {
        System.out.println("Starting server at " + port + " port.");
        try (var serverSocket = new ServerSocket(port)) {
            while (true) {
                try (
                        Socket socket = serverSocket.accept();
                        var in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                        var out = new PrintWriter(socket.getOutputStream())
                ) {
                    String request = in.readLine();
                    request = request.toLowerCase();
                    List<PageEntry> pageEntryList = engine.search(request);
                    out.println(answerJson(pageEntryList));
                }
            }
        } catch (IOException e) {
            System.out.println("Не могу стартовать сервер");
            e.printStackTrace();
        }
    }

    private String answerJson(List<PageEntry> pageEntryList) {
        var builder = new GsonBuilder();
        Gson gson = builder.create();
        List<String> jsonList = new ArrayList<>();

        if (pageEntryList.isEmpty()) {
            return gson.toJson("не найдено");
        }

        for (PageEntry pageEntry : pageEntryList) {
            jsonList.add(gson.toJson(pageEntry));
        }
        return jsonList.toString();
    }
}
