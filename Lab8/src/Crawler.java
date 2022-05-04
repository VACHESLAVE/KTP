import java.net.MalformedURLException;
import java.util.LinkedList;
import java.net.URL;
// Основной класс программы
public class Crawler{
    public static void crawl(String startURL, int maxDepth, int numThreads)
            throws MalformedURLException {

        //Инициализируем пул адресов
        UrlPool pool = new UrlPool(maxDepth);
        //Формируем корневой элемент(глубина 0) на основе введённых пользователем данных...
        URL rootURL = new URL(startURL);
        URLDepthPair urlPair = new URLDepthPair(rootURL, 0);
        //... и добавляем в пул
        pool.addPair(urlPair);

        // Запускаем потоки. Количество может задаваться пользователем. 1 - по умолчанию.
        for (int i = 0; i < numThreads; i++) {
            //передаём пул адресов и id нужен для вывода в консоль в каждый созданный поток
            CrawlerTask c = new CrawlerTask(i+1,pool);
            Thread t = new Thread(c);
            t.start();
        }

        // Ожидаем завершения работы всех потоков
        while (pool.getWaitCount() != numThreads) {
            try {
                Thread.sleep(100); // 0.1 second
            } catch (InterruptedException ie) {
                System.out.println("Some thing went wrong!");
            }
        }
        // Закончили осмотр - выводим результаты в консоль
        System.out.println( "\n" + "Result list of sites: ");
        // формат вывода: URL: https://habr.com/ru/about/, Depth: 0
        LinkedList<URLDepthPair> foundUrls = pool.allSitesSeen();
        for (URLDepthPair pair : foundUrls) {
            System.out.println(pair.toString());
        }
    }


    public static void main(String[] args) {

        // если пользователь не ввёл аргументы - ругаемся и отказываемся работать
        String startURL = "";
        int maxDepth = 0;
        int threadsNum = 1;

        switch (args.length) {
            case 4:
                //Количество потоков, с которыми будем работать
                threadsNum = Integer.parseInt(args[3]);
                //если пользователь ввёл дичь - ставим по умолчанию.
                if(threadsNum<1)
                    threadsNum=1;
            case 2:
                startURL = args[0];
                maxDepth = Integer.parseInt(args[1]);
                break;
            default:
                System.out.println("usage: java Crawler <URL> <maximum_depth> (-t <num_threads>)");
                System.exit(1);
                break;
        }

        try {
            // Начинаем сканировать интернеты начиная с заданного адреса.
            crawl(startURL, maxDepth, threadsNum);
        }
        catch (MalformedURLException e) {
            //Если пользователь ввёл кривой URL - приходим сюда, чтобы его обрадовать.
            System.err.println("Error: The URL " + args[0] + " is not valid");
            System.exit(1);
        }
        System.exit(0);
    }
}
