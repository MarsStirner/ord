package ru.entity.model.enums;

/**
 * @author Nastya Peshekhonova
 *         TODO: Временное решение, следует перевести это в таблицe базы данных и использовать ее. От куда были взяты номера статусов непонятно
 */
public enum DocumentStatus {
    PROJECT("Проект", 1),
    CHECK_IN_2("Зарегистрирован", 2),
    CHECK_IN_5("Зарегистрирован", 5),
    CHECK_IN_80("Зарегистрирован", 80),
    IN_ARCHIVE_99("В архиве", 99),
    IN_ARCHIVE_100("В архиве", 100),
    IN_ARCHIVE_3("В архиве", 3),
    OUT_ARCHIVE("Изъят из архива", 110),
    SOURCE_DESTROY("Оригинал уничтожен", 120),
    REDIRECT("Направлен в другой архив", 130),
    SEND("Отправлен", 200),
    RECIVE("Доставлен", 210),
    ON_REGISTRATION("На регистрации", 1),
    ON_EXECUTION_80("На исполнении", 80),
    ON_EXECUTION_2("На исполнении", 2),
    EXECUTE("Исполнен", 90),
    DOC_PROJECT_1("Проект документа", 1),
    DOC_PROJECT_12("Проект документа", 12),
    ON_CONSIDERATION("На рассмотрении", 2),
    AGREEMENT_3("Согласование", 3),
    AGREEMENT_14("Согласование", 14),
    CANCEL_150("Отказ", 150),
    CANCEL_4("Отказ", 4),
    DRAFT("Черновик", 1),
    ACTION_PROJECT("Проект дела", 1),
    REGISTERED("Зарегистрировано", 2),
    EXTRACT("Изъят", 110),
    CLOSE("Закрыт", 10),
    OPEN("Открыт", 2),
    VOLUME_PROJECT("Проект тома", 1),
    EXECUTED("Исполненo", 3),
    NEW("", 1);

    private final String name;
    private final int id;

    DocumentStatus(String name, int id) {
        this.name = name;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }
}
