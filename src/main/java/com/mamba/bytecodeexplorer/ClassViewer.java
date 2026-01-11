/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mamba.bytecodeexplorer;

import com.mamba.bytecodeexplorer.classanalysis.ClassAnalysis;
import com.mamba.bytecodeexplorer.classanalysis.ConstantPoolInfo;
import com.mamba.bytecodeexplorer.classanalysis.UtilBytecode;
import java.util.Optional;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.geometry.Side;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import jfx.incubator.scene.control.richtext.CodeArea;
import org.kordamp.ikonli.javafx.FontIcon;

/**
 *
 * @author joemw
 */
public class ClassViewer {
    private static final Font font = Font.loadFont(App.class.getResource("RobotoMono-Regular.ttf").toExternalForm(), 12);
    
    private final Tab tab;
    private final CodeArea bytecodeArea;
    private final CodeArea constantpoolArea;
    
    private ObjectProperty<ClassAnalysis> ca = new SimpleObjectProperty();

    public ClassViewer() {       
        bytecodeArea = new CodeArea(); 
        bytecodeArea.setContentPadding(new Insets(3, 0, 0, 10));
        bytecodeArea.setFont(font);
        bytecodeArea.setEditable(false);
        bytecodeArea.setLineNumbersEnabled(true);
        
        var stackByteCode = new StackPane(bytecodeArea);
        stackByteCode.setPadding(new Insets(2));
        
        constantpoolArea = new CodeArea();
        constantpoolArea.setContentPadding(new Insets(3, 0, 0, 10));
        constantpoolArea.setFont(font);
        constantpoolArea.setEditable(false);
        constantpoolArea.setLineNumbersEnabled(true);
        
        var stackConstantPool = new StackPane(constantpoolArea);
        stackConstantPool.setPadding(new Insets(2));

        var sideTabs = new TabPane();
        sideTabs.setSide(Side.LEFT);
        sideTabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        sideTabs.getTabs().addAll(
            new Tab("Class Bytecode", stackByteCode),
            new Tab("Constant Pool",stackConstantPool)   
        );
        
        var icon = new FontIcon("mdoal-code");
        icon.setIconSize(16);
        
        tab = new Tab();
        tab.textProperty().bind(
            Bindings.createStringBinding(
                () -> {
                    return switch(ca.get()){
                        case null -> "Untitled";
                        case ClassAnalysis a -> UtilBytecode.toBinaryClassName(a.name()) + ".class";
                    };
                },
                ca
            )
        );
        tab.setClosable(false);
        tab.setGraphic(icon);
        tab.setContent(sideTabs);        
    }

    public void show(ClassAnalysis ca) {
        this.ca.set(ca);
        
        
        var cpis = ConstantPoolInfo.project(ca.classModel());
        var builder = new StringBuilder();
        for (var cpi : cpis) {
            builder.append(ConstantPoolInfo.format(cpi)).append("\n");
        }
             
        bytecodeArea.setText(ca.bytecodeString());
        constantpoolArea.setText(builder.toString().trim());
    }

    public Tab tab() {
        return tab;
    }
    
    public Optional<ClassAnalysis> classAnalysis(){
        return Optional.ofNullable(ca.get());
    }
}
