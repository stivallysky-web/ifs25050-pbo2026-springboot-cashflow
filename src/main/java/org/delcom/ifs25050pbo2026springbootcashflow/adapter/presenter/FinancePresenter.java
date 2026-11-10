package org.delcom.ifs25050pbo2026springbootcashflow.adapter.presenter;

import org.delcom.ifs25050pbo2026springbootcashflow.domain.entity.Transaction;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Presenter untuk memformat data transaksi dan saldo menjadi teks siap tampil.
 */
public class FinancePresenter {
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    /** Memformat nominal uang dengan pemisah ribuan koma dan 2 desimal (contoh: Rp 1,000,000.00). */
    public String formatCurrency(double amount) {
        return String.format(Locale.US, "Rp %,.2f", amount);
    }

    /** Memformat waktu transaksi. */
    public String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "-";
        }
        return dateTime.format(DATE_FORMATTER);
    }

    /** Memformat satu baris transaksi sesuai acuan: [<id>] <keterangan> | Rp 1,000,000.00 | [<Pemasukan/Pengeluaran>] */
    public String formatTransaction(Transaction tx) {
        return String.format("[%d] %s | %s | [%s]",
                tx.getId(),
                tx.getDescription(),
                formatCurrency(tx.getAmount()),
                tx.getType().getLabel());
    }

    /** Memformat daftar transaksi beserta saldo akhir. */
    public String formatTransactionList(List<Transaction> transactions, double balance) {
        if (transactions.isEmpty()) {
            return "Belum ada catatan transaksi.\n" + formatBalance(balance);
        }
        String rows = transactions.stream()
                .map(this::formatTransaction)
                .collect(Collectors.joining("\n"));
        return rows + "\n" + formatBalance(balance);
    }

    /** Memformat teks saldo: Saldo: Rp 4,975,000.00 */
    public String formatBalance(double balance) {
        return "Saldo: " + formatCurrency(balance);
    }

    public String showAddSuccess(Transaction tx) {
        return "Berhasil menambahkan " + tx.getType().getLabel().toLowerCase() + ": " + formatTransaction(tx);
    }

    public String showUpdateSuccess(Transaction tx) {
        return "Berhasil memperbarui transaksi: " + formatTransaction(tx);
    }

    public String showDeleteSuccess(Transaction tx) {
        return "Berhasil menghapus transaksi: " + formatTransaction(tx);
    }

    public String showNotFoundError(int id) {
        return "[!] Transaksi dengan ID " + id + " tidak ditemukan.";
    }

    public String showValidationError(String message) {
        return "[!] " + message;
    }
}