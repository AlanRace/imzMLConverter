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
import javafx.scene.input.MouseEvent;

/**
 * FXML Controller class
 *
 * @author amr1
 */
public class FileCombinationOptionsController extends ManagedScreenController {

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }    
    
    @FXML
    public void backAction() {
//        getScreenManager().loadScreen("test", "/fxml/FileCombinationOptions.fxml");
        getScreenManager().setScreen(MainApp.DRAG_AND_DROP_SCREEN, TransitionDirection.RIGHT_TO_LEFT);
    }
}
