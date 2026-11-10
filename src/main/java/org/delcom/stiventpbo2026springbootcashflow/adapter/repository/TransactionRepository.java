package org.delcom.stiventpbo2026springbootcashflow.adapter.repository;

import org.delcom.stiventpbo2026springbootcashflow.domain.entity.Transaction;
import org.delcom.stiventpbo2026springbootcashflow.domain.entity.TransactionType;
import org.delcom.stiventpbo2026springbootcashflow.domain.repository.ITransactionRepository;

/**
 * Implementasi konkret repository transaksi dengan penomoran ID otomatis.
 */
public class TransactionRepository extends InMemoryRepository<Transaction, Integer> implements ITransactionRepository {
    private int idCounter = 0;

    public TransactionRepository() {
        super(Transaction::getId);
    }

    @Override
    public Transaction save(String description, double amount, TransactionType type) {
        Transaction tx = new Transaction(nextId(), description, amount, type);
        return super.save(tx);
    }

    private int nextId() {
        return ++idCounter;
    }
}