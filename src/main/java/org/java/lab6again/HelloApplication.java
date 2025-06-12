package org.java.lab6again;

import javafx.application.Application;
import javafx.stage.Stage;

public class HelloApplication extends Application {

    @Override
    public void start(Stage stage) {
        new ImageAppController().initialize(stage);
    }

    public static void main(String[] args) {
        launch();
    }
}
