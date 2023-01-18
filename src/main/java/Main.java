import java.io.File;

public class Main {
    public static void main(String[] args) throws Exception {
        var engine = new BooleanSearchEngine(new File("pdfs"));
        Server server = new Server(8989, engine);
        server.start();
    }
}
