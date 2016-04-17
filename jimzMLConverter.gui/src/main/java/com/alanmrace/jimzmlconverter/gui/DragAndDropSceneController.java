/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.alanmrace.jimzmlconverter.gui;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Logger;
import javafx.animation.FadeTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.TranslateTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * FXML Controller class
 *
 * @author amr1
 */
public class DragAndDropSceneController extends ManagedConverterScreenController {

    private static final Logger logger = Logger.getLogger(FXMLController.class.getName());

    final FileChooser fileChooser = new FileChooser();

    @FXML
    HBox dropBox;

    @FXML
    TextField dropLocation;

    @FXML
    HBox fileListBox;

    @FXML
    ListView fileListView;
    protected ObservableList<String> fileList;

    @FXML
    Button nextButton;

    protected Stage nextStage;

    boolean transitionOccured = false;

    /**
     * Initialises the controller class.
     *
     * @param url
     * @param rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        fileListView.setCellFactory(param -> new DraggableListCell<String>());
        fileListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }

    public void setNextStage(Stage nextStage) {
        this.nextStage = nextStage;
    }

    @FXML
    protected void fileBoxDragOver(DragEvent event) {
        Dragboard db = event.getDragboard();
        if (db.hasFiles()) {
            event.acceptTransferModes(TransferMode.COPY);
        } else {
            event.consume();
        }
    }

//    @FXML
//    protected void fileListOnDragDetected(MouseEvent event) {
//
//        int index = fileListView.getSelectionModel().getSelectedIndex();
//
//        if (index < 0) {
//            return;
//        }
//
//        ObservableList<String> items = fileListView.getItems();
//        
//        Dragboard dragboard = fileListView.startDragAndDrop(TransferMode.MOVE);
//        
//        ClipboardContent content = new ClipboardContent();
//        content.putString(fileListView.getSelectionModel().getSelectedItem().toString());
//    //    dragboard.setDragView(new Image(("http://images.clipartpanda.com/test-clip-art-ncB88RjcA.png")));
//        dragboard.setContent(content);
//
//        event.consume();
//    }
//
//    @FXML
//    protected void fileListOnDragOver(DragEvent event) {
//    }
//
//    @FXML
//    protected void fileListOnDragEntered(DragEvent event) {
//
//    }
//
//    @FXML
//    protected void fileListOnDragExited(DragEvent event) {
//
//    }
//
//    @FXML
//    protected void fileListOnDragDropped(DragEvent event) {
//
//    }
//
//    @FXML
//    protected void fileListOnDragDone(DragEvent event) {
//        event.consume();
//    }

    @FXML
    private void nextAction() {
        // Move onto the next screen
        
        getScreenManager().goToNextScreen();
    }

    private void performTransition() {
        if (transitionOccured) {
            return;
        }

        TranslateTransition moveDropBoxTransition = new TranslateTransition();
        moveDropBoxTransition.setDuration(Duration.millis(500));
        moveDropBoxTransition.setNode(dropBox);
        moveDropBoxTransition.setToY(10 - dropBox.getLayoutY());

//        moveDropBoxTransition.play();
        FadeTransition listViewFade = new FadeTransition(Duration.millis(500));
        listViewFade.setNode(fileListView);
        listViewFade.setFromValue(0.0);
        listViewFade.setToValue(1.0);
//        listViewFade.playFromStart();

        SequentialTransition fullTransition = new SequentialTransition(moveDropBoxTransition, listViewFade);
        fullTransition.play();

        // When the animation has finished set the opacity to be 1 for the remainder
        fullTransition.setOnFinished(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                fileListView.setStyle("-fx-opacity: 1.0;");
            }

        });

        transitionOccured = true;
    }

    @FXML
    public void fileBoxClicked(MouseEvent event) {
        File file = fileChooser.showOpenDialog(fileListBox.getScene().getWindow());

        if (file != null) {            
            getFileList().add(file.getAbsolutePath());

            if (!transitionOccured) {
                performTransition();
            }
        }
    }
    
    public ObservableList<String> getFileList() {
        if(fileList == null) {
            fileList = getScreenManager().getFileList();
            fileListView.setItems(fileList);
        }
        
        return fileList;
    }

    @FXML
    public void fileBoxDragDropped(DragEvent event) {
        Dragboard db = event.getDragboard();
        boolean success = false;

        if (db.hasFiles()) {            
            success = true;

            String filePath = null;
            for (File file : db.getFiles()) {
                filePath = file.getAbsolutePath();

                getFileList().add(filePath);
            }

            //fileListView.setItems(fileList);
            if (!transitionOccured) {
                performTransition();
            }
        }
        event.setDropCompleted(success);
        event.consume();
    }

}
