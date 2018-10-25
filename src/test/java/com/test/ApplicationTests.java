package com.test;

import com.github.karibu.testing.MockVaadin;
import com.vaadin.data.provider.Query;
import com.vaadin.spring.internal.UIScopeImpl;
import com.vaadin.ui.Button;
import com.vaadin.ui.Grid;
import com.vaadin.ui.TextField;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.util.Comparator;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static com.github.karibu.testing.LocatorJ.*;
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
        _click(_get(Button.class, spec -> spec.withCaption("New customer")));
        _setValue(_get(TextField.class, spec -> spec.withCaption("First name")), "Halk");
        _click(_get(Button.class, spec -> spec.withCaption("Save")));
        Grid<Customer> grid = _get(Grid.class);
        Stream<Customer> customerStream = grid.getDataProvider().fetch(new Query<>());
        assertTrue(customerStream.map(Customer::getFirstName).anyMatch("Halk"::equals));
    }

    @Test
    public void createNew2Customers() {
        Predicate<String> classesToWatch = name -> name.contains("vaadin") || name.startsWith("com.test.");

        _click(_get(Button.class, spec -> spec.withCaption("New customer")));
        _setValue(_get(TextField.class, spec -> spec.withCaption("First name")), "Halk");
        _click(_get(Button.class, spec -> spec.withCaption("Save")));

        HeapInfo.tryGC();
        HeapInfo heapInfo1 = new HeapInfo().classStatistics(classesToWatch);

        _click(_get(Button.class, spec -> spec.withCaption("New customer")));
        _setValue(_get(TextField.class, spec -> spec.withCaption("First name")), "Van Helsing");
        _click(_get(Button.class, spec -> spec.withCaption("Save")));

        HeapInfo.tryGC();
        HeapInfo heapInfo2 = new HeapInfo().classStatistics(classesToWatch);

        System.out.println("****** Class usage differences START *****");
        heapInfo2.delta(heapInfo1).printNicely(null, null, System.out::println);
        System.out.println("******* Class usage differences END ******");

        Grid<Customer> grid = _get(Grid.class);
        Stream<Customer> customerStream = grid.getDataProvider().fetch(new Query<>());
        assertTrue(customerStream.map(Customer::getFirstName).anyMatch("Halk"::equals));
        customerStream = grid.getDataProvider().fetch(new Query<>());
        assertTrue(customerStream.map(Customer::getFirstName).anyMatch("Van Helsing"::equals));
    }

    @AfterClass
    public static void heapDump() {

        HeapDump.heapDump(ApplicationTests.class);
    }

}
