/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.utc.lo23.sharutc.ui.widget;

import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ItemsList implements Initializable {

    static final Logger log = Logger.getLogger(SearchResultItemBox.class.getName());

    public FlowPane content;


    public ItemsList() {

    }

    public Pane buildPane() {
        Pane pane = null;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("../fxml/items_list.fxml"));
            loader.setController(this);
            pane = (Pane) loader.load();

        } catch (IOException ex) {
            log.log(Level.SEVERE, null, ex);
        }
        return pane;
    }


    @Override
    public void initialize(URL url, ResourceBundle rb) {
    }

    public void addChild(ItemBox item) {
        content.getChildren().add(item.buildPane("../fxml/item_box.fxml"));

    }

}
