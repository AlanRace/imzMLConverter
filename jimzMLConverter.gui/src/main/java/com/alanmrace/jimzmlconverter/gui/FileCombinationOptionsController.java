package com.alanmrace.jimzmlconverter.gui;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import com.alanmrace.jimzmlconverter.gui.ScreenManager.TransitionDirection;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;

/**
 * FXML Controller class
 *
 * @author amr1
 */
public class FileCombinationOptionsController extends ManagedConverterScreenController {

    @FXML DescriptiveButton parallelButton;
    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
    }    
    
    @FXML
    public void backAction() {
        //getScreenManager().setScreen(MainApp.DRAG_AND_DROP_SCREEN, TransitionDirection.RIGHT_TO_LEFT);
        getScreenManager().goToPreviousScreen();
    }
    
    @FXML public void nextAction() {
        //getScreenManager().setScreen(MainApp.WATERS_DETAILS_SCREEN);
        getScreenManager().goToNextScreen();
    }
}
