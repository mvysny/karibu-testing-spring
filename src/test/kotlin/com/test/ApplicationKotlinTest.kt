package com.test

import com.github.mvysny.kaributesting.v8.*
import com.vaadin.spring.internal.UIScopeImpl
import com.vaadin.ui.Button
import com.vaadin.ui.Grid
import com.vaadin.ui.TextField
import com.vaadin.ui.UI
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.BeanFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.context.web.WebAppConfiguration
import kotlin.test.expect

@ExtendWith(SpringExtension::class)
@SpringBootTest
@WebAppConfiguration
@DirtiesContext
class ApplicationKotlinTest {

    @Autowired
    private lateinit var beanFactory: BeanFactory

    @BeforeEach
    fun setup() {
        UIScopeImpl.setBeanStoreRetrievalStrategy(SingletonBeanStoreRetrievalStrategy())
        MockVaadin.setup({ beanFactory.getBean(MainUI::class.java) })
    }

    @AfterEach
    fun tearDown() {
        MockVaadin.tearDown()
    }

    @Test
    fun smokeTest() {
        expect<Class<*>>(MainUI::class.java) { UI.getCurrent().javaClass }
        _expectOne<Grid<Customer>>()
    }

    @Test
    fun createNewCustomer() {
        _get<Button> { caption = "New customer" }._click()
        _get<TextField> { caption = "First name" }._value = "Halk"
        _get<Button> { caption = "Save" }._click()
        val dataProvider = _get<Grid<Customer>> { }.dataProvider
        expect(true, "Halk does not exist: ${dataProvider._findAll()}") {
            dataProvider._findAll().any { it.firstName == "Halk" }
        }
    }
}
