package ru.efive.wf.core.data;

import javax.persistence.Entity;
import javax.persistence.Table;

import ru.entity.model.mapped.Document;

/**
 * Базовый документ-лог транзакции
 */
@Entity
@Table(name = "wf_transactions")
public class TransactionLog extends Document {

    private static final long serialVersionUID = -3146342730189398598L;
}