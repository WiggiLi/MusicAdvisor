package advisor;

import java.util.Scanner;

@FunctionalInterface
interface TwoParameterFunction<T, U, C, R> {
    public R apply(T t, U u, C c);
}

public class Main {
    static int  countEntriesOnPage = 5;

    public static void main(String[] args) {
        for(int i = 0; i < args.length; i++){
            if(args[i].equals("-access")){
                Authorisation.SERVER_PATH_ACCESS = args[i+1];
            }
            if(args[i].equals("-resource")){
                Authorisation.SERVER_PATH_RESOURCE = args[i+1];
            }
            if(args[i].equals("-page")){
                countEntriesOnPage = Integer.parseInt(args[i+1]);
            }
        }
        start();
    }

    public static void start()  {
        AdvisorService advisorService = new AdvisorService();

        Scanner in = new Scanner(System.in);
        String command = in.nextLine();

        while (!command.equals("exit")) {
            String namePlaylist =  "";
            if (command.startsWith("playlists")) {
                String[] words = command.split(" ", 2);
                command = words[0];
                namePlaylist = words[1];
            }

            switch (command) {
                case "auth":
                    Authorisation authorisation = new Authorisation();
                    String res = authorisation.SERVER_PATH_ACCESS + "/authorize?client_id=eda2fc1315e64971a484eb1f8ff1801a&redirect_uri=http://localhost:8080&response_type=code";
                    System.out.println("use this link to request the access code:");
                    System.out.println(res);
                    authorisation.getAccessCode();
                    authorisation.getAccessToken();
                    String access = advisorService.setAuthorization(true);
                    System.out.println(access);
                    command = in.nextLine();
                    break;
                case "new"://
                    command = printPage(advisorService::getNewReleases, "");
                    break;
                case "featured"://
                    command = printPage(advisorService::getFeaturedPlaylists, "");
                    break;
                case "categories"://
                    command = printPage(advisorService::getCategories, "");
                    break;
                case "playlists"://
                    command = printPage(advisorService::getPlaylistsForCategorie, namePlaylist);
                    break;
                default:
                   System.out.println("Wrong command");
            }
        }
        in.close();
    }

    public static String printPage(TwoParameterFunction<Integer, Integer, String, Integer> nameFunction, String namePlaylist){
        Scanner in = new Scanner(System.in);
        int page = 0;
        int total = nameFunction.apply(page, countEntriesOnPage, namePlaylist);   //advisorService.getNewReleases(page, countEntriesOnPage);///
        int countPages = (int)Math.ceil( (float) total / countEntriesOnPage);

        System.out.printf("---PAGE %d OF %d---\n", page+1, countPages);
        String command = in.nextLine();
        while (command.equals("prev") || command.equals("next")) {
            if (command.equals("prev")) {
                if (page == 0) {
                    System.out.println("No more pages.");
                    command = in.nextLine();
                    continue;
                }
                else page--;
            }
            if (command.equals("next")) {
                if (page == countPages-1) {
                    System.out.println("No more pages.");
                    command = in.nextLine();
                    continue;
                }
                else page++;
            }
            nameFunction.apply(page*countEntriesOnPage, countEntriesOnPage, namePlaylist);
            System.out.printf("---PAGE %d OF %d---\n", page+1, countPages);
            command = in.nextLine();
        }
        return command;
    }
}
