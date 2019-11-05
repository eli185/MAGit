package main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import java.net.URL;

public class Main extends Application {

    final Image MAGIT_LOGO = new Image(getClass().getResourceAsStream("/main/MagitLogo.png"));


    public static void main(String[] args) {
        Thread.currentThread().setName("main");
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader();
        URL url = getClass().getResource("mainAppForm.fxml");
        fxmlLoader.setLocation(url);
        BorderPane root = fxmlLoader.load(url.openStream());
        MainAppFormController headerController = fxmlLoader.getController();
        headerController.setPrimaryStage(primaryStage);
        primaryStage.setTitle("Magit");
        primaryStage.getIcons().add(MAGIT_LOGO);
        Scene scene = new Scene(root);
        headerController.setPrimaryScene(scene);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
