#Модуль для работы с версионированием схемы БД и справочниками
:warning: Сначала необходимо вручную создать схему БД к которой будут применятся апдейты, апдейты не способны сами создать схему и сразу в ней вести историю версий.  
Затем вручную настроить подключение к БД в файлике "liquibase.properties" 



## Настройки внутри файла *liquibase.properties*
Каждая из настроек в файле имеет вид:  
```propertyName: propertyValue```  
Необходимые настройки:

Имя | Описание | Пример
---| --- | ---
driver | имя JDBC класса драйвера к БД | com.mysql.jdbc.Driver
url | JDBC connection URL | jdbc:mysql://127.0.0.1:3306/liquibaseTest?useUnicode=true
username |  имя пользователя БД | root       
password |  пароль пользователя БД | root     
contexts | список контекстов для выполнения разделенных через запятую | SERBSKY, FNKC

##Использование утилиты из Maven 
:warning: Все команды выполняются относительно пути внутри модуля  
1. Накатить на БД миграции из заданного чейнджлога:  
```mvn liquibase:update -Dliquibase.changeLogFile=./src/main/resources/_total_.xml```
2. Создать SQL скрипт с миграциями для заданного чейнджлога вывод в файл из настройки [migrationSqlOutputFile](http://www.liquibase.org/documentation/maven/generated/updateSQL-mojo.html#migrationSqlOutputFile):  
```mvn liquibase:updateSQL -Dliquibase.changeLogFile=./src/main/resources/_total_.xml```
3. Сверить списки миграций между changeLog-ом и БД (проверяет *ТОЛЬКО* наличие миграций в DATABASECHANGELOG \[не сверяет сами схемы\])  
```mvn liquibase:status -Dliquibase.changeLogFile=./src/main/resources/_total_.xml```