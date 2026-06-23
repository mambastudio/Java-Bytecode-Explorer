/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mamba.bytecodeexplorer;

import com.mamba.bytecodeexplorer.file.HybridFileMonitorService;
import java.io.IOException;
import module atlantafx.base;
import module javafx.fxml;

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
        JavaBytecodeExplorerController controller = loader.getController();
        HybridFileMonitorService fileMonitorService =
                new HybridFileMonitorService(HybridFileMonitorService::pollingBackend);
        controller.setFileMonitorService(fileMonitorService);
        
        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("mambaui-atlanta-theme.css").toExternalForm());
        stage.setTitle("Java Bytecode Explorer");
        stage.setScene(scene);
        stage.getIcons().add(
            new Image(
                BytecodeExplorer.class
                    .getResourceAsStream("/com/mamba/bytecodeexplorer/byte.png")
            )
        );
        stage.setOnCloseRequest(_ -> {
            controller.closeWatcher();
            try {
                fileMonitorService.close();
            } catch (IOException ex) {
                System.getLogger(App.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
            }
        });
        stage.show();
        
        
        
    }
    
}
