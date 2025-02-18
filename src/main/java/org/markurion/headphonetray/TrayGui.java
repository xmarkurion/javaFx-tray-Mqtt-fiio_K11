package org.markurion.headphonetray;

import javafx.stage.Stage;
import java.awt.Image;
import javafx.application.Platform;
import java.awt.*;


public class TrayGui {
    String app_name;
    HelloController controller;
    Stage stage;
    Image image;

    public TrayGui(Stage stage, HelloController controller, Image image, String app_name) {
        this.app_name = app_name;
        this.controller = controller;
        this.stage = stage;
        this.image = image;
        this.stage.setOnCloseRequest(e -> {
            e.consume();
            this.stage.hide();
        });

        if (SystemTray.isSupported()) {
            createTrayIcon();
        } else {
            System.out.println("System tray is not supported!");
        }

    }

    private void createTrayIcon() {
        SystemTray tray = SystemTray.getSystemTray();
        TrayIcon trayIcon = new TrayIcon(image);

        trayIcon.setImageAutoSize(true);
        trayIcon.setToolTip(this.app_name);

        MenuItem exitItem = new MenuItem("Exit");
        exitItem.addActionListener(e -> {
            System.exit(0);
        });

        PopupMenu popup = new PopupMenu();
        popup.add(showApp());
        popup.addSeparator();
        popup.add(on());
        popup.add(off());
        popup.addSeparator();
        popup.add(exitItem);

        trayIcon.setPopupMenu(popup);
        try {
            tray.add(trayIcon);
        } catch (AWTException e) {
            e.printStackTrace();
        }
    }

    private MenuItem showApp() {
        MenuItem showMe = new MenuItem("Show App");
        showMe.addActionListener(e -> {
            Platform.runLater(() -> {
                if (!stage.isShowing()) {
                    stage.show();
                }
                if (stage.isIconified()) {
                    stage.setIconified(false);
                }
            });
        });
        return showMe;
    }
    private MenuItem on(){
        MenuItem turnOn = new MenuItem("ON");
        turnOn.addActionListener(e -> {
            Platform.runLater(() -> { controller.helper.sentStartCommand(); });
        });
        return turnOn;
    }

    private MenuItem off(){
        MenuItem turnOff = new MenuItem("OFF");
        turnOff.addActionListener(e -> {
            Platform.runLater(() -> { controller.helper.sentShutdownCommand(); });
        });
        return turnOff;
    }

}