/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.alanmrace.jimzmlconverter.gui;

import com.alanmrace.jimzmlconverter.Converter;
import com.alanmrace.jimzmlconverter.ImzMLConverter;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;

/**
 * FXML Controller class
 *
 * @author amr1
 */
public abstract class DetailsSceneController extends ManagedConverterScreenController {

    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }    
    
    
    @FXML
    protected void nextAction() {
        this.getScreenManager().goToNextScreen();
    }
    
    @FXML
    protected void backAction() {
        this.getScreenManager().goToPreviousScreen();
    }
    
    public abstract Converter getConverter();
}
