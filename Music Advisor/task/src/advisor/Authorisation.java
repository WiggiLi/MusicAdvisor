package advisor;

import com.google.gson.*;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.net.URI;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class Authorisation {
    public static String REDIRECT_URL="http://localhost:8080";
    public static String SERVER_PATH_ACCESS = "https://accounts.spotify.com";
    public static String SERVER_PATH_RESOURCE = "https://api.spotify.com";
    public static String ACCESS_TOCKEN = "";
    public static String ACCESS_CODE = "";
    public static String CLIENT_ID = "eda2fc1315e64971a484eb1f8ff1801a";
    public static String CLIENT_SECRET ="5d12facba24c4830b98512b819e6ddec";
    private volatile boolean value = false;

    public void getAccessCode() {
            //Creating a server and listening to the request.
            try {
                HttpServer server = HttpServer.create();
                server.bind(new InetSocketAddress(8080), 0);
                server.start();
                server.createContext("/",
                        new HttpHandler() {
                            public void handle(HttpExchange exchange) throws IOException {
                                String query = exchange.getRequestURI().getQuery();
                                String responce;
                                if (query != null && query.contains("code")) {
                                    ACCESS_CODE = query.substring(5);
                                    System.out.println("code received");
                                    responce = "Got the code. Return back to your program.";
                                } else {
                                    responce = "Authorization code not found. Try again.";
                                }
                                exchange.sendResponseHeaders(200, responce.length());
                                exchange.getResponseBody().write(responce.getBytes());
                                exchange.getResponseBody().close();
                                value = true;
                            }
                        }
                );
                System.out.println("waiting for code...");

                while(ACCESS_CODE.equals(""))//(!value)
                {
                    Thread.sleep(10);
                }
                server.stop(5); ///
            } catch (InterruptedException | IOException e) { //
                System.out.println("Server error");
            }
        }

    public void getAccessToken() {
        String URIstring = SERVER_PATH_ACCESS + "/api/token";
        HttpRequest request = HttpRequest.newBuilder()
                .header("Content-Type", "application/x-www-form-urlencoded")
                .uri(URI.create(URIstring))
                .POST(HttpRequest.BodyPublishers.ofString(  "grant_type=authorization_code" +
                        "&code=" + ACCESS_CODE +
                        "&client_id=" + CLIENT_ID +
                        "&client_secret=" + CLIENT_SECRET +
                        "&redirect_uri=" + "http://localhost:8080"))
                .build();
        System.out.println("Making http request for access_token...");
        try{
            HttpClient client = HttpClient.newBuilder().build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            JsonObject jsonObject = JsonParser.parseString(response.body()).getAsJsonObject();
            ACCESS_TOCKEN = jsonObject.get("access_token").getAsString();
        }
        catch (InterruptedException | IOException e) {
            System.out.println("Error response");
        }
    }
}
