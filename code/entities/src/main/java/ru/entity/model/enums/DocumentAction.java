package ru.entity.model.enums;

/**
 * @author Nastya Peshekhonova
 *         TODO: Временное решение, следует перевести это в таблицe базы данных и использовать ее.От куда были взяты номера статусов непонятно
 * @see ru.entity.model.document.Action  // DELETED since 0.3.2
 */

public enum DocumentAction {
    RETURN_TO_ARCHIVE("Вернуть в архив", 110),
    CHECK_IN_1("Зарегистрировать", 1),
    CHECK_IN_5("Зарегистрировать", 5),
    CHECK_IN_6("Зарегистрировать", 6),
    CHECK_IN_55("Зарегистрировать", 55),
    CHECK_IN_80("Зарегистрировать", 80),
    CHECK_IN_81("Зарегистрировать", 81),
    CHECK_IN_82("Зарегистрировать", 82),
    IN_ARCHIVE_99("В архив", 99),
    IN_ARCHIVE_90("В архив", 90),
    OUT_ARCHIVE("Изъять из архива", 100),
    SOURCE_DESTROY_120("Уничтожить оригинал документа", 120),
    SOURCE_DESTROY_125("Уничтожить оригинал документа", 125),
    DESTROY("Уничтожить документ", 115),
    REDIRECT_IN_OTHER_ARCHIVE_130("Направить в другой архив", 130),
    REDIRECT_IN_OTHER_ARCHIVE_135("Направить в другой архив", 135),
    SEND("Документ отправлен адресату", 200),
    RECIVE("Подтверждение о доставке документа получено", 210),
    CREATE("Документ создан", 0),
    REDIRECT_TO_EXECUTION_1("Направить на исполнение", 1),
    REDIRECT_TO_EXECUTION_2("Направить на исполнение", 2),
    REDIRECT_TO_EXECUTION_80("Направить на исполнение", 80),
    EXECUTE_80("Исполнен", 80),
    EXECUTE_90("Исполнен", 90),
    CHANGE_EXECUTION_DATE("Изменить срок исполнения", 1001),
    CHANGE_ACCESS_LEVEL("Изменить уровень допуска", 1002),
    REDIRECT_TO_CONSIDERATION_1("Направить на рассмотрение", 1),
    REDIRECT_TO_CONSIDERATION_2("Направить на рассмотрение", 2),
    REDIRECT_TO_AGREEMENT("Направить на согласование", 3),
    CANCEL_150("Отказаться", 150),
    CANCEL_25("Отказаться", 25),
    RETURN_TO_EDITING("Вернуть на редактирование", -10000),
    SHORT_CANCEL("Отказать", 25),
    EXECUTED("Исполненo", 2),
    CLOSE("Закрыть", 10),
    EXTRACT("Изъять", 100),
    RETURN("Вернуть", 110),
    REGISTRATION_CLOSE_PERIOD_551("Регистрация изменений закрытого периода", 551),
    REGISTRATION_CLOSE_PERIOD_661("Регистрация изменений закрытого периода", 661),
    REGISTRATION_CLOSE_PERIOD_552("Регистрация изменений закрытого периода", 552),
    NOT_AGREE("Не согласовано", 10001),
    DELEGATE_1001("Делегировать", 1001),
    DELEGATE_10002("Делегировать", 10002),
    AGREE("Согласовано", 10000);

    private final String name;
    private final int id;

    DocumentAction(String name, int id) {
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
