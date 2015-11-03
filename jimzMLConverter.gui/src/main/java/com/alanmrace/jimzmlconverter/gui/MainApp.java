package com.alanmrace.jimzmlconverter.gui;

import com.alanmrace.jimzmlconverter.ImzMLConverter;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;


public class MainApp extends Application {
    
    // Need to store a list of Scenes and Controllers so that it is possible to swap back and forward between them

    public static String DRAG_AND_DROP_SCREEN = "dragAndDrop";
    public static String DRAG_AND_DROP_SCREEN_FXML = "/fxml/DragAndDropScene.fxml";
    
    public static String FILE_COMBINATION_SCREEN = "fileCombination";
    public static String FILE_COMBINATION_SCREEN_FXML = "/fxml/FileCombinationOptions.fxml";
    
    @Override
    public void start(Stage stage) throws Exception {
        stage.setTitle("imzMLConverter " + ImzMLConverter.version);
        
        ScreenManager manager = new ScreenManager();
        
        // Load all of the relevant screens
        manager.loadScreen(DRAG_AND_DROP_SCREEN, DRAG_AND_DROP_SCREEN_FXML);
        manager.loadScreen(FILE_COMBINATION_SCREEN, FILE_COMBINATION_SCREEN_FXML);
        
        manager.setScreen(DRAG_AND_DROP_SCREEN);
        
        // Load the initial drag and drop scene
        //FXMLLoader rootLoader = new FXMLLoader(getClass().getResource("/fxml/DragAndDropScene.fxml"));
        //Parent root = rootLoader.load();
        //DragAndDropSceneController rootController = rootLoader.getController();
        
        Group root = new Group();
        root.getChildren().addAll(manager);
        
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    /**
     * The main() method is ignored in correctly deployed JavaFX application.
     * main() serves only as fallback in case the application can not be
     * launched through deployment artifacts, e.g., in IDEs with limited FX
     * support. NetBeans ignores main().
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

}
