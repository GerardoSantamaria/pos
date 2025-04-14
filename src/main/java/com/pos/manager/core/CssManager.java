package com.pos.manager.core;

import atlantafx.base.theme.PrimerDark;
import atlantafx.base.theme.PrimerLight;
import atlantafx.base.theme.Theme;
import javafx.application.Application;
import org.springframework.stereotype.Component;

@Component
public class CssManager {

    private Theme currentTheme;

    public String getCurrentTheme() {
        return currentTheme.getUserAgentStylesheet();
    }

    private void setCurrentTheme(Theme currentTheme) {
        this.currentTheme = currentTheme;
    }

    public void setLightTheme() {
        setCurrentTheme(new PrimerLight());
        Application.setUserAgentStylesheet(this.getCurrentTheme());
    }

    public void setDarkTheme() {
        setCurrentTheme(new PrimerDark());
        Application.setUserAgentStylesheet(this.getCurrentTheme());
    }

}
