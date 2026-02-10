package com.taskmanager.app;

import com.taskmanager.app.di.AppContainer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * JavaFX application entry point.
 */
public class Main extends Application {
    private AppContainer appContainer;

    @Override
    public void start(Stage stage) {
        appContainer = new AppContainer();

        Scene scene = appContainer.mainView().buildScene();
        stage.setTitle("Task Manager");
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void stop() {
        if (appContainer != null) {
            appContainer.close();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
