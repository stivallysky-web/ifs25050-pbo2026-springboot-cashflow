package org.delcom.stiventpbo2026springbootcashflow.domain.entity;

import java.util.Comparator;

/**
 * Pilihan kriteria pengurutan transaksi keuangan.
 */
public enum SortOption {
    AMOUNT_ASC(Comparator.comparingDouble(Transaction::getAmount)),
    AMOUNT_DESC(Comparator.comparingDouble(Transaction::getAmount).reversed()),
    INCOME_FIRST(Comparator.comparing(Transaction::isIncome).reversed()),
    EXPENSE_FIRST(Comparator.comparing(Transaction::isExpense).reversed());

    private final Comparator<Transaction> comparator;

    SortOption(Comparator<Transaction> comparator) {
        this.comparator = comparator;
    }

    public Comparator<Transaction> comparator() {
        return comparator;
    }
}