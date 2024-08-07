import java.util.HashMap;
/*
 * Обход политики доступа рефлексии Java >= 9:
 * В терминале перейти в папку java, поменять кодировку в терминале на 1251, скомпилировать с разрешением и кодировкой UTF-8, запустить с разрешением:
 *
 * cd .../java
 * chcp 1251
 * javac -encoding UTF-8 --add-opens java.base/java.util=ALL-UNNAMED HashMapLogger.java Main.java
 * java --add-opens java.base/java.util=ALL-UNNAMED Main
 */

/**
 * Здесь можно посмотреть логи на основные взаимодействия с HashMap, чтобы лучше понять как он работает.
 * Позапускай методы get и put через обёртку и посмотри в терминале, что в "кишках" творится.
 * @see HashMap
 * @see HashMapLogger
 * @see HashMap
 */
public class Main {
    public static void main(String[] args) {
        HashMapLogger<String, Integer> loggerContainer = new HashMapLogger<>(new HashMap<>());
        for (int i = 0; i < 13; i++) {
            loggerContainer.put(String.valueOf(i), i);
            loggerContainer.logBuckets();
        }
    }
}
