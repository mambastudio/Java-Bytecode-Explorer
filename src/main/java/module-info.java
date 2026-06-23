/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/module-info.java to edit this template
 */

module BytecodeExplorer {
    requires atlantafx.base;
    requires mambauifx;
    requires org.kordamp.ikonli.material2;
    requires org.kordamp.ikonli.core;
    requires org.kordamp.ikonli.javafx;
    requires javafx.controls;
    requires javafx.base;
    requires javafx.graphics;
    requires javafx.fxml;
    requires jfx.incubator.input;
    requires jfx.incubator.richtext;
    requires java.logging;
    
    exports com.mamba.bytecodeexplorer;
    opens com.mamba.bytecodeexplorer to javafx.fxml;
    opens com.mamba.bytecodeexplorer.dialog to javafx.fxml;
}
