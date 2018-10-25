package com.test

import com.github.karibu.testing.*
import com.test.utils.HeapDump
import com.test.utils.SingletonBeanStoreRetrievalStrategy
import com.vaadin.spring.internal.UIScopeImpl
import com.vaadin.ui.Button
import com.vaadin.ui.Grid
import com.vaadin.ui.TextField
import org.junit.After
import org.junit.AfterClass
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.BeanFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.context.web.WebAppConfiguration
import kotlin.test.expect

@RunWith(SpringRunner::class)
@SpringBootTest
@WebAppConfiguration
@DirtiesContext
class ApplicationKotlinTest {

    @Autowired
    private val beanFactory: BeanFactory? = null

    @Before
    fun setup() {
        UIScopeImpl.setBeanStoreRetrievalStrategy(SingletonBeanStoreRetrievalStrategy())
        MockVaadin.setup { beanFactory!!.getBean(MainUI::class.java) }
    }

    @After
    fun tearDown() {
        MockVaadin.tearDown()
    }

    @Test
    fun createNewCustomer() {
        _get<Button> { caption = "New customer" }._click()
        _get<TextField> { caption = "First name" }._value = "Halk"
        _get<Button> { caption = "Save" }._click()
        val dataProvider = _get<Grid<Customer>> { }.dataProvider
        expect(true, "Halk exists: ${dataProvider._findAll()}") {
            dataProvider._findAll().any { it.firstName == "Halk" }
        }
    }

    companion object {
        @AfterClass
        @JvmStatic
        fun heapDump() {
            HeapDump.heapDump(ApplicationKotlinTest::class.java)
        }
    }
}
