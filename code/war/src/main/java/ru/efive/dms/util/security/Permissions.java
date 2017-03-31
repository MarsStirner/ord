package ru.efive.dms.util.security;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Author: Upatov Egor <br>
 * Date: 12.11.2014, 12:59 <br>
 * Company: Korus Consulting IT <br>
 * Description: <br>
 */
public class Permissions {

    /**
     * Все права RWX
     */
    public static final Permissions ALL_PERMISSIONS = new Permissions(true, true, true);
    /**
     * Права RW
     */
    public static final Permissions RW_PERMISSIONS = new Permissions(true, true, false);
    /**
     * Только R право
     */
    public static final Permissions R_PERMISSIONS = new Permissions(true, false, false);
    /**
     * Все права RWX
     */
    public static final Permissions EMPTY_PERMISSIONS = new Permissions();
    /**
     * Унутреннее хранилише прав для экземпляра класса
     */
    private Set<Permission> value;

    public Permissions() {
        this.value = new HashSet<>(3);
    }

    public Permissions(boolean read, boolean write, boolean execute) {
        this();
        if (read) {
            value.add(Permission.READ);
        }
        if (write) {
            value.add(Permission.WRITE);
        }
        if (execute) {
            value.add(Permission.EXECUTE);
        }
    }

    public boolean hasAllPermissions() {
        return value.size() == Permission.values().length;
    }

    public void mergePermissions(Permissions toMergeWith) {
        if (toMergeWith != null) {
            value.addAll(toMergeWith.getValue());
        }
    }

    /**
     * Получить весь список прав
     *
     * @return список прав
     */
    public Set<Permission> getValue() {
        return value;
    }

    /**
     * Проверяет есть ли право
     *
     * @param checkTo право для проверки
     * @return true- есть, false - право отсутствует
     */
    public boolean hasPermission(Permission checkTo) {
        return value.contains(checkTo);
    }

    /**
     * Добавить какое-то право
     *
     * @param toAdd право для добавления
     * @return
     */
    public boolean addPermission(Permission toAdd) {
        return value.add(toAdd);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("{");
        for (Iterator<Permission> iterator = value.iterator(); iterator.hasNext(); ) {
            Permission permission = iterator.next();
            sb.append(permission);
            if (iterator.hasNext()) {
                sb.append(", ");
            }
        }
        sb.append('}');
        return sb.toString();
    }

    /**
     * Допустимый список прав
     */
    public enum Permission {
        READ,
        WRITE,
        EXECUTE
    }
}
