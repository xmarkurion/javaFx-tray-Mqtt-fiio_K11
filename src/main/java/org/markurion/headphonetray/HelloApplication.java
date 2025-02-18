package org.markurion.headphonetray;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import javax.swing.*;
import java.io.IOException;
import java.util.Objects;

public class HelloApplication extends Application {
    private final String app_name = "Auto headphone switcher";
    public HelloController controller;
    private TrayGui trayGui;

    @Override
    public void init() {
        new Add();
    }

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("main.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        Image icon = new Image("pngIcon.png");
        stage.getIcons().add(icon);
        stage.setTitle(app_name);
        stage.setResizable(false);

        stage.setScene(scene);
        controller = fxmlLoader.getController(); // Get the controller instance

        stage.hide();

        trayGui = new TrayGui(
                stage,
                controller,
                SwingFXUtils.fromFXImage(icon, null),
                app_name);

        // stage.setIconified(true); -> this will minimize the window on start

        // Prevent the window from closing when the close button is clicked
        Platform.setImplicitExit(false);
        stage.setOnCloseRequest(e -> {
            e.consume();
            stage.hide();
        });

        // Add a shutdown hook to turn off the device when the application is closed
        shutDownHook();
    }

    public void shutDownHook(){
        Thread shutdownHook = new Thread(() -> {
            controller.turnOff();
        });
        Runtime.getRuntime().addShutdownHook(shutdownHook);
    }




    public static void main(String[] args) {
        launch();
    }
}