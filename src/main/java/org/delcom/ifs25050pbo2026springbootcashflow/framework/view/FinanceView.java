package org.delcom.ifs25050pbo2026springbootcashflow.framework.view;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.delcom.ifs25050pbo2026springbootcashflow.adapter.presenter.FinancePresenter;
import org.delcom.ifs25050pbo2026springbootcashflow.domain.entity.SortOption;
import org.delcom.ifs25050pbo2026springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25050pbo2026springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25050pbo2026springbootcashflow.domain.exception.DomainException;
import org.delcom.ifs25050pbo2026springbootcashflow.usecase.FinanceUseCase;

import java.util.Comparator;
import java.util.List;

/**
 * Tampilan web Vaadin untuk aplikasi Catatan Keuangan (Cashflow).
 */
@Route("")
@PageTitle("Catatan Keuangan - Cashflow")
public class FinanceView extends VerticalLayout {

    private final FinanceUseCase financeUseCase;
    private final FinancePresenter financePresenter;

    private final Grid<Transaction> grid = new Grid<>(Transaction.class, false);
    private final TextField searchField = new TextField();
    private final ComboBox<SortOption> sortComboBox = new ComboBox<>();

    private final Span totalCountSpan = new Span("0");
    private final Span totalIncomeSpan = new Span("Rp 0.00");
    private final Span totalExpenseSpan = new Span("Rp 0.00");
    private final Span balanceSpan = new Span("Rp 0.00");

    public FinanceView(FinanceUseCase financeUseCase, FinancePresenter financePresenter) {
        this.financeUseCase = financeUseCase;
        this.financePresenter = financePresenter;

        if (com.vaadin.flow.component.UI.getCurrent() != null) {
            com.vaadin.flow.component.UI.getCurrent().getPage().executeJs(
                    "let m = document.querySelector('meta[name=\"description\"]');" +
                    "if (!m) { m = document.createElement('meta'); m.name = 'description'; document.head.appendChild(m); }" +
                    "m.content = 'Aplikasi Catatan Keuangan (Cashflow) untuk mengelola pemasukan dan pengeluaran.';"
            );
        }
        
        setSizeFull();
        setPadding(true);
        setSpacing(true);

        add(
                createHeader(),
                createSummarySection(),
                createToolbar(),
                createGrid()
        );

        refreshAll();
    }

    private Component createHeader() {
        H1 title = new H1("Aplikasi Catatan Keuangan (Cashflow)");
        title.getStyle()
                .set("margin", "0")
                .set("font-size", "1.75rem");
        return title;
    }

    private Component createSummarySection() {
        HorizontalLayout layout = new HorizontalLayout(
                createSummaryCard("Total Transaksi", totalCountSpan, "#1976d2"),
                createSummaryCard("Total Pemasukan", totalIncomeSpan, "#2e7d32"),
                createSummaryCard("Total Pengeluaran", totalExpenseSpan, "#c62828"),
                createSummaryCard("Saldo Saat Ini", balanceSpan, "#6a1b9a")
        );
        layout.setWidthFull();
        return layout;
    }

    private Div createSummaryCard(String label, Span valueSpan, String accentColor) {
        Span labelSpan = new Span(label);
        labelSpan.getStyle()
                .set("font-size", "0.85rem")
                .set("color", "#666");

        valueSpan.getStyle()
                .set("font-size", "1.35rem")
                .set("font-weight", "bold")
                .set("color", accentColor);

        Div card = new Div(labelSpan, valueSpan);
        card.getStyle()
                .set("display", "flex")
                .set("flex-direction", "column")
                .set("padding", "1rem")
                .set("border-radius", "8px")
                .set("background", "#f8f9fa")
                .set("border-left", "4px solid " + accentColor)
                .set("flex", "1");
        return card;
    }

    private Component createToolbar() {
        searchField.setPlaceholder("Cari keterangan transaksi...");
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.setClearButtonVisible(true);
        searchField.setWidth("280px");

        Button searchButton = new Button("Cari", VaadinIcon.SEARCH.create(), e -> searchTransactions());
        Button refreshButton = new Button("Muat Ulang", VaadinIcon.REFRESH.create(), e -> refreshAll());

        sortComboBox.setPlaceholder("Urutkan...");
        sortComboBox.setItems(SortOption.values());
        sortComboBox.setItemLabelGenerator(option -> switch (option) {
            case AMOUNT_ASC -> "Jumlah Terkecil";
            case AMOUNT_DESC -> "Jumlah Terbesar";
            case INCOME_FIRST -> "Pemasukan Dulu";
            case EXPENSE_FIRST -> "Pengeluaran Dulu";
        });
        sortComboBox.setClearButtonVisible(true);
        sortComboBox.addValueChangeListener(event -> {
            if (event.getValue() != null) {
                grid.setItems(financeUseCase.sortTransactions(event.getValue()));
            } else {
                grid.setItems(financeUseCase.getAllTransactions());
            }
        });

        Button addIncomeButton = new Button("Tambah Pemasukan", VaadinIcon.PLUS.create(),
                e -> openAddDialog(TransactionType.INCOME));
        addIncomeButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);

        Button addExpenseButton = new Button("Tambah Pengeluaran", VaadinIcon.MINUS.create(),
                e -> openAddDialog(TransactionType.EXPENSE));
        addExpenseButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);

        HorizontalLayout toolbar = new HorizontalLayout(
                searchField, searchButton, refreshButton, sortComboBox, addIncomeButton, addExpenseButton
        );
        toolbar.setWidthFull();
        toolbar.setAlignItems(Alignment.CENTER);
        return toolbar;
    }

    private Component createGrid() {
        grid.addColumn(Transaction::getId)
                .setHeader("ID")
                .setSortable(true)
                .setComparator(Comparator.comparingInt(Transaction::getId))
                .setWidth("80px")
                .setFlexGrow(0);

        grid.addColumn(Transaction::getDescription)
                .setHeader("Keterangan")
                .setSortable(true)
                .setComparator(Comparator.comparing(Transaction::getDescription, String.CASE_INSENSITIVE_ORDER))
                .setFlexGrow(2);

        grid.addColumn(tx -> financePresenter.formatCurrency(tx.getAmount()))
                .setHeader("Jumlah")
                .setSortable(true)
                .setComparator(SortOption.AMOUNT_ASC.comparator())
                .setFlexGrow(1);

        grid.addComponentColumn(tx -> {
                    Span badge = new Span(tx.getType().getLabel());
                    badge.getStyle()
                            .set("padding", "0.25rem 0.6rem")
                            .set("border-radius", "12px")
                            .set("font-size", "0.8rem")
                            .set("font-weight", "600")
                            .set("color", tx.isIncome() ? "#1b5e20" : "#b71c1c")
                            .set("background", tx.isIncome() ? "#e8f5e9" : "#ffebee");
                    return badge;
                })
                .setHeader("Tipe")
                .setSortable(true)
                .setComparator(SortOption.INCOME_FIRST.comparator())
                .setWidth("150px")
                .setFlexGrow(0);

        grid.addColumn(tx -> financePresenter.formatDateTime(tx.getCreatedAt()))
                .setHeader("Waktu")
                .setSortable(true)
                .setComparator(Comparator.comparing(Transaction::getCreatedAt))
                .setWidth("180px")
                .setFlexGrow(0);

        grid.addComponentColumn(tx -> {
                    Button deleteButton = new Button("Hapus", VaadinIcon.TRASH.create(), e -> openDeleteDialog(tx));
                    deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL);
                    return deleteButton;
                })
                .setHeader("Aksi")
                .setWidth("130px")
                .setFlexGrow(0);

        grid.addItemDoubleClickListener(event -> openEditDialog(event.getItem()));
        grid.setSizeFull();
        return grid;
    }

    private void openAddDialog(TransactionType type) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Tambah " + type.getLabel());

        TextField descField = new TextField("Keterangan");
        descField.setWidthFull();
        descField.setPlaceholder("Contoh: Gaji bulanan / Makan siang");

        NumberField amountField = new NumberField("Jumlah (Rp)");
        amountField.setWidthFull();
        amountField.setPlaceholder("Contoh: 1000000");
        amountField.setMin(0);

        VerticalLayout formLayout = new VerticalLayout(descField, amountField);
        formLayout.setPadding(false);
        dialog.add(formLayout);

        Button saveButton = new Button("Simpan", VaadinIcon.CHECK.create(), e -> {
            try {
                double amount = amountField.getValue() != null ? amountField.getValue() : 0.0;
                Transaction saved = (type == TransactionType.INCOME)
                        ? financeUseCase.addIncome(descField.getValue(), amount)
                        : financeUseCase.addExpense(descField.getValue(), amount);
                showSuccess(financePresenter.showAddSuccess(saved));
                dialog.close();
                refreshAll();
            } catch (DomainException ex) {
                showError(financePresenter.showValidationError(ex.getMessage()));
            }
        });
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelButton = new Button("Batal", VaadinIcon.CLOSE.create(), e -> dialog.close());
        dialog.getFooter().add(cancelButton, saveButton);
        dialog.open();
    }

    private void openEditDialog(Transaction tx) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Ubah Transaksi #" + tx.getId());

        TextField descField = new TextField("Keterangan");
        descField.setWidthFull();
        descField.setValue(tx.getDescription());

        NumberField amountField = new NumberField("Jumlah (Rp)");
        amountField.setWidthFull();
        amountField.setValue(tx.getAmount());

        VerticalLayout formLayout = new VerticalLayout(descField, amountField);
        formLayout.setPadding(false);
        dialog.add(formLayout);

        Button saveButton = new Button("Simpan", VaadinIcon.CHECK.create(), e -> {
            try {
                double amount = amountField.getValue() != null ? amountField.getValue() : 0.0;
                Transaction updated = financeUseCase.updateTransaction(tx.getId(), descField.getValue(), amount);
                showSuccess(financePresenter.showUpdateSuccess(updated));
                dialog.close();
                refreshAll();
            } catch (DomainException ex) {
                showError(financePresenter.showValidationError(ex.getMessage()));
            }
        });
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelButton = new Button("Batal", VaadinIcon.CLOSE.create(), e -> dialog.close());
        dialog.getFooter().add(cancelButton, saveButton);
        dialog.open();
    }

    private void openDeleteDialog(Transaction tx) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Konfirmasi Hapus");
        dialog.add(new Paragraph("Apakah Anda yakin ingin menghapus transaksi: " + financePresenter.formatTransaction(tx) + "?"));

        Button confirmButton = new Button("Hapus", VaadinIcon.TRASH.create(), e -> {
            try {
                Transaction removed = financeUseCase.removeTransaction(tx.getId());
                showSuccess(financePresenter.showDeleteSuccess(removed));
                dialog.close();
                refreshAll();
            } catch (DomainException ex) {
                showError(financePresenter.showValidationError(ex.getMessage()));
            }
        });
        confirmButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);

        Button cancelButton = new Button("Batal", VaadinIcon.CLOSE.create(), e -> dialog.close());
        dialog.getFooter().add(cancelButton, confirmButton);
        dialog.open();
    }

    private void searchTransactions() {
        try {
            List<Transaction> results = financeUseCase.searchTransactions(searchField.getValue());
            grid.setItems(results);
        } catch (DomainException ex) {
            showError(financePresenter.showValidationError(ex.getMessage()));
        }
    }

    private void refreshAll() {
        searchField.clear();
        sortComboBox.clear();
        List<Transaction> all = financeUseCase.getAllTransactions();
        grid.setItems(all);
        updateSummary(all.size());
    }

    private void updateSummary(int totalCount) {
        totalCountSpan.setText(String.valueOf(totalCount));
        totalIncomeSpan.setText(financePresenter.formatCurrency(financeUseCase.getTotalIncome()));
        totalExpenseSpan.setText(financePresenter.formatCurrency(financeUseCase.getTotalExpense()));
        balanceSpan.setText(financePresenter.formatCurrency(financeUseCase.getBalance()));
    }

    private void showSuccess(String message) {
        Notification notification = Notification.show(message, 3500, Notification.Position.BOTTOM_START);
        notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }

    private void showError(String message) {
        Notification notification = Notification.show(message, 4000, Notification.Position.BOTTOM_START);
        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }
}