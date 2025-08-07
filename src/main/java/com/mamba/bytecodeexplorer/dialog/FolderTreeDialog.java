/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mamba.bytecodeexplorer.dialog;

import com.mamba.bytecodeexplorer.watcher.FileRef;
import com.mamba.mambaui.control.Tile;
import com.mamba.mambaui.modal.ModalDialog;
import javafx.scene.control.ButtonBar;

/**
 *
 * @author user
 */
public class FolderTreeDialog extends ModalDialog<FolderTreePair> {
    
    public FolderTreeDialog() {
        super((handle, dialog) -> {
            var header = new Tile("Folder Monitor Load");
            
            var ok = new javafx.scene.control.Button("OK");
            var cancel = new javafx.scene.control.Button("Cancel");
            ok.setOnAction(e -> handle.submit(null));
            cancel.setOnAction(e -> handle.cancel());
            
            var buttonBar = new ButtonBar();
            ButtonBar.setButtonData(ok, ButtonBar.ButtonData.YES);
            ButtonBar.setButtonData(cancel, ButtonBar.ButtonData.NO);
            buttonBar.getButtons().addAll(ok, cancel);
            
            var content = new FolderTreeDialogPane();

            dialog.setDialogSize(1000, 650);
            
            handle.setHeader(header);
            handle.setContent(content);
            handle.setFooter(buttonBar);
        });
    }
    
}
