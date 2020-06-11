package com.test;

import com.vaadin.icons.VaadinIcons;
import com.vaadin.navigator.View;
import com.vaadin.shared.ui.ValueChangeMode;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.ui.*;
import org.springframework.util.StringUtils;

/**
 * The main view.
 */
@SpringView(name = "")
public class CustomerView extends VerticalLayout implements View {
    private CustomerRepository repo;

    private final CustomerEditor editor;

    final Grid<Customer> grid;

    final TextField filter;

    private final Button addNewBtn;

    public CustomerView(CustomerRepository repo, CustomerEditor editor) {
        this.repo = repo;
        this.editor = editor;
        this.grid = new Grid<>(Customer.class);
        this.filter = new TextField();
        this.addNewBtn = new Button("New customer", VaadinIcons.PLUS);

        // build layout
        HorizontalLayout actions = new HorizontalLayout(filter, addNewBtn);
        addComponents(actions, grid, editor);
        setSizeFull();
        setExpandRatio(grid, 1f);

        grid.setColumns("id", "firstName", "lastName");
        grid.getColumn("id").setWidth(50).setExpandRatio(0);
        grid.getColumn("firstName").setExpandRatio(1);
        grid.getColumn("lastName").setExpandRatio(1);
        grid.setSizeFull();

        filter.setPlaceholder("Filter by last name");

        // Hook logic to components

        // Replace listing with filtered content when user changes filter
        filter.setValueChangeMode(ValueChangeMode.EAGER);
        filter.addValueChangeListener(e -> listCustomers(e.getValue()));

        // Connect selected Customer to editor or hide if none is selected
        grid.asSingleSelect().addValueChangeListener(e -> {
            editor.editCustomer(e.getValue());
        });

        // Instantiate and edit new Customer the new button is clicked
        addNewBtn.addClickListener(e -> editor.editCustomer(new Customer("", "")));

        // Listen changes made by the editor, refresh data from backend
        editor.setChangeHandler(() -> {
            editor.setVisible(false);
            listCustomers(filter.getValue());
        });

        // Initialize listing
        listCustomers(null);

    }

    public void listCustomers(String filterText) {
        if (StringUtils.isEmpty(filterText)) {
            grid.setItems(repo.findAll());
        } else {
            grid.setItems(repo.findByLastNameStartsWithIgnoreCase(filterText));
        }

    }
}
