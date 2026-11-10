package org.delcom.ifs25050pbo2026springbootcashflow.config;

import org.delcom.ifs25050pbo2026springbootcashflow.adapter.presenter.FinancePresenter;
import org.delcom.ifs25050pbo2026springbootcashflow.adapter.repository.TransactionRepository;
import org.delcom.ifs25050pbo2026springbootcashflow.domain.repository.ITransactionRepository;
import org.delcom.ifs25050pbo2026springbootcashflow.usecase.FinanceUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Composition root Spring Boot untuk menghubungkan dependency antar layer Clean Architecture.
 */
@Configuration
public class AppConfig {

    @Bean
    public ITransactionRepository transactionRepository() {
        return new TransactionRepository();
    }

    @Bean
    public FinanceUseCase financeUseCase(ITransactionRepository transactionRepository) {
        return new FinanceUseCase(transactionRepository);
    }

    @Bean
    public FinancePresenter financePresenter() {
        return new FinancePresenter();
    }
}