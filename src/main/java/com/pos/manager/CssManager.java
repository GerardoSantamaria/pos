package com.pos.manager;

import atlantafx.base.theme.PrimerLight;
import atlantafx.base.theme.Theme;
import javafx.application.Application;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class CssManager {

    private Theme currentTheme;

    public String getCurrentTheme() {
        return currentTheme.getUserAgentStylesheet();
    }

    public void setCurrentTheme(Theme currentTheme) {
        this.currentTheme = currentTheme;
    }

    public void setDefaultTheme() {
        setCurrentTheme(new PrimerLight());
        Application.setUserAgentStylesheet(this.getCurrentTheme());
    }
}
