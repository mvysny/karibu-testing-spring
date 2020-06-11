package com.test;

import com.github.mvysny.kaributesting.v8.MockVaadin;
import com.vaadin.data.provider.Query;
import com.vaadin.spring.internal.UIScopeImpl;
import com.vaadin.ui.Button;
import com.vaadin.ui.Grid;
import com.vaadin.ui.TextField;

import com.vaadin.ui.UI;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.util.stream.Stream;

import static com.github.mvysny.kaributesting.v8.LocatorJ.*;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest
@WebAppConfiguration
@DirtiesContext
public class ApplicationTests {

    @Autowired
    private BeanFactory beanFactory;

    @Before
    public void setup() {
        UIScopeImpl.setBeanStoreRetrievalStrategy(new SingletonBeanStoreRetrievalStrategy());
        MockVaadin.setup(() -> beanFactory.getBean(MainUI.class));
    }

    @After
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
