/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mamba.bytecodeexplorer.dialog;

import com.mamba.bytecodeexplorer.tree.model.FileRefModel;
import com.mamba.bytecodeexplorer.tree.item.RootTreeItem;
import com.mamba.bytecodeexplorer.file.type.FileRef;
import com.mamba.bytecodeexplorer.file.type.RealFile;
import com.mamba.bytecodeexplorer.tree.model.FileRefInfo;
import java.io.File;
import java.io.IOException;
import java.util.Optional;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.DirectoryChooser;
import org.kordamp.ikonli.javafx.FontIcon;

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
    TreeView<FileRefInfo> folderSelectedTreeView;
    private final ObjectProperty<FileRef> folderSelectedProperty = new SimpleObjectProperty();
    
    @FXML
    ComboBox<FileRef> parentComboBox;
    TreeItem<FileRefInfo> folderInfoRootItem = new TreeItem<>(); 
       
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
                setFolderExplore(nv);
        });
        
        /****Folder explore treeview section****/
        folderExploreTreeView.setRoot(folderExploreRootItem);
        folderExploreTreeView.setShowRoot(false); 
        
        folderExploreTreeView.getSelectionModel().selectedItemProperty().addListener((o, ov, nv) ->{
            var f = Optional.<FileRef>ofNullable(nv.getValue().ref());  
            if(!())
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
        folderSelectedProperty.addListener((_, _, nv) -> {
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
                                        
                    var fileInfoTreeModel = RootTreeItem.ofFileRef(new FileRefInfo(f.get(), ".class"));
                    fileInfoTreeModel.setExpanded(true);
                    
                    //Setup the treeview
                    folderInfoRootItem.setValue(new FileRefInfo(f.get()));
                    folderInfoRootItem.setExpanded(true);
                    folderSelectedTreeView.setRoot(folderInfoRootItem);
                    folderInfoRootItem.getChildren().setAll(fileInfoTreeModel.rootTreeItem());
                    fileInfoTreeModel.rootModel().reloadSystemChildren();                  
                }
                case false -> clearSelectedFolderInfo();
            }
        });
        FontIcon folderInfoRootIcon = new FontIcon("mdal-extension");
        folderInfoRootIcon.getStyleClass().clear();
        folderInfoRootIcon.setIconColor(Color.LIGHTGREEN);
        folderInfoRootIcon.setIconSize(20);
        folderInfoRootItem.setGraphic(folderInfoRootIcon);
        parentComboBox.getSelectionModel().selectedItemProperty().addListener((_, _, nv)->{
            var f = Optional.<FileRef>ofNullable(nv);       
            if(f.isPresent()){
                folderInfoRootItem.setValue(new FileRefInfo(f.get()));    
            }
        });
    }
    
    public void openFolder(ActionEvent e){        
        var selectedDirectory = Optional.ofNullable(directoryChooser.showDialog(this.sceneProperty().get().getWindow()));
        
        if(selectedDirectory.isPresent())
            if(selectedDirectory.get().isDirectory() && selectedDirectory.get() instanceof File f){
                if(!folderListView.getItems().contains(new RealFile(f)))
                    folderListView.getItems().add(new RealFile(f));
            }
    }
    
    private void setFolderExplore(FileRef folder){        
        var fileTreeModel = RootTreeItem.ofFileRef(new FileRefModel(folder, ".class"));
        fileTreeModel.setExpanded(true);
        folderExploreRootItem.getChildren().setAll(fileTreeModel.rootTreeItem());  
        folderExploreRootProperty.set(folder);
    }
    
    public void clearSelectedFolderInfo(){
        parentComboBox.getItems().clear();
        folderInfoRootItem.setValue(null);
        folderInfoRootItem.getChildren().clear();
    }
    
    public Optional<FolderTreePair> getSelectedFolderTreePair(){
        return switch(folderInfoRootItem.valueProperty().isNotNull().get()){
            case true -> Optional.of(new FolderTreePair(
                                folderInfoRootItem.getValue().getFileRef(), 
                                folderInfoRootItem.getChildren().get(0).getValue().getFileRef()));
            case false -> Optional.empty();
        };
    }
}
