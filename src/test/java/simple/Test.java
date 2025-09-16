/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package simple;

import com.mamba.bytecodeexplorer.watcher.treeitem.FileRefModel;
import com.mamba.bytecodeexplorer.watcher.treeitem.FileRefTreeItem;
import javafx.scene.Node;
import javafx.scene.control.TreeItem;
import javafx.util.Callback;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.javafx.StackedFontIcon;

/**
 *
 * @author user
 */
public class Test {
    Callback<FileRefModel, Node> graphicsFactory = fileRef -> {
        StackedFontIcon fontIcon = new StackedFontIcon();            
        if(fileRef.ref().isDirectory() && fileRef.ref().isDirectoryEmpty(".class")){
            FontIcon icon = new FontIcon("mdal-folder");
            fontIcon.getChildren().add(icon);
        }
        else if(fileRef.ref().isDirectory()){
            FontIcon icon = new FontIcon("mdoal-create_new_folder");
            fontIcon.getChildren().add(icon);                
        }
        else if(!fileRef.ref().isDirectory()){
            FontIcon icon = new FontIcon("mdoal-code");
            fontIcon.getChildren().add(icon);
        }
        return fontIcon;
    };
    
    
    FileRefModel rootModel = new FileRefModel("C:\\Users\\user\\Documents\\NetBeansProjects\\Bitmap", ".class");
    TreeItem<FileRefModel> rootItem = new TreeItem<>(rootModel); // acts like an invisible virtual rootItem TODO: Help in creating a virtual fileref (points to nothing) to avoid nulls
    
    void main(){
        
    }
    
    //this should be called if folder is added in rootItem (children and their whole hierarchy of subchildren are added automatically in the listener or during initialisation)
    private void addToRoot(FileRefModel fileRefModel){        
        //rootItem.getChildren().add(new FileTreeItem(null, null, null));
        rootItem.getChildren().add(new FileRefTreeItem<>(
                fileRefModel,
                graphicsFactory,
                FileRefModel::children // no parentheses here
            )
        );    
    }
    
    
}
