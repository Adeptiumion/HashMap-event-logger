import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

/**
 * Класс-обертка под HashMap для регистрации инкапсулированного состояния HashMap.
 *
 * @param <K> ключ HashMap
 * @param <V> значение HashMap
 */
public class HashMapLogger<K, V> {

    /**
     * Логгер, который будет выводить, то что скрыто в "кишках" HashMap.
     *
     * @see Logger
     * @see LoggerProvider
     */
    private static final Logger mapLogger = LoggerProvider.instanceLogger(HashMapLogger.class.getSimpleName());
    /**
     * Коэффициент загрузки, при котором число бакетов в HashMap увеличится в 2 раза.
     */
    private float loadFactory;

    /**
     * Это значение, которое определяет, когда хеш-таблица должна быть расширена.
     * Порог рассчитывается как произведение текущей емкости (количества бакетов) и фактора загрузки (load factor)
     */
    private int threshold;

    /**
     * HashMap обернутая данным классом.
     */
    private final HashMap<K, V> map;


    /**
     * Конструктор принимающий HashMap для логгирования.
     *
     * @param map маппа.
     */
    public HashMapLogger(HashMap<K, V> map) {

        /*
         * Инициализируем логируемый HashMap.
         */
        this.map = map;

        /*
         * Определяю поля HashMap, которые я буду "выкорчёвывать" с помощью рефлексии для последующего наблюдения за их состоянием.
         * Нужно это для большей ясности, что и когда происходит, так как доступ к этим полям "ограничен".
         */
        Field loadFactoryField, thresholdField;
        try {
            /*
             * Обращение к полям класса через рефлексию.
             */
            loadFactoryField = HashMap.class.getDeclaredField("DEFAULT_LOAD_FACTOR");
            thresholdField = HashMap.class.getDeclaredField("threshold");
            /*
             * Открытие доступа к полям.
             */
            loadFactoryField.setAccessible(true);
            thresholdField.setAccessible(true);
            /*
             * Получаем значения полей указанных объектов.
             * Приводим к нужному типу.
             */
            loadFactory = (float) loadFactoryField.get(this.map);
            threshold = (int) thresholdField.get(this.map);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * Обёртка оригинального метода put класса HashMap
     *
     * @param key   ключ.
     * @param value значение.
     */
    public void put(K key, V value) {

        mapLogger.severe("\n\n\nНачинаем вставлять пару (" + key + ", " + value + ")"); // Сообщение о старте вставки и логирования.

        /*
         Это просто локальный класс, предоставляющий логику хэширования.
         */
        class HashCoder {
            // "Cкопипащенно" из реализации HashMap:
            static int hash(Object key) {
                int h;
                return (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16);
            }
        }

        map.put(key, value); // Сама вставка.

        /*
         * n - число бакетов маппы.
         * threshold - порог расширения бакетов.
         * loadFactory - локальный коэффициент загрузки для определения того изменился ли он?
         * buckets - сами бакеты.
         * tableField - поле класса HashMap, содержащее бакеты(и ноды внутри них).
         * loadFactoryField - поле класса HashMap, определяющее коэффициент загрузки.
         * thresholdField - поле класса HashMap, это значение, которое определяет, когда хеш-таблица должна быть расширена.
         * Порог рассчитывается как произведение текущей емкости (количества бакетов) и фактора загрузки (load factor).
         */
        int n, threshold;
        float loadFactory;
        Object[] buckets;
        Field tableField, loadFactoryField, thresholdField;

        try {
            /*
             * Обращение к полям класса через рефлексию.
             */
            tableField = HashMap.class.getDeclaredField("table");
            loadFactoryField = HashMap.class.getDeclaredField("loadFactor");
            thresholdField = HashMap.class.getDeclaredField("threshold");
            /*
             * Открытие доступа к полям.
             */
            tableField.setAccessible(true);
            loadFactoryField.setAccessible(true);
            thresholdField.setAccessible(true);
            /*
             * Получаем значения полей указанных объектов.
             * Приводим к нужному типу.
             */
            buckets = ((Object[]) tableField.get(this.map));
            n = buckets.length;
            loadFactory = (float) loadFactoryField.get(this.map);
            threshold = (int) thresholdField.get(this.map);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }

        // Если коэффициент загрузки поменялся, то выведу об этом сообщение и обновлю "локального наблюдателя".
        if (loadFactory != this.loadFactory) {
            mapLogger.severe("LOAD FACTORY (коэффициент загрузки) = " + loadFactory);
            this.loadFactory = loadFactory;
        }

        // Если порог для увеличения бакетов поменялся, то выведу об этом сообщение и обновлю "локального наблюдателя".
        if (threshold != this.threshold) {
            mapLogger.warning("threshold (порог увеличения бакетов) = " + threshold);
            mapLogger.warning("old threshold (старый порог увеличения бакетов) = " + this.threshold);
            mapLogger.warning("full buckets (заполненных бакетов) = " + Arrays.stream(buckets).filter(Objects::nonNull).toList().size()); // Ну тут если непонятно, то я просто фильтрую бакеты и считаю только те, что не null.
            this.threshold = threshold;
        }

        /*
         * hash - хэш ключа.
         */
        int hash = HashCoder.hash(key);
        /*
         * Форматирую ответ лога...
         */
        String answer = String.format("""
                        KEY(%s) <-> VALUE(%s)
                        Size of Buckets (всего бакетов) = %d
                        Hash of key (хэшкод ключа) - %s = %d
                        Index in buckets for this Node (индекс ноды в рамках бакета) = hash & (n - 1) = %d & (%d - 1) = %d & %d = (to binary (перевожу в двоичный вид)) %s & %s = %d""",
                key, value, n, key, key.hashCode(), hash, n, hash, (n - 1), Integer.toBinaryString(hash), Integer.toBinaryString(n - 1), (hash & (n - 1))
        );
        /*
         * Вывожу лог...
         */
        mapLogger.info(answer);

    }


    /**
     * Метод выводящий всю таблицу бакетов и нод внутри них.
     */
    public void logBuckets() {

        /*
         * table - поле класса HashMap, содержащее бакеты(и ноды внутри них).
         */
        Field table;

        try {
            /*
             * Обращение к полю класса через рефлексию.
             */
            table = HashMap.class.getDeclaredField("table");
            /*
             * Открытие доступа к полю.
             */
            table.setAccessible(true);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }

        try {
            /*
             * Получаем значение поля table/
             * Приводим к нужному типу.
             */
            Object[] buckets = (Object[]) table.get(this.map);
            /*
             * Кидаю бакеты в стрим, преобразую в строковый вид и формирую финальный список бакетов.
             */
            List<String> list = Arrays.stream(buckets).map(e -> {
                StringBuilder result = new StringBuilder();
                /*
                 * Преобразую бакет в строку. Запоминаю внутри преобразования индексы нод в рамках бакета.
                 */
                parseNodeToStringRecursive(e, result, 0);
                return result.toString();
            }).toList();

            /*
             * Ну тут просто вывожу красиво...
             */
            for (int i = 0; i < list.size(); i++) {
                System.out.println("bucket[" + i + "]" + list.get(i));
            }

        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * Метод преобразования ноды в строку.
     * @param node    сама нода.
     * @param result  билдер, который "собирает" результат для лога.
     * @param counter счетчик индексов нод.
     */
    private void parseNodeToStringRecursive(Object node, StringBuilder result, int counter) {

        /*
         * Пустой бакет? Значит выхожу отсюда и в билдер пихаю null.
         */
        if (node == null) {
            result.append(" NULL");
            return;
        }

        /*
         * keyField - поле хранящее ключ ноды в HashMap.
         * valueField - поле хранящее значение ноды в HashMap.
         * hashField - поле хранящее хэш ключа в HashMap.
         * nextField - поле хранящее следующую ноду на которую ссылается текущая.
         * key - значение поля хранящее ключ ноды в HashMap.
         * value - значение поля хранящее значение ноды в HashMap.
         * next - значение поля хранящее следующую ноду на которую ссылается текущая.
         * hash - значение поля хранящее хэш ключа в HashMap.
         */
        Field keyField, valueField, hashField, nextField;
        Object key, value, next;
        int hash;

        try {
            /*
             * Обращение к полям класса через рефлексию.
             */
            keyField = node.getClass().getDeclaredField("key");
            valueField = node.getClass().getDeclaredField("value");
            hashField = node.getClass().getDeclaredField("hash");
            nextField = node.getClass().getDeclaredField("next");
            /*
             * Открытие доступа к полям.
             */
            keyField.setAccessible(true);
            valueField.setAccessible(true);
            hashField.setAccessible(true);
            nextField.setAccessible(true);
            /*
             * Получаем значения полей указанных объектов.
             * Приводим к нужному типу.
             */
            key = keyField.get(node);
            value = valueField.get(node);
            hash = (int) hashField.get(node);
            next = nextField.get(node);
        } catch (NoSuchFieldException | IllegalAccessException ex) {
            throw new RuntimeException(ex);
        }

        result.append(" Node[").append(counter).append("] { KEY: ").append(key).append(" VALUE: ").append(value).append(" HASH: ").append(hash).append(" } ---> ");

        /*
         * Если есть нода, на которую ссылается текущая, то продолжаем парсить.
         */
        if (next != null) {
            parseNodeToStringRecursive(next, result, counter + 1);  // Рекурсивный вызов для следующей ноды
        } else {
            /*
             * Нет? Дополняю билдер и всё...
             */
            result.append("NULL");
        }

    }

}
