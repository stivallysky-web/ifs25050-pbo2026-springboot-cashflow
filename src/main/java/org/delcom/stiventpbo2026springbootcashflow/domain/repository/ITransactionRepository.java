package org.delcom.stiventpbo2026springbootcashflow.domain.repository;

import org.delcom.stiventpbo2026springbootcashflow.domain.entity.Transaction;
import org.delcom.stiventpbo2026springbootcashflow.domain.entity.TransactionType;

public interface ITransactionRepository extends IRepository<Transaction, Integer> {
    Transaction save(String description, double amount, TransactionType type);
}