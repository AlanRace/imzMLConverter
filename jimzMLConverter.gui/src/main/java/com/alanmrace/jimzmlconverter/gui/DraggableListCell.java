/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.alanmrace.jimzmlconverter.gui;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ListCell;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;

/**
 *
 * @author amr1
 * @param <T>
 */
public class DraggableListCell<T> extends ListCell<T> implements Serializable {

    protected static DataFormat dataFormat = new DataFormat("draggableContent");
    
    private class DraggableContent implements Serializable {
        public Integer[] indices;
        
        public DraggableContent(ObservableList<Integer> originalList) {
            this.indices = new Integer[originalList.size()];
            originalList.toArray(indices);
        }
    }
    
    public DraggableListCell() {
        setContentDisplay(ContentDisplay.TEXT_ONLY);
        setAlignment(Pos.CENTER_LEFT);

        this.setOnDragDetected(event -> {
            if (getItem() == null) {
                return;
            }

            Dragboard dragboard = startDragAndDrop(TransferMode.MOVE);
            ClipboardContent content = new ClipboardContent();
            
            content.putString(getItem().toString());
            content.put(dataFormat, new DraggableContent(getListView().getSelectionModel().getSelectedIndices()));
            dragboard.setContent(content);

            event.consume();
        });

        this.setOnDragOver(event -> {
            event.acceptTransferModes(TransferMode.MOVE);
            event.consume();
        });

        this.setOnDragEntered(event -> {
            if (event.getGestureSource() != this
                    && event.getDragboard().hasString()) {
                event.acceptTransferModes(TransferMode.MOVE);
            }

            event.consume();
        });

        this.setOnDragEntered(event -> {
            if (event.getGestureSource() != this) {
                //setOpacity(0.3);
                setBorder(new Border(new BorderStroke(Color.BLACK, 
                        BorderStrokeStyle.SOLID, CornerRadii.EMPTY, 
                        new BorderWidths(1, -1, -1, -1))));
            }
        });

        this.setOnDragExited(event -> {
            if (event.getGestureSource() != this) {
                //setOpacity(1);
                
                setBorder(Border.EMPTY);
            }
        });

        this.setOnDragDropped(event -> {
            if (getItem() == null) {
                return;
            }

            Dragboard dragboard = event.getDragboard();
            boolean success = false;

            if (dragboard.hasString()) {
                ObservableList<T> items = getListView().getItems();
                int draggedIndex = items.indexOf(dragboard.getString());
                int thisIndex = items.indexOf(getItem());
                
                // TODO: swap the underlying data
                //Object[] selectedItems = ((DraggableContent)dragboard.getContent(dataFormat)).list;
                
                DraggableContent content = (DraggableContent)dragboard.getContent(dataFormat);
                Integer[] indices = content.indices;
                List<T> itemsDragged = new ArrayList<>();
                
                for (Integer index : indices) {
                    itemsDragged.add(items.get(index));
                }
                
                List<T> itemsToShift = new ArrayList<>(items.subList(thisIndex, items.size()));
                itemsToShift.removeAll(itemsDragged);
                items.removeAll(itemsDragged);
                items.removeAll(itemsToShift);
                
                items.addAll(itemsDragged);
                items.addAll(itemsToShift);
                
                
                //items.set(draggedIndex, getItem());
                //items.set(thisIndex, );

                //List<String> itemscopy = new ArrayList<>(getListView().getItems());
                //getListView().getItems().setAll(itemscopy);

                success = true;
            }

            event.setDropCompleted(success);

            event.consume();
        });

        setOnDragDone(DragEvent::consume);
    }

    @Override
    protected void updateItem(T item, boolean empty) {
        super.updateItem(item, empty);

        if (empty || item == null) {
            setText(null);
        } else {
            setText(item.toString());
        }
    }
}
