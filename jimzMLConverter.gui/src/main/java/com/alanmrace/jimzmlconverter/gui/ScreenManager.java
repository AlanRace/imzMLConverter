/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.alanmrace.jimzmlconverter.gui;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

/**
 *
 * @author amr1
 */
public class ScreenManager extends StackPane {

    private static final Logger logger = Logger.getLogger(ScreenManager.class.getName());

    private final HashMap<String, Node> screens;
    private final HashMap<String, ManagedScreenController> screenControllers;
    private Node currentScreen;

    private double fadeInTime;
    private double fadeOutTime;

//    public class SceneLoad {
//        private Scene screen;
//        private double fadeInTime;
//    } 
    
    public enum TransitionDirection {
        LEFT_TO_RIGHT,
        RIGHT_TO_LEFT
    }

    
    public ScreenManager() {
        super();

        this.screens = new HashMap<>();
        this.screenControllers = new HashMap<>();
        this.fadeInTime = 1000;
        this.fadeOutTime = 1000;
    }

    protected void addScreen(String name, Node screen, ManagedScreenController screenController) {
        screens.put(name, screen);
        
        screenControllers.put(name, screenController);
    }

    public Node getScreen(String screenName) {
        return screens.get(screenName);
    }

    public ManagedScreenController getScreenController(String screenName) {
        return screenControllers.get(screenName);
    }
    
    public Node getCurrentScreen() {
        return currentScreen;
    }

    public boolean loadScreen(String name, String resource) {
        // Check if the current sceen exists
        if (screens.get(name) != null) {
            return true;
        }

        return loadScreenFresh(name, resource);
    }

    public boolean loadScreenFresh(String name, String resource) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(resource));
            Parent root = loader.load();

            if (loader.getController() instanceof ManagedScreenController) {
                ManagedScreenController screenController = ((ManagedScreenController) loader.getController());

                screenController.setScreenManager(this);
                addScreen(name, root, screenController);
                
                return true;
            } else {
                logger.log(Level.SEVERE, "Incompatible screen controller used, must extend ManagedScreenController");
            }
        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
        }

        return false;
    }
    
    public boolean unloadScreen(String name) {
        boolean removal;
        
        if(screens.remove(name) != null) {
            if(screenControllers.remove(name) != null) {
                removal = true;
            } else {
                logger.log(Level.SEVERE, MessageFormat.format("No such screen controller to remove {0}", name));
                removal = false;
            }
        } else {
            logger.log(Level.SEVERE, MessageFormat.format("No such screen to remove {0}", name));
            removal = false;
        }
            
        return removal;    
    }
    
    public boolean setScreen(final String name) {
        return setScreen(name, TransitionDirection.LEFT_TO_RIGHT);
    }

    public boolean setScreen(final String name, TransitionDirection direction) {
//        System.out.println("Setting screen to be " + name);

        // Check that the screen has previously been loaded
        if (screens.get(name) != null) {   
            Node screenToAdd = screens.get(name);
            
            // To avoid exceptions when clicking during animation, only add the child if it currently doesn't exist
            if(!getChildren().contains(screenToAdd))
                getChildren().add(screenToAdd);

            if (currentScreen != null) {
                final Node screenToRemove = currentScreen;

                screenToAdd.layoutXProperty().set(currentScreen.getScene().getWidth());

                TranslateTransition removalTransition = new TranslateTransition(Duration.millis(fadeOutTime), currentScreen);
                TranslateTransition insertTransition = new TranslateTransition(Duration.millis(fadeInTime), screenToAdd);

                // When the removal transition has completed, remove the screen 
                removalTransition.setOnFinished(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        getChildren().remove(screenToRemove);
                    }
                });
                
                // Set up the direction of the transition
                if(direction == TransitionDirection.LEFT_TO_RIGHT) {
                    removalTransition.setFromX(0);
                    removalTransition.setToX(-currentScreen.getScene().getWidth());
                    
                    insertTransition.setFromX(currentScreen.getScene().getWidth());
                    insertTransition.setToX(0);
                } else if(direction == TransitionDirection.RIGHT_TO_LEFT) {
                    removalTransition.setFromX(0);
                    removalTransition.setToX(currentScreen.getScene().getWidth());
                    
                    insertTransition.setFromX(-currentScreen.getScene().getWidth());
                    insertTransition.setToX(0);
                }
                
                // Play the transitions
                removalTransition.play();
                insertTransition.play();
            }

            currentScreen = screenToAdd;

            return true;
        } else {
            logger.log(Level.SEVERE, "Screen has not been loaded and so cannot be shown");

            return false;
        }
    }

    public double getFadeInTime() {
        return fadeInTime;
    }

    public void setFadeInTime(double fadeInTime) {
        this.fadeInTime = fadeInTime;
    }
    
    public double getFadeOutTime() {
        return fadeOutTime;
    }
    
    public void setFadeOutTime(double fadeOutTime) {
        this.fadeOutTime = fadeOutTime;
    }
}
