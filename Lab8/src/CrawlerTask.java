import java.io.*;
import java.net.*;
import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;

/**
 *  Поток для обработки адресов
 */
public class CrawlerTask implements Runnable {
    //Ссылка на пул адресов
    UrlPool pool;
    // HTML href тэг, по которому будем ловить ссылки
    static final String HREF_TAG = "<a href=\"http";
    //id потока
    int idThread;

    public CrawlerTask(int idThread, UrlPool pool) {

        this.pool = pool;
        this.idThread = idThread;
    }

    @Override
    public void run() {
        SocketFactory socketFactory = SSLSocketFactory.getDefault();
        while (true) {
            URLDepthPair currPair = pool.getNextPair();
            int currDepth = currPair.getDepth();

            if (currDepth>pool.getMaxDepth()){
                continue;
            }
            Socket socket;
            try {
                socket = socketFactory.createSocket(currPair.getHost(), 443);
                socket.setSoTimeout(5000);
                System.out.println("Thread:" + idThread+ " Connecting to " + currPair.getURL());
                System.out.flush();
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                out.println("GET " + currPair.getDocPath() + " HTTP/1.1");
                out.println("Host: " + currPair.getHost());
                out.println("Connection: close");
                out.println();
            }
            catch (UnknownHostException e) {
                System.err.println("Thread:" + idThread+ " Host "+ currPair.getHost() + " couldn't be determined");
                continue;
            }
            catch (SocketException e) {
                System.err.println("Thread:" + idThread+ " Error with socket connection: " + currPair.getURL() +
                        " - " + e.getMessage());
                continue;
            }
            catch (IOException e) {
                System.err.println("Thread:" + idThread+ " Couldn't retrieve page at " + currPair.getURL() +
                        " - " + e.getMessage());
                continue;
            }

            String line;
            int lineLength;
            int shiftIdx;
            boolean firstTry = true;
            try{
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                while ((line = in.readLine()) != null) {
                    if(firstTry){
                        firstTry = false;
                        if(line.equals("HTTP/1.1 200 OK")){
                            System.out.println("Thread:" + idThread+ " Connected successfully!");
                            System.out.flush();
                            continue;
                        }else{
                            System.out.println("Thread:" + idThread+ " Server return error: " + line);
                            System.out.flush();
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
                        if(foundFullLink) {
                            URL currentURL = new URL(sb.toString());
                            URLDepthPair newPair = new URLDepthPair(currentURL, currDepth + 1);
                            pool.addPair(newPair);
                        }
                    }
                }

                in.close();
                socket.close();
            }
            catch (IOException e) {
            }
        }
    }
}