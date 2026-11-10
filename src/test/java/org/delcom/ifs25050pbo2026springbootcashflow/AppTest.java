package org.delcom.ifs25050pbo2026springbootcashflow;

import org.delcom.ifs25050pbo2026springbootcashflow.adapter.presenter.FinancePresenter;
import org.delcom.ifs25050pbo2026springbootcashflow.domain.entity.SortOption;
import org.delcom.ifs25050pbo2026springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25050pbo2026springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.ifs25050pbo2026springbootcashflow.domain.exception.ValidationException;
import org.delcom.ifs25050pbo2026springbootcashflow.domain.repository.ITransactionRepository;
import org.delcom.ifs25050pbo2026springbootcashflow.usecase.FinanceUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AppTest {

    @Autowired
    private ITransactionRepository transactionRepository;

    @Autowired
    private FinanceUseCase financeUseCase;

    @Autowired
    private FinancePresenter financePresenter;

    @Test
    void contextLoads() {
        assertNotNull(transactionRepository);
        assertNotNull(financeUseCase);
        assertNotNull(financePresenter);
    }

    @Test
    void testCashflowOperationsAndFormatting() throws ValidationException, EntityNotFoundException {
        Transaction income = financeUseCase.addIncome("Gaji Bulanan", 5000000);
        Transaction expense = financeUseCase.addExpense("Beli Buku", 25000);

        assertEquals(4975000.0, financeUseCase.getBalance(), 0.001);
        assertEquals("Rp 5,000,000.00", financePresenter.formatCurrency(income.getAmount()));
        assertEquals("Saldo: Rp 4,975,000.00", financePresenter.formatBalance(financeUseCase.getBalance()));

        List<Transaction> searchResult = financeUseCase.searchTransactions("buku");
        assertFalse(searchResult.isEmpty());

        List<Transaction> sortedAsc = financeUseCase.sortTransactions(SortOption.AMOUNT_ASC);
        assertEquals(25000.0, sortedAsc.get(0).getAmount(), 0.001);

        financeUseCase.removeTransaction(expense.getId());
        financeUseCase.removeTransaction(income.getId());
    }

    @Test
    void testValidationErrors() {
        assertThrows(ValidationException.class, () -> financeUseCase.addIncome("", 10000));
        assertThrows(ValidationException.class, () -> financeUseCase.addExpense("Makan", -5000));
        assertThrows(EntityNotFoundException.class, () -> financeUseCase.removeTransaction(99999));
    }
}