import java.util.HashMap;
/*
 * Обход политики доступа рефлексии Java >= 9:
 * В терминале перейти в папку java, поменять кодировку в терминале на 1251, скомпилировать с разрешением и кодировкой UTF-8, запустить с разрешением:
 *
 * cd src/main/java
 * chcp 1251
 * javac -encoding UTF-8 --add-opens java.base/java.util=ALL-UNNAMED HashMapLogger.java Main.java
 * java --add-opens java.base/java.util=ALL-UNNAMED Main
 *
 * либо запусти run.ps1
 * P.S Если run.ps1 вытрёпывается на безопасность, открой его отдельно от имени администратора и вставь Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser.
 */

/**
 * Здесь можно посмотреть логи на основные взаимодействия с HashMap, чтобы лучше понять как он работает.
 * Позапускай методы get и put через обёртку и посмотри в терминале, что в "кишках" творится.
 * @see HashMap
 * @see HashMapLogger
 */
public class Main {
    public static void main(String[] args) {
        HashMapLogger<String, Integer> loggerContainer = new HashMapLogger<>(new HashMap<>());
        for (int i = 0; i < 25; i++) {
            loggerContainer.put(String.valueOf(i) + "---", i * i);
            loggerContainer.logBuckets();
        }
        System.out.println("Answer: " + loggerContainer.get("21"));
    }
}
