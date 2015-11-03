/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.alanmrace.jimzmlconverter.gui;

import java.io.IOException;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;

/**
 *
 * @author amr1
 */
public class DescriptiveButton extends VBox {

    StringProperty titleOn;
    StringProperty titleOff;
    StringProperty bodyOn;
    StringProperty bodyOff;
    
    BooleanProperty isOn;

    @FXML Label titleLabel;
    @FXML Label bodyLabel;
    
    public DescriptiveButton() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/DescriptiveButton.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        this.addEventFilter(MouseEvent.MOUSE_CLICKED,
                new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {
                        isOn.set(!isOn.get());
                    }
                }
        );
        
        titleOn = new SimpleStringProperty();
        titleOff = new SimpleStringProperty();
        bodyOn = new SimpleStringProperty();
        bodyOff = new SimpleStringProperty();
        
        isOn = new SimpleBooleanProperty();
        
        ChangeListener listener = new DescriptiveButtonChangeListener();
        
        titleOn.addListener(listener);
        titleOff.addListener(listener);
        bodyOn.addListener(listener);
        bodyOff.addListener(listener);
        isOn.addListener(listener);
        
        updateText();
    }

    private class DescriptiveButtonChangeListener implements ChangeListener {

        @Override
        public void changed(ObservableValue observable, Object oldValue, Object newValue) {
            updateText();
        }
    }

    private final String[] STYLE_CLASSES = {"descriptiveButtonOn", "descriptiveButtonOff"};
    
    private void updateText() {
        ObservableList<String> styleClasses = this.getStyleClass();
        styleClasses.removeAll(STYLE_CLASSES);
        
        if(isOn.get()) {
            styleClasses.add("descriptiveButtonOn");
            titleLabel.setText(titleOn.get());
            bodyLabel.setText(bodyOn.get());
        } else {
            styleClasses.add("descriptiveButtonOff");
            titleLabel.setText(titleOff.get());
            bodyLabel.setText(bodyOff.get());
        }
    }
    
    public StringProperty titleOnProperty() {
        return titleOn;
    }
    
    public String getTitleOn() {
        return titleOn.get();
    }
    
    public void setTitleOn(String value) {
        this.titleOn.set(value);
    }
    
    public StringProperty titleOffProperty() {
        return titleOff;
    }
    
    public String getTitleOff() {
        return titleOff.get();
    }
    
    public void setTitleOff(String value) {
        this.titleOff.set(value);
    }
    
    public StringProperty bodyOnProperty() {
        return bodyOn;
    }
    
    public String getBodyOn() {
        return bodyOn.get();
    }
    
    public void setBodyOn(String value) {
        bodyOn.set(value);
    }
    
    public StringProperty bodyOffProperty() {
        return bodyOff;
    }
    
    public String getBodyOff() {
        return bodyOff.get();
    }
    
    public void setBodyOff(String value) {
        bodyOff.set(value);
    }
    
    public BooleanProperty isOnProperty() {
        return isOn;
    }
    
    public boolean getIsOn() {
        return isOn.get();
    }
    
    public void setIsOn(boolean isOn) {
        this.isOn.set(isOn);
    }
}
