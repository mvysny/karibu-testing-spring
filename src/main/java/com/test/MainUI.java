package com.test;

import com.vaadin.icons.VaadinIcons;
import com.vaadin.server.VaadinRequest;
import com.vaadin.shared.ui.ValueChangeMode;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.ui.Button;
import com.vaadin.ui.Grid;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import org.springframework.util.StringUtils;

@SpringUI
public class MainUI extends UI {

    private final CustomerRepository repo;

    private final CustomerEditor editor;

    final Grid<Customer> grid;

    final TextField filter;

    private final Button addNewBtn;
    private final Button memoryLeak;

    @Override
    protected void init(VaadinRequest request) {

        // build layout
        HorizontalLayout actions = new HorizontalLayout(filter, addNewBtn, memoryLeak);
        VerticalLayout verticalLayout = new VerticalLayout(actions, grid, editor);
        verticalLayout.setSizeFull();
        setContent(verticalLayout);
        verticalLayout.setExpandRatio(grid, 1f);

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
        grid.asSingleSelect().addValueChangeListener(e -> editor.editCustomer(e.getValue()));

        // Instantiate and edit new Customer the new button is clicked
        addNewBtn.addClickListener(e -> editor.editCustomer(new Customer("", "")));

        // Listen changes made by the editor, refresh data from backend
        editor.setChangeHandler(() -> {
            editor.setVisible(false);
            listCustomers(filter.getValue());
        });

        // Initialize listing
        listCustomers(null);

        // Add a component with a listener without adding it to the design
        memoryLeak.addClickListener(e ->
        {
            Label label = new Label();
            addNewBtn.addClickListener(btnClick -> label.setValue(label.getValue() + "Clicked again!"));
        });

    }

    public void listCustomers(String filterText) {
        if (StringUtils.isEmpty(filterText)) {
            grid.setItems(repo.findAll());
        } else {
            grid.setItems(repo.findByLastNameStartsWithIgnoreCase(filterText));
        }

    }

    public MainUI(CustomerRepository repo, CustomerEditor editor) {
        this.repo = repo;
        this.editor = editor;
        this.grid = new Grid<>(Customer.class);
        this.filter = new TextField();
        this.addNewBtn = new Button("New customer", VaadinIcons.PLUS);
        this.memoryLeak = new Button("Memory Leak", VaadinIcons.BOMB);
    }
}