[![Powered By Vaadin on Kotlin](http://vaadinonkotlin.eu/iconography/vok_badge.svg)](http://vaadinonkotlin.eu)
[![Build Status](https://travis-ci.org/mvysny/karibu-testing-spring.svg?branch=master)](https://travis-ci.org/mvysny/karibu-testing-spring)

# Karibu-Testing Demo project for Spring Boot

An example project which demonstrates the possibility to use the [Karibu-Testing](https://github.com/mvysny/karibu-testing)
Browserless Testing Framework with Spring Boot. No web server, no Selenium, and no TestBench, pure business logic testing.

Either Java or Kotlin might be used, or both.


Now testing of Vaadin application is as easy as:

Java
---
```java
    @Test
    public void createNewCustomer() {
        _click(_get(Button.class, spec -> spec.withCaption("New customer")));
        _setValue(_get(TextField.class, spec -> spec.withCaption("First name")), "Halk");
        _click(_get(Button.class, spec -> spec.withCaption("Save")));
        Grid<Customer> grid = _get(Grid.class);
        Stream<Customer> customerStream = grid.getDataProvider().fetch(new Query<>());
        assertTrue("Halk does not exist", customerStream.map(Customer::getFirstName).anyMatch("Halk"::equals));
    }
```

Kotlin
---
```kotlin
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

```
Workflow
========

To compile the entire project, run `./mvnw -C clean package` (or on Windows: `./mvnw.cmd -C clean package`).

To run the application, run `./mvnw spring-boot:run` and open [http://localhost:8080/](http://localhost:8080/).

To run the tests, run `./mvnw verify`.
