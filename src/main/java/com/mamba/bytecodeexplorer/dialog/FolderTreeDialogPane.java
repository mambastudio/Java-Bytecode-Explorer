/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mamba.bytecodeexplorer.dialog;

import com.mamba.bytecodeexplorer.watcher.treeitem.FileRefModel;
import com.mamba.bytecodeexplorer.watcher.treeitem.FileRefTreeItem;
import com.mamba.bytecodeexplorer.watcher.FileRef;
import java.io.File;
import java.io.IO;
import java.io.IOException;
import java.util.Optional;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.HBox;
import javafx.stage.DirectoryChooser;
import javafx.util.Callback;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.javafx.StackedFontIcon;

/**
 *
 * @author user
 */
public class FolderTreeDialogPane extends HBox {        
    private final DirectoryChooser directoryChooser = new DirectoryChooser();   
    
    @FXML
    ListView<FileRef> folderListView;
    
    @FXML
    TreeView<FileRefModel> folderExploreTreeView;    
    TreeItem<FileRefModel> folderExploreRootItem = new TreeItem<>(null);
    private final ObjectProperty<FileRef> folderExploreRootProperty = new SimpleObjectProperty();
    private final ObjectProperty<FileRef> folderExploreSelectedProperty = new SimpleObjectProperty();
    
    @FXML
    Label partialFolderLabel;
    @FXML
    Button partialFolderTransferButton;
    
    @FXML
    Button clearSelectedFolderButton;    
    @FXML
    TreeView<FileRef> folderSelectedTreeView;
    private final ObjectProperty<FileRef> folderSelectedProperty = new SimpleObjectProperty();
    
    @FXML
    ComboBox<FileRef> parentComboBox;
    
    Callback<FileRefModel, Node> graphicsFactory = (FileRefModel fileRef) -> {
        StackedFontIcon fontIcon = new StackedFontIcon();            
        if(fileRef.getRef().isDirectory() && fileRef.getRef().isDirectoryEmpty(".class")){
            FontIcon icon = new FontIcon("mdal-folder");
            fontIcon.getChildren().add(icon);
        }
        else if(fileRef.getRef().isDirectory()){
            FontIcon icon = new FontIcon("mdoal-create_new_folder");
            fontIcon.getChildren().add(icon);                
        }
        else if(!fileRef.getRef().isDirectory()){
            FontIcon icon = new FontIcon("mdoal-code");
            fontIcon.getChildren().add(icon);
        }
        return fontIcon;
    };
    
    public FolderTreeDialogPane() {
       
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(
                "FolderTreeDialog.fxml"));
        fxmlLoader.setRoot(FolderTreeDialogPane.this);
        fxmlLoader.setController(FolderTreeDialogPane.this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
        
        init();
    }
    
    private void init(){
        /****Folder listview section****/
        folderListView.setCellFactory(p -> {
            FontIcon folderIcon = new FontIcon("mdal-folder");
            return new ListCell<>() {
                @Override
                protected void updateItem(FileRef item, boolean empty) {
                    super.updateItem(item, empty);

                    if (item == null || empty) {
                        setGraphic(null);
                    } else {                        
                        setGraphic(folderIcon);
                        setText(item.toString());
                    }
                }
            };
        });
        
        folderListView.getSelectionModel().selectedItemProperty().addListener((o, ov, nv)->{            
            if(Optional.<FileRef>ofNullable(nv).isPresent())
                Platform.runLater(()->setFolderExplore(nv));
        });
        
        /****Folder explore treeview section****/
        folderExploreTreeView.setRoot(folderExploreRootItem);
        folderExploreTreeView.setShowRoot(false); 
        
        folderExploreTreeView.getSelectionModel().selectedItemProperty().addListener((o, ov, nv) ->{
            var f = Optional.<FileRef>ofNullable(nv.getValue().getRef());  
            switch(f.isPresent() && f.get().isLeaf()){
                case true -> folderExploreSelectedProperty.set(f.get().parent().get());
                case false -> folderExploreSelectedProperty.set(null);
            }                  
        });
        
        folderExploreSelectedProperty.addListener((o, ov, nv)->{
            var f = Optional.<FileRef>ofNullable(nv);           
            switch(f.isPresent()){ //set label for parent of selected classfile
                case true -> partialFolderLabel.setText(f.get().name());
                case false -> partialFolderLabel.setText("None");
            }     
        });
        
        partialFolderTransferButton.setOnAction(e -> {
            var f = Optional.<FileRef>ofNullable(folderExploreSelectedProperty.get());    
            if(f.isPresent())
                folderSelectedProperty.set(f.get());
        });
        
        /****Folder selected section****/
        clearSelectedFolderButton.setOnAction(e -> folderSelectedProperty.set(null));        
        folderSelectedProperty.addListener((o, ov, nv) -> {
            var f = Optional.<FileRef>ofNullable(nv);       
            switch(f.isPresent()){
                case true -> {
                    //Observable list of parent folders to select parent from
                    if(f.get() instanceof FileRef r && folderExploreRootProperty.get() instanceof FileRef root){
                        var parents = FXCollections.<FileRef>observableArrayList();

                        FileRef rr = r;
                        while(!root.equals(rr)){
                            parents.add(rr);
                            rr = rr.parent().get();
                        }
                        parents.add(root);
                        parentComboBox.getItems().setAll(parents.reversed());
                        parentComboBox.setValue(r);
                    }
                }
                case false -> parentComboBox.getItems().clear();
            }
        });
    }
    
    public void openFolder(ActionEvent e){        
        File selectedDirectory = directoryChooser.showDialog(this.sceneProperty().get().getWindow());
        
        if(selectedDirectory.isDirectory()){
            if(!folderListView.getItems().contains(new FileRef(selectedDirectory)))
                folderListView.getItems().add(new FileRef(selectedDirectory));
        }
    }
    
    private void setFolderExplore(FileRef folder){
        FileRefModel rootModel = new FileRefModel(folder, ".class");
        var rootItem = new FileRefTreeItem(
            rootModel,
            graphicsFactory,
            FileRefModel::getChildren
        );
        rootItem.setExpanded(true); // 
        
        folderExploreRootItem.getChildren().setAll(rootItem);  
        folderExploreRootProperty.set(folder);
    }
}
