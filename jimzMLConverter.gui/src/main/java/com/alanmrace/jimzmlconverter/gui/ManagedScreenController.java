/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.alanmrace.jimzmlconverter.gui;

import javafx.fxml.Initializable;

/**
 *
 * @author amr1
 */
public abstract class ManagedScreenController  implements Initializable {
    protected ScreenManager manager;
    
    public void setScreenManager(ScreenManager manager) {
        this.manager = manager;
    }
    
    protected ScreenManager getScreenManager() {
        return manager;
    }
}
