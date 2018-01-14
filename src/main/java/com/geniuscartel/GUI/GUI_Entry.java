package com.geniuscartel.GUI;

import com.geniuscartel.EQBCClient;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class GUI_Entry extends Application{
    private Stage primaryStage;
    private EQBCClient client;


    @Override
    public void start(Stage primaryStage) throws Exception {
        this.primaryStage = primaryStage;
        this.client = new EQBCClient();
        String hudResourceName = "com/geniuscartel/GUI/hud.fxml";
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource(hudResourceName));
        Parent root = fxmlLoader.load();
        hud controller = fxmlLoader.getController();

        Scene mainStage = new Scene(root, 500, 500);
        primaryStage.setScene(mainStage);
        primaryStage.setTitle("EQBC Client");
        primaryStage.show();
    }
}
