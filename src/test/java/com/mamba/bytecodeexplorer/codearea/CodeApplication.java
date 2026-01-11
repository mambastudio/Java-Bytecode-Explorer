/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mamba.bytecodeexplorer.codearea;

import com.mamba.bytecodeexplorer.codearea.decorator.TodoHighlighter;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import jfx.incubator.scene.control.richtext.CodeArea;

/**
 *
 * @author joemw
 */
public class CodeApplication extends Application{
    
    String string = """
                    Joe is a good boy, TODO this.
                    The following is TODO
                    TODO
                    Wewe ni fala!!!
                    """;

    @Override
    public void start(Stage stage) throws Exception {
        CodeArea codearea = new CodeArea();
        codearea.setSyntaxDecorator(new TodoHighlighter());
        codearea.setText(string);
        
        Scene scene = new Scene(new StackPane(codearea), 1000, 700);
                
        stage.setTitle("Code Area");
        stage.setScene(scene);
        stage.show(); 
    }
    
}
