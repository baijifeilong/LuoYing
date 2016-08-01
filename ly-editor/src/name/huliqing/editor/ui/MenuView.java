/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package name.huliqing.editor.ui;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;

/**
 *
 * @author huliqing
 */
public class MenuView extends MenuBar {
    
    private Menu fileMenu;
    private MenuItem quick = new MenuItem("Quick");
    
    private Menu helpMenu;
    
    public MenuView() {
        fileMenu = new Menu("File");
        helpMenu = new Menu("Help");
        
        quick = new MenuItem("Quick");
        
        fileMenu.getItems().add(quick);

        getMenus().addAll(fileMenu, helpMenu);
    }
    
    public void setOnQuick(EventHandler<ActionEvent> event) {
        quick.setOnAction(event);
    }
}