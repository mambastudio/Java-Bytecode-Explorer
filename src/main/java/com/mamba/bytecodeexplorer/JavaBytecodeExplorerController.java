/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package com.mamba.bytecodeexplorer;

import atlantafx.base.theme.NordDark;
import atlantafx.base.theme.NordLight;
import com.mamba.bytecodeexplorer.dialog.FolderTreeDialog;
import com.mamba.bytecodeexplorer.dialog.FolderTreePair;
import com.mamba.bytecodeexplorer.tree.model.FileRefModel;
import com.mamba.bytecodeexplorer.file.FileRefWatcher;
import com.mamba.bytecodeexplorer.file.FileRefWatcher.FileEventListener.FileEvent.*;
import com.mamba.bytecodeexplorer.file.type.FileRef;
import com.mamba.bytecodeexplorer.file.type.RealFile;
import com.mamba.bytecodeexplorer.tree.item.FileRefTreeItem;
import com.mamba.bytecodeexplorer.tree.item.RootTreeItem;
import com.mamba.bytecodeexplorer.tree.model.ClassRefModel;
import com.mamba.mambaui.modal.ModalDialogs.InformationDialog;
import java.io.IO;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TreeView;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import jfx.incubator.scene.control.richtext.CodeArea;
import org.kordamp.ikonli.javafx.FontIcon;

/**
 * FXML Controller class
 *
 * @author user
 */
public class JavaBytecodeExplorerController implements Initializable {
    
    @FXML
    TabPane tabView;
    
    @FXML
    TreeView fileTreeView;
    
    @FXML
    StackPane root;
    
    FileRefWatcher watcher = new FileRefWatcher();
    FolderTreeDialog folderTreeDialog = new FolderTreeDialog();
    InformationDialog aboutDialog = new InformationDialog("Java bytecode viewer to assess code ops realtime");
    
    /**
     * Initializes the controller class.
     * @param url
     * @param rb
     */
        
    RootTreeItem<FileRefModel, FileRefTreeItem<FileRefModel>> rtSetup = RootTreeItem.ofFileRef(new FileRefModel("C:\\Users\\joemw\\OneDrive\\Documents\\GitHub", ".class"));
        
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Font font = Font.loadFont(App.class.getResource("RobotoMono-Regular.ttf").toExternalForm(), 12);
                       
        CodeArea codeArea = new CodeArea();
        codeArea.setFont(font);
        codeArea.setLineNumbersEnabled(true);
        codeArea.setText(SampleString.str);       
        
        StackPane stack = new StackPane(codeArea);
        stack.setPadding(new Insets(2));
        
        FontIcon icon = new FontIcon("mdoal-code");
        icon.setIconSize(16);
        Tab sampleTab = new Tab("Untitled");
        sampleTab.setGraphic(icon);
        sampleTab.setContent(stack);
        tabView.getTabs().add(sampleTab);
                
        initFileExplorer();        
        
        root.getChildren().addAll(folderTreeDialog, aboutDialog);
    }    
        
    private void initFileExplorer(){  
        rtSetup.setExpanded(true);
        fileTreeView.setRoot(rtSetup.withNullRootItem());
        fileTreeView.setShowRoot(false);  
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
            IO.println(f);
            var rootVirtualClass = new ClassRefModel();
            
            var ancestor = new ClassRefModel(ancestorFolder, false);   
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
                    case Created(var dir, var file) -> {}
                    case Deleted(var dir, var file) -> {                         
                        var fi = new RealFile(file);                                           
                        var result = rootVirtualClass.findInTree2(fi);   
                        IO.println("-> " +fi);
                        IO.println(result);
                    }
                    case Modified(var dir, var file) -> {IO.println("modified");}
                    case Overflow(var dir) -> {IO.println("overflow");}
                    case KeyInvalid(var dir) -> {IO.println("invalid");}
                    case DirectoryRevalidated(var dir) -> {IO.println(new RealFile(dir)+ " revalidated");}
                }
            });
            
            var rootT = RootTreeItem.ofFileRef(rootVirtualClass).expandAll().rootTreeItem();
            
            fileTreeView.setRoot(rootT);
            fileTreeView.setShowRoot(false);  
        }
    }
    
    public void about(ActionEvent e){
        aboutDialog.showAndWait(result -> {});
    }
   
    public void darkTheme(ActionEvent e){
        Application.setUserAgentStylesheet(new NordDark().getUserAgentStylesheet());
    }
    
    public void lightTheme(ActionEvent e){
        Application.setUserAgentStylesheet(new NordLight().getUserAgentStylesheet());
    }
}
