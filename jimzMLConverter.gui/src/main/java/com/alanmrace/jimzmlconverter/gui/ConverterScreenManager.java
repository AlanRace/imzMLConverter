/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.alanmrace.jimzmlconverter.gui;

import com.alanmrace.jimzmlconverter.Converter;
import java.io.File;
import java.util.ArrayList;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;

/**
 *
 * @author amr1
 */
public class ConverterScreenManager extends ScreenManager {
    
    public static String DRAG_AND_DROP_SCREEN = "dragAndDrop";
    public static String DRAG_AND_DROP_SCREEN_FXML = "/fxml/DragAndDropScene.fxml";
    
    public static String FILE_COMBINATION_SCREEN = "fileCombination";
    public static String FILE_COMBINATION_SCREEN_FXML = "/fxml/FileCombinationOptions.fxml";
    
//    public static String WATERS_DETAILS_SCREEN = "watersDetails";
    public static String WATERS_DETAILS_SCREEN_FXML = "/fxml/WatersDetailsScene.fxml";
    
    // Need the file list
    protected ObservableList<String> fileList = FXCollections.observableArrayList();
    
    protected ArrayList<String> filesHandled = new ArrayList<>();
    
    // List of already dealt with files
    protected ArrayList<Converter> converters = new ArrayList<>();
    
    // Remaining files is the difference between the two lists
    
    protected ArrayList<String> screens = new ArrayList<>();
    protected int currentScreenIndex;
    
    public ConverterScreenManager() {
        
    }
    
    public ObservableList<String> getFileList() {
        return fileList;
    }
    
    public void loadScreens() {
        // Load all of the relevant screens
        loadScreen(DRAG_AND_DROP_SCREEN, DRAG_AND_DROP_SCREEN_FXML);
        loadScreen(FILE_COMBINATION_SCREEN, FILE_COMBINATION_SCREEN_FXML);
        
        screens.add(DRAG_AND_DROP_SCREEN);
        screens.add(FILE_COMBINATION_SCREEN);
    }
    
    public void goToNextScreen() {
        int prevScreen = currentScreenIndex;
        
        // Check which files have not been dealt with yet        
        FilteredList<String> mzMLFilesNotHandled = new FilteredList<>(fileList, param -> param.toLowerCase().contains(".mzml") && !filesHandled.contains(param));
        FilteredList<String> watersFilesNotHandled = new FilteredList<>(fileList, param -> {
            return param.toLowerCase().contains(".raw") && !filesHandled.contains(param) && (new File(param)).isDirectory(); 
        });
        FilteredList<String> thermoFilesNotHandled = new FilteredList<>(fileList, param -> {
            return param.toLowerCase().contains(".raw") && !filesHandled.contains(param) && !(new File(param)).isDirectory(); 
        });
        FilteredList<String> sciexFilesNotHandled = new FilteredList<>(fileList, param -> param.toLowerCase().contains(".wiff") && !filesHandled.contains(param));
        
        if(watersFilesNotHandled.size() > 0) {
            String filename = watersFilesNotHandled.get(0);
            
            loadScreen(filename, WATERS_DETAILS_SCREEN_FXML);
            WatersDetailsSceneController controller = (WatersDetailsSceneController)this.getScreenController(filename);
            
            if(controller != null)
                controller.setFilenameText(filename);
            
            screens.add(filename);
            filesHandled.add(filename);
        }

        currentScreenIndex++;
        
        if(currentScreenIndex >= screens.size())
            currentScreenIndex = screens.size()-1;        
        
        if(prevScreen != currentScreenIndex)
            setScreen(screens.get(currentScreenIndex), TransitionDirection.LEFT_TO_RIGHT);
    }
    
    public void goToPreviousScreen() {
        int prevScreen = currentScreenIndex;
        
        currentScreenIndex--;
        
        if(currentScreenIndex < 0)
            currentScreenIndex = 0;
        
        if(prevScreen != currentScreenIndex)
            setScreen(screens.get(currentScreenIndex), TransitionDirection.RIGHT_TO_LEFT);
    }
}
