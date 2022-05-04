import java.util.HashSet;
import java.util.LinkedList;
import java.net.URL;

public class UrlPool {
    // Список всех сайтов, которые мы успешно просмотрели
    static LinkedList<URLDepthPair> allSitesSeen = new LinkedList<URLDepthPair>();
    // Список всех сайтов, которые мы хотим просмотреть
    static LinkedList<URLDepthPair> toVisit = new LinkedList<URLDepthPair>();
    //Максимальная глубина относительно сайта введёного пользователем.
    int maxDepth;
    // Число потоков которые ждут адрес для обработки
    int waitCount;
    //Список хэшированных адресов, которые мы просмотрели.
    //это позволяет обеспечить уникальность адреса в результирующем списке.
    static HashSet<URL> seenURLs = new HashSet<URL>();

    /**
     * Конструктор класса.
     * Принимает на вход значения максимальной глубины.
     */
    public UrlPool(int maxDepth) {
        this.maxDepth = maxDepth;
        waitCount = 0;
    }

    /**
     *  Метод для получения пары для обработки из потока
     *  Так как к методу будут обращаться несколько потоков - устанавливаем для него синхронизацию (аналог мьютекса)
     */
    public synchronized URLDepthPair getNextPair() {
        // бесконечный цикл, в котором будут сидеть потоки и ждать работу
        while (toVisit.size() == 0) {
            try {
                waitCount++;
                wait();
                waitCount--;
            } catch (InterruptedException e) {
            }
        }
        URLDepthPair nextPair = toVisit.removeFirst();
        return nextPair;
    }

    /**
     *  Метод для добавления пары из потока в результирующий список
     *  Он обеспечивает уникальность адреса и дополнительную проверку выхода за пределы "глубины"
     */
    public synchronized void addPair(URLDepthPair pair) {
        if (seenURLs.contains(pair.getURL())) {
            return;
        }
        allSitesSeen.add(pair);
        seenURLs.add(pair.getURL());
        if (pair.getDepth() < maxDepth) {
            toVisit.add(pair);
            // Говорим потокам, сидящим в wait (метод getNextPair), что для них есть работа.
            notify();
        }
    }

    /**
     *  Метод для получения числа потоков, сидящих без работы.
     *  Служит для проверки, что все потоки завершили работу
     */
    public synchronized int getWaitCount() {

        return waitCount;
    }

    /**
     *  Метод для получения максимальной глубины адресов.
     *  Служит для ускорения работы потоков путём пропуска адресов выходящих за границу глубины
     */
    public synchronized int getMaxDepth() {

        return maxDepth;
    }

    /**
     *  Метод возвращает результирующий списков пар.
     *  Служит для вывода списка в консоль
     */
    public LinkedList<URLDepthPair> allSitesSeen() {
        return allSitesSeen;
    }
}
