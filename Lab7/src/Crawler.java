import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.LinkedList;
import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;
import java.net.*;

public class Crawler {
    // HREF_TAG - регулярное выражение, по которому находятся ссылки

    static final String HREF_TAG = "<a href=\"http";

    // Список всех просмотренных сайтов
    static LinkedList<URLDepthPair> allSitesSeen = new LinkedList<URLDepthPair>();
    // Список всех не просмотренных сайтов
    static LinkedList<URLDepthPair> toVisit = new LinkedList<URLDepthPair>();


    public static void crawl(String startURL, int maxDepth)
            throws MalformedURLException {

        //Формируем корневой элемент(глубина 0) на основе введённых пользователем данных
        URL rootURL = new URL(startURL);
        URLDepthPair urlPair = new URLDepthPair(rootURL, 0);
        toVisit.add(urlPair);

        int depth;

        HashSet<URL> seenURLs = new HashSet<URL>();
        seenURLs.add(rootURL);
        SocketFactory socketFactory = SSLSocketFactory.getDefault();
        while (!toVisit.isEmpty()) {
            URLDepthPair currPair = toVisit.removeFirst();
            depth = currPair.getDepth();
            if (depth > maxDepth) {
                continue;
            }

            // Инициализируем сокет и отравляем запрос на сайт.
            Socket socket;
            try {
                socket = socketFactory.createSocket(currPair.getHost(), 443);
                socket.setSoTimeout(5000);
                System.out.println("Connecting to " + currPair.getURL());
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                // Отправляем HTTP запрос
                out.println("GET " + currPair.getDocPath() + " HTTP/1.1");
                out.println("Host: " + currPair.getHost());
                out.println("Connection: close");
                out.println();
            } catch (UnknownHostException e) {
                System.err.println("Host " + currPair.getHost() + " couldn't be determined");
                continue;
            } catch (SocketException e) {
                System.err.println("Error with socket connection: " + currPair.getURL() +
                        " - " + e.getMessage());
                continue;
            } catch (IOException e) {
                System.err.println("Couldn't retrieve page at " + currPair.getURL() +
                        " - " + e.getMessage());
                continue;
            }

            String line;
            int lineLength;
            int shiftIdx;
            boolean firstTry = true;
            try {
                //Будем читать буфер сокета пока не дойдём до конца
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                while ((line = in.readLine()) != null) {
                    if (firstTry) {
                        firstTry = false;
                        if (line.equals("HTTP/1.1 200 OK")) {
                            System.out.println("Connected successfully!");
                            continue;
                        } else {
                            System.out.println("Server return error: " + line);
                            break;
                        }
                    }
                    boolean foundFullLink = false;
                    int idx = line.indexOf(HREF_TAG);
                    if (idx > 0) {
                        StringBuilder sb = new StringBuilder();
                        shiftIdx = idx + 9;
                        char c = line.charAt(shiftIdx);
                        lineLength = line.length();
                        while (c != '"' && shiftIdx < lineLength - 1) {
                            sb.append(c);
                            shiftIdx++;
                            c = line.charAt(shiftIdx);
                            if (c == '"') {
                                foundFullLink = true;
                            }
                        }
                        if (foundFullLink) {
                            URL currentURL = new URL(sb.toString());
                            if (!seenURLs.contains(currentURL)) {
                                URLDepthPair newPair = new URLDepthPair(currentURL, depth + 1);
                                toVisit.add(newPair);
                                seenURLs.add(currentURL);
                            }
                        }
                    }
                }

                // Закрытие сокета
                in.close();
                socket.close();
                // и добавление в результирующий список
                allSitesSeen.add(currPair);
            } catch (IOException e) {
            }
        }
        // В конце - выводим все результаты
        System.out.println("\n" + "Result list of sites: ");

        // Формат вывода: URL: https://habr.com/ru/about/, Depth: 0
        for (URLDepthPair pair : allSitesSeen) {
            System.out.println(pair.toString());
        }
    }

    public static void main(String[] args) {
        // Пользователь должен ввести 2 аргумента. Иначе выводим ошибку
        if (args.length != 2) {
            System.out.println("usage: java Crawler <URL> <maximum_depth>");
            System.exit(1);
        }
        try {
            // Сканируем начиная с указанного адреса
            crawl(args[0], Integer.parseInt(args[1]));
        } catch (MalformedURLException e) {
            // Проверка введенного URL на корректность
            System.err.println("Error: The URL " + args[0] + " is not valid");
            System.exit(1);
        }
        System.exit(0);
    }
}
