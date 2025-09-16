/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package com.mamba.bytecodeexplorer;

import atlantafx.base.theme.NordDark;
import atlantafx.base.theme.NordLight;
import com.mamba.bytecodeexplorer.dialog.FolderTreeDialog;
import com.mamba.bytecodeexplorer.watcher.treeitem.FileRefModel;
import com.mamba.bytecodeexplorer.watcher.FileRef;
import com.mamba.bytecodeexplorer.watcher.FileRefWatcher;
import com.mamba.bytecodeexplorer.watcher.FileRefWatcherListener;
import com.mamba.bytecodeexplorer.watcher.treeitem.FileRefTreeItem;
import com.mamba.bytecodeexplorer.watcher.treeitem.RootItemSetup;
import com.mamba.mambaui.modal.ModalDialogs.InformationDialog;
import java.io.IO;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.application.Application;
import javafx.application.Platform;
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
    
    FolderTreeDialog folderTreeDialog = new FolderTreeDialog();
    InformationDialog aboutDialog = new InformationDialog("Java bytecode viewer to assess code ops realtime");
    
    /**
     * Initializes the controller class.
     * @param url
     * @param rb
     */
        
    FileRefWatcher watcher  = new FileRefWatcher(100); 
    final RootItemSetup<FileRefModel> rtSetup = new RootItemSetup(new FileRefModel("C:\\Users\\user\\Documents\\NetBeansProjects\\Bitmap", ".class"));
        
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
                
        watcher.setMonitor(new FileRef("C:\\Users\\user\\Documents\\NetBeansProjects\\Bitmap"));
        watcher.registerListener(new FileRefWatcherListener(){
            @Override
            public void onCreate(FileRef parent, FileRef child) {
                Platform.runLater(() -> {
                    rtSetup.rootModel().findInTree(parent).ifPresent(fModel -> {
                        if ((child.hasExtension() && child.isFileExtension(".class")) || child.isDirectory()) {
                            boolean exists = fModel.children().stream()
                                .anyMatch(m -> m.ref().equals(child));

                            if (!exists) {
                                fModel.children().add(new FileRefModel(child));
                            }
                        }
                    });
                });
            }
            @Override
            public void onModify(FileRef parent, FileRef child) {
                
            }
            @Override
            public void onDelete(FileRef parent, FileRef child) {
                Platform.runLater(()->{
                    rtSetup.rootModel().findInTree(parent).ifPresent(fModel -> {
                        fModel.findInTree(child).ifPresent(toRemove -> {
                            fModel.children().remove(toRemove);                            
                        });
                    });
                });
            }
            

        });
        watcher.processEvents();        
               
        initFileExplorer();        
        
        root.getChildren().addAll(folderTreeDialog, aboutDialog);
    }    
        
    private void initFileExplorer(){  
        rtSetup.rootItem().setExpanded(true);
        fileTreeView.setRoot(rtSetup.rootItem());
        fileTreeView.setShowRoot(false);  
    }
    
    public void open(ActionEvent e){
        folderTreeDialog.showAndWait(result -> {               
            result.ifPresentOrElse(r -> IO.println(r), () -> IO.println("No result"));
        });  
        
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
