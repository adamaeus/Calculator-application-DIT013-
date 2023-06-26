import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import static java.lang.System.out;
import static java.net.HttpURLConnection.HTTP_OK;

/*

        A Server to be able to do calculation on the Web
        Start this and visit localhost:8080

        **** NOTHING TO DO HERE ****

 */
public class WebCalcServer {

    public static void main(String[] args) throws IOException {
        new WebCalcServer().run();
    }

    public final int DEFAULT_PORT = 8080;
    public final String HOME_URL = "/";
    private final Calculator calc = new Calculator();

    private void run() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(DEFAULT_PORT), 0);
        HttpContext context = server.createContext(HOME_URL);
        context.setHandler(this::handleRequest);
        server.start();
        out.println("Server started. In a web browser, visit localhost:" + server.getAddress().getPort());
    }

    // Main method to handle HTTP requests
    private void handleRequest(HttpExchange exchange) {
        try {
            out.println(exchange.getRequestMethod());
            out.println(exchange.getRequestURI());
            if (exchange.getRequestMethod().equals("GET")) {
                String page = getPage(Double.NaN);
                sendResponse(exchange, page);
            } else if (exchange.getRequestMethod().equals("POST")) {
                Scanner sc = new Scanner(exchange.getRequestBody());
                String args = sc.nextLine();
                // This is "Expression=1+2 ..."
                String expr = args.split("=")[1];
                expr = URLDecoder.decode(expr, "UTF-8");
                double result = calc.eval(expr);
                out.println(expr + " = " + result);
                sendResponse(exchange, getPage(result));
            } else {
                sendResponse(exchange, "En Error occurred");
            }
        } catch (IOException ioe) {
            out.println("An exception occurred " + ioe.getMessage());
        }
    }

    // ---------- Helper Methods ----------------

    private String getPage(double result) {
        PageBuilder pb = new PageBuilder();
        return pb.addTitle("Welcome to WebCalc")
                .addH1("Welcome to WebCalc")
                .addForm("/", "Enter expression", Arrays.asList("Expression"))
                .addParagraph("Result = " + result)
                .toString();
    }

    private void sendResponse(HttpExchange exchange, String response) throws IOException {
        exchange.sendResponseHeaders(HTTP_OK, response.getBytes().length);
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }

    // --- Helper class to build HTML Elements -----------

    class PageBuilder {
        final List<String> head = new ArrayList<>();
        final List<String> body = new ArrayList<>();

        PageBuilder addTitle(String title) {
            head.add("<title>" + title + "</title>");
            return this;
        }

        PageBuilder addH1(String text) {
            body.add("<h1>" + text + "</h1>");
            return this;
        }

        PageBuilder addParagraph(String text) {
            body.add("<p>" + text + "</p>");
            return this;
        }

        PageBuilder addForm(String action, String legend, List<String> names) {
            StringBuilder sb = new StringBuilder();
            sb.append("<form action=\"").append(action)
                    .append("\" method=\"POST\">")
                    .append("<fieldset><legend>")
                    .append(legend).append("</legend>");
            for (String name : names) {
                sb.append(name);
                sb.append("<br/>");
                sb.append("<input type=\"text\" name=\"");
                sb.append(name);
                sb.append("\"><br/>");
            }
            sb.append("<input type=\"submit\" value=\"Submit\">");
            sb.append("</fieldset></form>");
            body.add(sb.toString());
            return this;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("<!doctype html><html lang=\"en\">")
                    .append("<head><link rel=\"icon\" href=\"data:,\"><meta charset=\"utf-8\">");
            for (String s : head) {
                sb.append(s);
            }
            sb.append("</head>").append("<body>");
            for (String s : body) {
                sb.append(s);
            }
            sb.append("</body>").append("</html>");
            return sb.toString();
        }
    }
}

