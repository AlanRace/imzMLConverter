package com.alanmrace.jimzmlconverter.gui;

import com.alanmrace.jimzmlconverter.ImzMLConverter;
import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker.State;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class FXMLController implements Initializable {

    private static final Logger logger = Logger.getLogger(FXMLController.class.getName());

    final FileChooser fileChooser = new FileChooser();
    
    String displayLog = "";

    @FXML
    private Label label;

    @FXML
    private WebView logWebView;

    @FXML
    private void browseButtonAction(ActionEvent event) {
        File file = fileChooser.showOpenDialog(label.getScene().getWindow());

        if (file != null) {
            label.setText(file.getAbsolutePath());
        }
    }

    @FXML
    private void convertButtonAction(ActionEvent event) {
        final String[] args = {label.getText()};

        Thread t1 = new Thread(new Runnable() {
            @Override
            public void run() {
                // code goes here.
                ImzMLConverter.main(args);
            }
        });
        t1.start();

    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Create a logging formatter
        final Formatter formatter = new Formatter() {

            @Override
            public String format(LogRecord record) {
                StringBuilder sb = new StringBuilder();
                sb.append(String.format("%1$tY/%1$tm/%1$td %1$tH:%1$tM:%1$tS", new java.util.Date(record.getMillis())));
                sb.append(" [").append(record.getLevel()).append("]");
                sb.append(" ").append(record.getMessage());
                return sb.toString();
            }
            
        };

        logWebView.getEngine().load(FXMLController.class.getResource("/html/logview.html").toExternalForm());
        
        // Output all log based information to the text area
        Logger.getLogger("com.alanmrace").addHandler(new Handler() {

            @Override
            public void publish(final LogRecord record) {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        String colour;
                
                        if(record.getLevel().equals(Level.SEVERE)) {
                            colour = "red";
                        } else if(record.getLevel().equals(Level.WARNING)) {
                            colour = "orange";
                        } else {
                            colour = "black";
                        }   
                        
                        Document document = logWebView.getEngine().getDocument();
                        Element logElement = document.getElementById("log");
                        
                        Element font = document.createElement("font");
                        logElement.appendChild(font);
                        font.setAttribute("color", colour);
                        
                        font.appendChild(document.createTextNode(formatter.format(record)));
                        
                        logElement.appendChild(document.createElement("br"));
                        
                        // Auto Scroll as the log is updated
                        logWebView.getEngine().executeScript("window.scrollTo(0, document.body.scrollHeight);");
                    }
                });
            }

            @Override
            public void flush() {
            }

            @Override
            public void close() {
            }
        });
        
        // When the logging page has been loaded add 
        logWebView.getEngine().getLoadWorker().stateProperty().addListener(new ChangeListener<State>() {
            @Override
            public void changed(ObservableValue observableValue, State state, State newState) {
                if (newState.equals(State.SUCCEEDED)) {
                    logger.log(Level.INFO, "imzMLConverter " + ImzMLConverter.version);
                }
            }
        });
    }
}
