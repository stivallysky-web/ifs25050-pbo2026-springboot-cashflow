package org.delcom.stiventpbo2026springbootcashflow.domain.entity;

/**
 * Tipe transaksi keuangan: Pemasukan (INCOME) atau Pengeluaran (EXPENSE).
 */
public enum TransactionType {
    INCOME("Pemasukan"),
    EXPENSE("Pengeluaran");

    private final String label;

    TransactionType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}