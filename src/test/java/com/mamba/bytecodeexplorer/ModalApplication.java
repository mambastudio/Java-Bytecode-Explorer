/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mamba.bytecodeexplorer;

import atlantafx.base.theme.NordDark;
import com.mamba.mambaui.modal.ModalDialogs;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 *
 * @author user
 */
public class ModalApplication extends Application{    
    @Override
    public void start(Stage stage) throws Exception {
        Application.setUserAgentStylesheet(new NordDark().getUserAgentStylesheet());
        
        var dialog = ModalDialogs.error();
        Exception dummyException = new RuntimeException("Simulated Exception for Testing");
        Button btn = new Button("Open");
        btn.setOnAction(e-> { 
            dialog.setStackTrace(dummyException);
            dialog.showAndWait(result -> {               
                dummyCall();
            });            
        });
        
        Scene scene = new Scene(new StackPane(dialog, btn), 1000, 700);
        scene.getStylesheets().add(getClass().getResource("mambaui-atlanta-theme.css").toExternalForm());
                
        stage.setTitle("Close Button Viewer");
        stage.setScene(scene);
        stage.show(); 
    }
    
    public void dummyCall(){
        IO.println("kubafu");
    }
    
}
