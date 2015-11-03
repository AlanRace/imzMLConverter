/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.alanmrace.jimzmlconverter.gui;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.animation.FadeTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.TranslateTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * FXML Controller class
 *
 * @author amr1
 */
public class DragAndDropSceneController extends ManagedScreenController {

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
    protected final static ObservableList<String> fileList = FXCollections.observableArrayList();

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
        fileListView.setItems(fileList);
    }

    public void setNextStage(Stage nextStage) {
        this.nextStage = nextStage;
    }

    @FXML
    public void fileBoxDragOver(DragEvent event) {
        Dragboard db = event.getDragboard();
        if (db.hasFiles()) {
            event.acceptTransferModes(TransferMode.COPY);
        } else {
            event.consume();
        }
    }

    @FXML
    private void nextAction() {
        getScreenManager().loadScreen("test", "/fxml/FileCombinationOptions.fxml");
        getScreenManager().setScreen("test");
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
            fileList.add(file.getAbsolutePath());

            if (!transitionOccured) {
                performTransition();
            }
        }
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

                fileList.add(filePath);
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
