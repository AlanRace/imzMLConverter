/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.alanmrace.jimzmlconverter.gui;

import com.alanmrace.jimzmlconverter.Converter;
import com.alanmrace.jimzmlconverter.WatersRAWTomzMLConverter;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;

/**
 * FXML Controller class
 *
 * @author amr1
 */
public class WatersDetailsSceneController extends DetailsSceneController {

    @FXML DescriptiveButton maldiButton;
    @FXML DescriptiveButton desiButton;
    
    @FXML Label filenameText;
    
    @FXML TextField outputFilenameTextField;
    
    @FXML HBox maldiHBox;
    @FXML HBox desiHBox;
    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }    
    
    public void setFilenameText(String text) {
        filenameText.setText("Waters specific conversion options for:\n" + text);
        
        if(text != null)
            outputFilenameTextField.setText(text.replace(".RAW", ".imzML").replace(".raw", ".imzML"));
    }
    
    @FXML
    protected void maldiButtonAction(MouseEvent event) {
        maldiButton.setIsOn(true);
        maldiHBox.setVisible(true);
        
        desiButton.setIsOn(false);
        desiHBox.setVisible(false);
    }
    
    @FXML
    protected void desiButtonAction(MouseEvent event) {
        desiButton.setIsOn(true);
        desiHBox.setVisible(true);
        
        maldiButton.setIsOn(false);
        maldiHBox.setVisible(false);
    }

    @Override
    public Converter getConverter() {
        WatersRAWTomzMLConverter converter = new WatersRAWTomzMLConverter();
        
        return (Converter) converter;
    }
}
