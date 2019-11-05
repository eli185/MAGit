package components.commitTree.commitNode;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import java.util.function.BiConsumer;

public class ContextMenuCommitNode {
    private  ContextMenu contextMenu ;

    public ContextMenuCommitNode(){
        contextMenu = new ContextMenu();
    }

    public ContextMenu getContextMenu() {
        return contextMenu;
    }

    public MenuItem createMenuItem(String i_Content, BiConsumer i_Consumer, Object o, Object b){
        MenuItem menuItem=new MenuItem(i_Content);
        menuItem.setOnAction(v->i_Consumer.accept(o,b));
        return  menuItem;
    }

    public void mainMenuCreator(MenuItem menuItem){
        contextMenu.getItems().add(menuItem);
    }
}