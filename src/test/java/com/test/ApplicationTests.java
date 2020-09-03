package com.test;

import com.github.mvysny.kaributesting.v8.MockVaadin;
import com.vaadin.data.provider.Query;
import com.vaadin.spring.internal.UIScopeImpl;
import com.vaadin.ui.Button;
import com.vaadin.ui.Grid;
import com.vaadin.ui.TextField;

import com.vaadin.ui.UI;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;

import java.util.stream.Stream;

import static com.github.mvysny.kaributesting.v8.LocatorJ.*;
import static org.junit.Assert.assertTrue;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@WebAppConfiguration
@DirtiesContext
public class ApplicationTests {

    @Autowired
    private BeanFactory beanFactory;

    @BeforeEach
    public void setup() {
        UIScopeImpl.setBeanStoreRetrievalStrategy(new SingletonBeanStoreRetrievalStrategy());
        MockVaadin.setup(() -> beanFactory.getBean(MainUI.class));
    }

    @AfterEach
    public void tearDown() {
        MockVaadin.tearDown();
    }

    @Test
    public void createNewCustomer() {
        UI.getCurrent().getNavigator().navigateTo("");
        _click(_get(Button.class, spec -> spec.withCaption("New customer")));
        _setValue(_get(TextField.class, spec -> spec.withCaption("First name")), "Halk");
        _click(_get(Button.class, spec -> spec.withCaption("Save")));
        Grid<Customer> grid = _get(Grid.class);
        Stream<Customer> customerStream = grid.getDataProvider().fetch(new Query<>());
        assertTrue("Halk does not exist", customerStream.map(Customer::getFirstName).anyMatch("Halk"::equals));
    }
}
