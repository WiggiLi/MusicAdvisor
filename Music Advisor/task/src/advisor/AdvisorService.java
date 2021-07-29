package advisor;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;

public class AdvisorService {
    boolean Authorization = false;
    HashMap<String, String> categories = new HashMap<String, String>();

    public boolean isAuthorization(){
        return this.Authorization;
    }

    public String setAuthorization(boolean val){
        this.Authorization = val;
        if (val){
            return "Success!";
        }
        return "false";
    }

    public int getNewReleases(int offset, int countEntriesOnPage, String abc){ //abc - necessary only in getCategories(namePlaylist)
        if (Authorization){
            String url = Authorisation.SERVER_PATH_RESOURCE +"/v1/browse/new-releases" + "?offset="+offset+"&limit="+countEntriesOnPage;
            String res = getRequest(url);

            JsonObject jsonObject = JsonParser.parseString(res).getAsJsonObject();

            JsonObject albumsObj = jsonObject.getAsJsonObject("albums");
            int total = albumsObj.get("total").getAsInt();

            for (JsonElement album : albumsObj.getAsJsonArray("items")) {
                JsonObject albumObj = album.getAsJsonObject();

                System.out.println(albumObj.get("name").getAsString());

                System.out.print("[");
                int i = 0;
                for (JsonElement artist : albumObj.getAsJsonArray("artists")) {
                    JsonObject artistObj = artist.getAsJsonObject();

                    System.out.print(artistObj.get("name").getAsString());
                    if(i++ != albumObj.getAsJsonArray("artists").size() - 1){
                        System.out.print(", ");
                    }
                }
                System.out.println("]");

                JsonObject urlObj = albumObj.getAsJsonObject("external_urls");
                System.out.println(urlObj.get("spotify").getAsString());
                System.out.println();
            }
            return total;
        } else
            System.out.println("Please, provide access for application.");
        return -1;
    }

    public int getFeaturedPlaylists(int offset, int countEntriesOnPage, String abc){
        if (Authorization){
            String url = Authorisation.SERVER_PATH_RESOURCE +"/v1/browse/featured-playlists" + "?offset="+offset+"&limit="+countEntriesOnPage;
            String res = getRequest(url);

            JsonObject jsonObject = JsonParser.parseString(res).getAsJsonObject();
            JsonObject albumsObj = jsonObject.getAsJsonObject("playlists");
            int total = albumsObj.get("total").getAsInt();

            for (JsonElement album : albumsObj.getAsJsonArray("items")) {
                JsonObject albumObj = album.getAsJsonObject();

                System.out.println(albumObj.get("name").getAsString());

                JsonObject urlObj = albumObj.getAsJsonObject("external_urls");
                System.out.println(urlObj.get("spotify").getAsString());
                System.out.println();
            }
            return total;
        } else
            System.out.println("Please, provide access for application.");
        return -1;
    }

    public int getCategories(int offset, int countEntriesOnPage, String abc){
        if (Authorization){
            String url = Authorisation.SERVER_PATH_RESOURCE +"/v1/browse/categories" + "?limit="+countEntriesOnPage +"&offset="+offset;
            String res = getRequest(url);

            JsonObject jsonObject = JsonParser.parseString(res).getAsJsonObject();
            System.out.println("jsonObject "+jsonObject);
            JsonObject albumsObj = jsonObject.getAsJsonObject("categories");
            int total = albumsObj.get("total").getAsInt();

            for (JsonElement album : albumsObj.getAsJsonArray("items")) {
                JsonObject albumObj = album.getAsJsonObject();

                categories.put(albumObj.get("name").getAsString(), albumObj.get("id").getAsString());
                System.out.println(albumObj.get("name").getAsString());
            }
            return total;
        } else
            System.out.println("Please, provide access for application.");
        return -1;
    }

    public void getCategories(boolean output){
        if (Authorization){
            String url = Authorisation.SERVER_PATH_RESOURCE +"/v1/browse/categories";
            String res = getRequest(url);

            JsonObject jsonObject = JsonParser.parseString(res).getAsJsonObject();
            JsonObject albumsObj = jsonObject.getAsJsonObject("categories");

            for (JsonElement album : albumsObj.getAsJsonArray("items")) {
                JsonObject albumObj = album.getAsJsonObject();

                categories.put(albumObj.get("name").getAsString(), albumObj.get("id").getAsString());

                if (output)
                    System.out.println(albumObj.get("name").getAsString());
            }
        } else
            System.out.println("Please, provide access for application.");
    }

    public int getPlaylistsForCategorie(int offset, int countEntriesOnPage, String nameCategory){
        if (Authorization){
            getCategories(false); //fill list of categories

            System.out.println("nameCategory "+nameCategory);
            String idCategory = categories.get(nameCategory);
            if (idCategory == null) {
                System.out.println("Unknown category name.");
            } else {
                System.out.println("idCategory "+idCategory);
                String url = Authorisation.SERVER_PATH_RESOURCE + "/v1/browse/categories/" + idCategory + "/playlists" + "?offset="+offset+"&limit="+countEntriesOnPage;
                String res = getRequest(url);

                JsonObject jsonObject = JsonParser.parseString(res).getAsJsonObject();
                if (jsonObject.getAsJsonObject("error") != null){
                    JsonObject error = jsonObject.getAsJsonObject("error");
                    String reason = error.get("message").getAsString();
                    System.out.println(reason);
                    return -1;
               }
                System.out.println("jsonObject "+jsonObject);
                JsonObject albumsObj = jsonObject.getAsJsonObject("playlists");
                int total = albumsObj.get("total").getAsInt();

                for (JsonElement album : albumsObj.getAsJsonArray("items")) {
                    JsonObject albumObj = album.getAsJsonObject();

                    System.out.println(albumObj.get("name").getAsString());

                    JsonObject urlObj = albumObj.getAsJsonObject("external_urls");
                    System.out.println(urlObj.get("spotify").getAsString());
                    System.out.println();
                }
                return total;
            }
        } else
            System.out.println("Please, provide access for application.");
        return -1;
    }

    public String getRequest(String url){
        HttpClient client = HttpClient.newBuilder().build();
        HttpRequest request = HttpRequest.newBuilder()
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("Authorization", "Bearer " + Authorisation.ACCESS_TOCKEN)
                .uri(URI.create(url))
                .build();
        try{
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return response.body();
        }
        catch (InterruptedException | IOException e) {
            System.out.println("Error response");
        }
        return "";
    }
}
