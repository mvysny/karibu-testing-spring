package com.test;

import com.vaadin.server.VaadinRequest;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.spring.navigator.SpringNavigator;
import com.vaadin.ui.UI;
import org.springframework.beans.factory.annotation.Autowired;

@SpringUI
public class MainUI extends UI {

    @Autowired
    private SpringNavigator navigator;

    @Override
    protected void init(VaadinRequest request) {
        navigator.init(this, this);
        setNavigator(navigator);
    }
}
