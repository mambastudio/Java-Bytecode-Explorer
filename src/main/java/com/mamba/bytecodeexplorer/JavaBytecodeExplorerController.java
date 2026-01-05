/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package com.mamba.bytecodeexplorer;

import module java.base;

import atlantafx.base.theme.NordDark;
import atlantafx.base.theme.NordLight;
import com.mamba.bytecodeexplorer.classanalysis.ClassAnalysis;
import com.mamba.bytecodeexplorer.classanalysis.ClassAspect;
import com.mamba.bytecodeexplorer.classanalysis.ExecutableInfo;
import com.mamba.bytecodeexplorer.dialog.FolderTreeDialog;
import com.mamba.bytecodeexplorer.dialog.FolderTreePair;
import com.mamba.bytecodeexplorer.file.FileRefWatcher;
import com.mamba.bytecodeexplorer.file.FileRefWatcher.FileEventListener.FileEvent.*;
import com.mamba.bytecodeexplorer.file.type.FileRef;
import com.mamba.bytecodeexplorer.file.type.RealFile;
import com.mamba.bytecodeexplorer.tree.item.FileRefTreeItem;
import com.mamba.bytecodeexplorer.tree.item.RootTreeItem;
import com.mamba.bytecodeexplorer.tree.model.ClassRefMeta;
import com.mamba.bytecodeexplorer.tree.model.ClassRefModel;
import com.mamba.mambaui.modal.ModalDialogs.InformationDialog;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.application.Application;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TreeView;
import javafx.scene.layout.StackPane;

/**
 * FXML Controller class
 *
 * @author user
 */
public class JavaBytecodeExplorerController implements Initializable {
       
    @FXML
    TabPane tabPaneView;
    
    @FXML
    TreeView<ClassRefModel> fileTreeView;
    
    @FXML
    StackPane root;
    
    @FXML
    ComboBox<ClassAspect> classAspectCombo; 
    
    @FXML
    ListView<String> classAspectListView;
    
    FileRefTreeItem<ClassRefModel>  rootT;
        
    ObjectProperty<ClassAnalysis> currentAnalysisProperty = new SimpleObjectProperty();
    
    FileRefWatcher watcher = new FileRefWatcher();
    FolderTreeDialog folderTreeDialog = new FolderTreeDialog();
    InformationDialog aboutDialog = new InformationDialog("Java bytecode viewer to assess code ops realtime");
    
    ClassViewer classViewer = new ClassViewer();
    
    /**
     * Initializes the controller class.
     * @param url
     * @param rb
     */
               
    @Override
    public void initialize(URL url, ResourceBundle rb) {       
        tabPaneView.getTabs().add(classViewer.tab());
        
        root.getChildren().addAll(folderTreeDialog, aboutDialog);
        
        classAspectCombo.setItems(FXCollections.observableArrayList(ClassAspect.values()));
        classAspectCombo.getSelectionModel().select(ClassAspect.METHODS);
        classAspectCombo.selectionModelProperty().addListener((o, ov, nv)->{
            if(!(currentAnalysisProperty.get() instanceof ClassAnalysis ca))
                return;
            
            switch(nv.getSelectedItem()){
                case METHODS -> {
                    classAspectListView.setItems(FXCollections.observableArrayList(ca.getMethodNames()));
                }                    
            }
        });
        
        currentAnalysisProperty.addListener((o, ov, nv) -> {
            if(nv instanceof ClassAnalysis ca){
                switch(classAspectCombo.getValue()){
                    case METHODS -> {
                        var listMethods = ExecutableInfo.of(ca.classModel());
                        var listMethodNames = new ArrayList<String>();
                        
                        for(ExecutableInfo info : listMethods)
                            listMethodNames.add(info.formatExecutable());
                        
                        classAspectListView.setItems(FXCollections.observableArrayList(listMethodNames));
                    }                    
                }
                classViewer.show(ca); //where we display class bytecode
            }    
        });
        
        
        fileTreeView.getSelectionModel().selectedItemProperty().addListener((o, ov, nv)->{
            var opt = Optional.ofNullable(nv); 
            if(opt.isEmpty())
                return;
            //throws exception if f does not exist (recompiled situation of monitored file)
            if(opt.get().getValue().ref() instanceof RealFile f && f.exists() && f.isLeaf()){ 
                try {                  
                    currentAnalysisProperty.set(new ClassAnalysis(f.path()));           
                } catch (IOException ex) {
                    System.getLogger(JavaBytecodeExplorerController.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
                }
            }
        });
    }    
        
    public void open(ActionEvent e){
        folderTreeDialog.showAndWait(result -> {               
            result.ifPresentOrElse(r -> {                
                setTreeItem(r); 
            }, 
            () -> IO.println("No result"));
        });          
    }    
    
    //Update UI tree for classes files
    private void setTreeItem(FolderTreePair f){
        if(f instanceof FolderTreePair(FileRef ancestorFolder, FileRef parentFolder, _)){  
            
            //remove file watcher from current model
            if(rootT != null){
                watcher.unwatchTree(rootT.getValue());
                IO.println("Are there folders being monitored: " +watcher.statesCountWatched());
            }
            
            var rootVirtualClass = new ClassRefModel();
            
            var ancestor = new ClassRefModel(ancestorFolder, true);   
            var addedParentToAncestor = ancestor.addChild(new ClassRefModel(parentFolder, true)); //It might fail, hence we resolve one possibility below
            
            if(!addedParentToAncestor){ //did it fail?
                if(ancestorFolder.equals(parentFolder)) //why? is it because ancestor is same as parent folder
                    rootVirtualClass.addChild(new ClassRefModel(parentFolder, true)); //add to rootvirtual if true
            }
            else
                rootVirtualClass.addChild(ancestor); //since parent added to ancestor, add now to virtual root
            
            
            
            //TODO: Replace with FileRefWatcher2, since we aren't doing recursive monitoring automatically (hierarchy folders might not be direct children)
            watcher.watchTree(rootVirtualClass, e->{
                switch(e){
                    case Created(var parent, var child) -> {
                        var pi = new RealFile(parent);   
                        var fi = new RealFile(child);
                        var parentPresent = rootVirtualClass.findInTree(pi); 
                        
                        //notice we search parent, metamap we search child
                        //since in metamap we saved folders only, we filter automatically in the if
                        if(parentPresent.isPresent() && fi.isFileExtension(".class") && parentPresent.tree().classChildrenIntended()){                           
                            parentPresent.tree().addChild(new ClassRefModel(fi, false));
                        }
                              
                    }
                    case Deleted(var parent, var child) -> {    
                        var pi = new RealFile(parent);   
                        var fi = new RealFile(child);   
                        var parentPresent = rootVirtualClass.findInTree(pi);                          
                        if(parentPresent.isPresent() && fi.isFileExtension(".class") && parentPresent.tree().classChildrenIntended()){
                            parentPresent.tree().removeChild(fi);
                        }
                    }
                    case Modified(var _, var _) -> {}
                    case Overflow(var _) -> {}
                    case KeyInvalid(var parent) -> {
                        var fi = new RealFile(parent);                          
                        var result = rootVirtualClass.findInTree(fi);                          
                        if(result.isPresent()){ 
                            var item = rootT.find(new ClassRefModel(fi, false)); 
                            watcher.metaMap().put(parent, new ClassRefMeta(result.parent(), fi, item.get().isExpanded(), result.tree().classChildrenIntended()));
                            rootVirtualClass.remove(fi);
                        }
                    }
                    case DirectoryRevalidated(var parent) -> {
                        var uiMeta = watcher.metaMap().get(parent);                         
                        if(uiMeta instanceof ClassRefMeta(var pi, var fi, var _, var classChildrenIntended)){ 
                            var a = new ClassRefModel(fi, classChildrenIntended); 
                            var p = rootVirtualClass.findInTree(pi);                            
                            p.tree().addChild(a);

                            var treeItemOpt = this.rootT.find(a);
                            var ti = treeItemOpt.get();                            
                            ti.setExpanded(true);                    
                            
                            watcher.metaMap().remove(parent);
                        }
                    }
                }
            });
            
            this.rootT = RootTreeItem.<ClassRefModel>ofFileRef(rootVirtualClass).expandAll().rootTreeItem();
                        
            fileTreeView.setRoot(rootT);
            fileTreeView.setShowRoot(false);  
        }
    }
    
    public void about(ActionEvent e){
        aboutDialog.showAndWait(result -> {});
    }    
  
    public Optional<Tab> getTabByTitle(TabPane tabPane, String title) {
        var tabs = tabPane.getTabs();
        for (Tab tab : tabs) {
            if (tab.getText() != null && tab.getText().equals(title)) {
                return Optional.of(tab);
            }
        }
        return Optional.empty();
    }
    
    
   
    public void darkTheme(ActionEvent e){
        Application.setUserAgentStylesheet(new NordDark().getUserAgentStylesheet());
    }
    
    public void lightTheme(ActionEvent e){
        Application.setUserAgentStylesheet(new NordLight().getUserAgentStylesheet());
    }
}
