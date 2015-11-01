package com.alanmrace.jimzmlconverter.gui;

import com.alanmrace.jimzmlconverter.ImzMLConverter;
import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.FileChooser;

public class FXMLController implements Initializable {

    private static final Logger log = Logger.getLogger(FXMLController.class.getName());

    final FileChooser fileChooser = new FileChooser();

    @FXML
    private Label label;

    @FXML
    private TextArea logTextArea;

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
                sb.append(" ").append(record.getMessage()).append('\n');
                return sb.toString();
            }
            
        };

        // Output all log based information to the text area
        Logger.getLogger("com.alanmrace").addHandler(new Handler() {

            @Override
            public void publish(final LogRecord record) {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        logTextArea.appendText(formatter.format(record));
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
    }
}
