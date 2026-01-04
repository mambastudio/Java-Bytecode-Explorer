/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mamba.bytecodeexplorer;

import atlantafx.base.theme.NordDark;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 *
 * @author user
 */
public class App extends Application {
    
    

    @Override
    public void start(Stage stage) throws Exception {
        Application.setUserAgentStylesheet(new NordDark().getUserAgentStylesheet());
        
        FXMLLoader loader = new FXMLLoader(getClass().getResource("JavaBytecodeExplorer.fxml"));
        Parent root = loader.load();       
        
        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("mambaui-atlanta-theme.css").toExternalForm());
        stage.setTitle("Java Bytecode Explorer");
        stage.setScene(scene);
        stage.show();
        
        
        
    }
    
}
