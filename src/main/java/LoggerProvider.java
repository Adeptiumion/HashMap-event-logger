import java.util.logging.Logger;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * Утилити-класс предоставляющий гибкую настройку кастомного логгера и сам логгер.
 */
public class LoggerProvider {
    /**
     * Коды ANSI для изменения цвета
     * В ANSI-совместимых терминалах существует несколько цветовых кодов:
     * - 30 — черный
     * - 31 — красный
     * - 32 — зеленый
     * - 33 — желтый
     * - 34 — синий
     * - 35 — пурпурный
     * - 36 — циановый
     * - 37 — белый
     **/
    public static String RESET = "\u001B[0m";
    public static String SEVERE = "\u001B[31m";
    public static String INFO = "\u001B[37m";
    public static String WARNING = "\u001B[35m";
    public static String CONFIG = "\u001B[32m";

    /**
     * Экземпляр поставляемый классом.
     *
     * @see Logger
     */
    private static Logger logger;

    /**
     * Метод предоставляющий кастомный логгер.
     * Тут я ставлю свой форматёр, где определяю цвета для каждого из уровней логирования.
     * При желании их можно изменить в методах setColorOfSevere, setColorOfWarning, setColorOfInfo.
     *
     * @param className имя класса, который будет использовать логгер.
     * @return настроенный и готовый к использованию экземпляр кастомного логгера.
     * @see LoggerProvider
     * @see ConsoleHandler
     * @see Formatter
     * @see Level
     * @see LogRecord
     */
    public static Logger instanceLogger(String className) {
        logger = Logger.getLogger(className); // Установка имени логгера.
        ConsoleHandler consoleHandler = new ConsoleHandler(); // Обработчик вывода в консоль.
        consoleHandler.setLevel(Level.ALL); // Какие уровни сообщений будут обрабатываться ConsoleHandler и самим логгером.
        consoleHandler.setFormatter(new Formatter() { // Настройка формата сообщений.
            @Override
            public String format(LogRecord record) {
                String color;
                if (record.getLevel().intValue() >= Level.SEVERE.intValue())
                    color = SEVERE;
                else if (record.getLevel().intValue() >= Level.WARNING.intValue())
                    color = WARNING;
                else if (record.getLevel().intValue() >= Level.INFO.intValue())
                    color = INFO;
                else
                    color = CONFIG;
                return color + record.getMessage() + RESET + "\n"; // Перед сообщением, путём конкатенации можно определить код цвета текста. Определение цвета происходит в самом терминале!
            }
        });
        logger.addHandler(consoleHandler); // Устанавливаем именно наш обработчик вывода.
        logger.setUseParentHandlers(false); // Отключения родительских обработчиков логов, мы будем использовать исключительно наш!
        return logger;
    }

    /**
     * Определяет цвет для сообщения уровня SEVERE.
     *
     * @param color код цвета.
     * @return Logger.
     */
    public static Logger setColorOfSevere(String color) {
        SEVERE = color;
        return logger;
    }

    /**
     * Определяет цвет для сообщения уровня WARNING.
     *
     * @param color код цвета.
     * @return Logger.
     */
    public static Logger setColorOfWarning(String color) {
        WARNING = color;
        return logger;
    }

    /**
     * Определяет цвет для сообщения уровня INFO.
     *
     * @param color код цвета.
     * @return Logger.
     */
    public static Logger setColorOfInfo(String color) {
        INFO = color;
        return logger;
    }

}
