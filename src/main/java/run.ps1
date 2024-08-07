# Переход в директорию src/main/java
Set-Location -Path "src/main/java"

# Установка кодировки на 1251
chcp 1251

# Компиляция с указанием кодировки и открытием модулей
javac -encoding UTF-8 --add-opens java.base/java.util=ALL-UNNAMED HashMapLogger.java Main.java

# Запуск программы с открытием модулей
java --add-opens java.base/java.util=ALL-UNNAMED Main

