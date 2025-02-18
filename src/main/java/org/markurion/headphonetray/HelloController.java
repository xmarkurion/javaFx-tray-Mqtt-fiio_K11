package org.markurion.headphonetray;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class HelloController {
    protected Helper helper;

    @FXML private Button button_save;
    @FXML
    ChoiceBox select;
    @FXML private Button btn_connect;
    @FXML private Button btn_on;
    @FXML private Button btn_off;
    @FXML TextField mqtt_url;
    @FXML
    TextField mqtt_topic;
    @FXML
    TextField mqtt_on;
    @FXML
    TextField mqtt_off;
    @FXML
    CheckBox checkBox;
    @FXML
    Label statusBar;
    @FXML Label label_current_dev;
    @FXML Label label_target_dev_status;

    private Boolean configFilePresent = false;

    @FXML public void initialize() {
        //TODO: Load config file and check if it's present

        // Set the prompt text for the text fields
        mqtt_url.setPromptText("Enter MQTT broker URL like localhost:1883");
        mqtt_topic.setPromptText("Enter MQTT topic lke /home/data");
        mqtt_on.setPromptText("Enter MQTT message to turn on device");
        mqtt_off.setPromptText("Enter MQTT message to turn off device");

        if(!configFilePresent){
            statusBar.setText("Please enter the required information");
        }

        // Instantiate the helper class to help with the logic
        helper = new Helper(this);
        helper.loadConfigFile();
        helper.fillAvailableDevices();

        // Set the button save action
        button_save.setOnAction(e -> {

            boolean checkFields = helper.checkFields();
            if (checkFields) {
                helper.saveConfig();
                statusBar.setText("Configuration saved");
                helper.setStatusMessage("",1);

                // restart application
                helper.restartApplication();
            }
        });

        //Set the button connect action
        btn_connect.setOnAction(e -> {
            helper.connectToMqtt();
        });

        // Set the button ON   action
        btn_on.setOnAction(e -> {
            helper.sentStartCommand();
        });

        // Set the button OFF action
        btn_off.setOnAction(e -> {
            helper.sentShutdownCommand();
        });

        // When select is changed update helper.changeTargetDevice
        select.setOnAction(e -> {
            helper.changeTargetDevice(select.getValue().toString());
        });

        // Run in the background to check if the sound device is active
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(() -> {
            Platform.runLater(() -> {
                helper.activeUsedSoundDevice();
            });
        }, 0, 1, TimeUnit.SECONDS);
    }

    public void turnOff() {
        // if checkbox selected send off command
        if(checkBox.isSelected()){
            System.out.println("Sending off message to turn off the AMP");
            helper.sentShutdownCommand();
        }
    }

}