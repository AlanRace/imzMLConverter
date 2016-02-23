/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.alanmrace.jimzmlconverter.gui;

import javafx.animation.FadeTransition;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 *
 * @author amr1
 */
public class JavaFX_FadeInOut extends Application {
 
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
     
    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("http://java-buddy.blogspot.com/");
        final Button btn = new Button();
        btn.setText("Say 'Hello World'");
        btn.setOnAction(new EventHandler<ActionEvent>() {
 
            @Override
            public void handle(ActionEvent event) {
                System.out.println("Hello World!");
            }
        });
         
        StackPane root = new StackPane();
        root.getChildren().add(btn);
        Scene scene = new Scene(root, 300, 250);
         
        scene.setOnMouseEntered(new EventHandler<MouseEvent>(){
 
            public void handle(MouseEvent mouseEvent){
                FadeTransition fadeTransition 
                        = new FadeTransition(Duration.millis(500), btn);
                fadeTransition.setFromValue(0.0);
                fadeTransition.setToValue(1.0);
                fadeTransition.play();
            }
        });
         
        scene.setOnMouseExited(new EventHandler<MouseEvent>(){
 
            public void handle(MouseEvent mouseEvent){
                FadeTransition fadeTransition 
                        = new FadeTransition(Duration.millis(500), btn);
                fadeTransition.setFromValue(1.0);
                fadeTransition.setToValue(0.0);
                fadeTransition.play();
            }
        });
         
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
