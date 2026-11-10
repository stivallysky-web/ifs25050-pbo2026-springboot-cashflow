package org.delcom.ifs25050pbo2026springbootcashflow.usecase;

import org.delcom.ifs25050pbo2026springbootcashflow.domain.entity.SortOption;
import org.delcom.ifs25050pbo2026springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25050pbo2026springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25050pbo2026springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.ifs25050pbo2026springbootcashflow.domain.exception.ValidationException;
import org.delcom.ifs25050pbo2026springbootcashflow.domain.repository.ITransactionRepository;

import java.util.List;
import java.util.Optional;

/**
 * Use case yang menangani logika bisnis aplikasi catatan keuangan (cashflow).
 */
public class FinanceUseCase {
    private final ITransactionRepository transactionRepository;

    public FinanceUseCase(ITransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    /** Mengambil semua daftar transaksi. */
    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAll();
    }

    /** Menambah transaksi pemasukan baru. */
    public Transaction addIncome(String description, double amount) throws ValidationException {
        validateInput(description, amount);
        return transactionRepository.save(description.trim(), amount, TransactionType.INCOME);
    }

    /** Menambah transaksi pengeluaran baru. */
    public Transaction addExpense(String description, double amount) throws ValidationException {
        validateInput(description, amount);
        return transactionRepository.save(description.trim(), amount, TransactionType.EXPENSE);
    }

    /** Mengubah keterangan dan jumlah transaksi berdasarkan ID. */
    public Transaction updateTransaction(int id, String newDescription, double newAmount)
            throws ValidationException, EntityNotFoundException {
        validateInput(newDescription, newAmount);
        Transaction tx = findOrThrow(id);
        tx.changeDescription(newDescription.trim());
        tx.changeAmount(newAmount);
        transactionRepository.update(tx);
        return tx;
    }

    /** Menghapus transaksi berdasarkan ID. */
    public Transaction removeTransaction(int id) throws EntityNotFoundException {
        Transaction tx = findOrThrow(id);
        transactionRepository.deleteById(id);
        return tx;
    }

    /** Mencari transaksi berdasarkan kata kunci pada keterangan (case-insensitive). */
    public List<Transaction> searchTransactions(String keyword) throws ValidationException {
        if (keyword == null || keyword.trim().isEmpty()) {
            throw new ValidationException("Kata kunci pencarian tidak boleh kosong.");
        }
        String lowerKeyword = keyword.trim().toLowerCase();
        return transactionRepository.findBy(tx ->
                tx.getDescription().toLowerCase().contains(lowerKeyword)
        );
    }

    /** Mengurutkan transaksi sesuai kriteria SortOption. */
    public List<Transaction> sortTransactions(SortOption option) {
        return transactionRepository.findAll().stream()
                .sorted(option.comparator())
                .toList();
    }

    /** Menghitung total pemasukan. */
    public double getTotalIncome() {
        return transactionRepository.findAll().stream()
                .filter(Transaction::isIncome)
                .mapToDouble(Transaction::getAmount)
                .sum();
    }

    /** Menghitung total pengeluaran. */
    public double getTotalExpense() {
        return transactionRepository.findAll().stream()
                .filter(Transaction::isExpense)
                .mapToDouble(Transaction::getAmount)
                .sum();
    }

    /** Menghitung saldo saat ini (total pemasukan - total pengeluaran). */
    public double getBalance() {
        return getTotalIncome() - getTotalExpense();
    }

    private void validateInput(String description, double amount) throws ValidationException {
        if (description == null || description.trim().isEmpty()) {
            throw new ValidationException("Keterangan transaksi tidak boleh kosong.");
        }
        if (Double.isNaN(amount) || Double.isInfinite(amount) || amount <= 0) {
            throw new ValidationException("Jumlah transaksi harus lebih besar dari 0.");
        }
    }

    private Transaction findOrThrow(int id) throws EntityNotFoundException {
        Optional<Transaction> found = transactionRepository.findById(id);
        if (found.isEmpty()) {
            throw new EntityNotFoundException(id);
        }
        return found.get();
    }
}