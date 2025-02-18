package org.markurion.headphonetray;

import javafx.application.Platform;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.jetbrains.annotations.NotNull;
import java.nio.file.Files;
import java.nio.file.Paths;


public class Helper {
    private boolean attempt = false;
    private String username,password;

    private MqttHandler mqttHandler;
    private SoundDeviceChecker soundDeviceChecker;
    private ConfigFile configFile;
    private HelloController controller;

    public Helper(HelloController controller) {
        this.configFile = new ConfigFile();
        this.controller = controller;

        try{
            checkConfig();
        } catch (ConfigFile.ConfigFileException e) {
            e.printStackTrace();
        }

    }
    public void checkConfig() throws ConfigFile.ConfigFileException {
        if(!configFile.checkConfig()){
            controller.statusBar.setText("Error creating config file");
        }

        // Check if file config.properties exists
        if(! Files.exists(Paths.get("config.properties")) ){
            System.out.println("File config.properties do not exists");
            return;
        }

        // check if there are keys in a config file and return true and flase if
        if(configFile.getProperty("mqtt_topic") == "" ||
                configFile.getProperty("mqtt_url") == "" ||
                configFile.getProperty("mqtt_on") == "" ||
                configFile.getProperty("mqtt_off") == "" ||
                configFile.getProperty("device") == "" ||
                configFile.getProperty("checkBox") == "" ||
                configFile.getProperty("checkBox_ON") == "" ||
                configFile.getProperty("username") == "" ||
                configFile.getProperty("password") == "")
        {
            controller.statusBar.setText("Please enter the required information and press save.");
            return;
        }
    }

    /**
     * Check if all the fields are filled
     * @return boolean
     */
    public boolean checkFields(){
        //check if all the fields are filled and if not return an error message
        if(controller.mqtt_topic.getText().isEmpty() ||
                controller.mqtt_on.getText().isEmpty() ||
                controller.mqtt_off.getText().isEmpty() ||
                controller.mqtt_url.getText().isEmpty() ||
                controller.select.getValue() == null)
        {
            controller.statusBar.setText("Some fields are empty please fill them.");
            return false;
        }

        //check if the IP address is valid
        if(!validateIpWithPort(controller.mqtt_url.getText())){
            controller.statusBar.setText("Invalid IP and Port");
            return false;
        }

        return true;
    }

    public void saveConfig() {
        try {
            configFile.setProperty("mqtt_url", controller.mqtt_url.getText());
            configFile.setProperty("mqtt_topic", controller.mqtt_topic.getText());
            configFile.setProperty("mqtt_on", controller.mqtt_on.getText());
            configFile.setProperty("mqtt_off", controller.mqtt_off.getText());
            configFile.setProperty("device", controller.select.getValue().toString());
            configFile.setProperty("username", controller.login.getText());
            configFile.setProperty("password", controller.password.getText());
            configFile.setProperty("checkBox", controller.checkBox.isSelected() ? "true" : "false");
            configFile.setProperty("checkBox_ON", controller.checkBox_ON.isSelected() ? "true" : "false");
            configFile.save();
        } catch (ConfigFile.ConfigFileException e) {
            e.printStackTrace();
        }
    }

    public void restartApplication(){
        // Restart the application after saving the configuration
        // TODO: Implement the restart logic
    }

    public void fillAvailableDevices() {
        controller.select.getItems().addAll(soundDeviceChecker.getListOfDevices());
    }

    public boolean loadConfigFile() {
        try {
            controller.mqtt_url.setText(configFile.getProperty("mqtt_url"));
            controller.mqtt_topic.setText(configFile.getProperty("mqtt_topic"));
            controller.mqtt_on.setText(configFile.getProperty("mqtt_on"));
            controller.mqtt_off.setText(configFile.getProperty("mqtt_off"));
            controller.select.setValue(configFile.getProperty("device"));

            String device = configFile.getProperty("device");
            controller.select.setValue(device);
            soundDeviceChecker = new SoundDeviceChecker(device);

            // Set the checkbox to true if the value is true
            controller.checkBox.setSelected(configFile.getProperty("checkBox").equals("true"));
            controller.checkBox_ON.setSelected(configFile.getProperty("checkBox_ON").equals("true"));

            if(configFile.getProperty("username") == null || configFile.getProperty("password") == null){
                return false;
            }

            controller.login.setText(configFile.getProperty("username"));
            controller.password.setText(configFile.getProperty("password"));
            username = configFile.getProperty("username");
            password = configFile.getProperty("password");

            //if the username and password are not null then connect to the mqtt broker
            this.connectToMqtt();

            return true;
        } catch (ConfigFile.ConfigFileException e) {
            e.printStackTrace();
            controller.statusBar.setText("Please add username and password to config file manually.");
            return false;
        }
    }

    public void changeTargetDevice(String device) {
        if(soundDeviceChecker != null){
            soundDeviceChecker.setTargetDeviceName(device);
        }
    }

    public void activeUsedSoundDevice() {
        String activeSoundDevice = soundDeviceChecker.getActiveDevice();
        controller.label_current_dev.setText(activeSoundDevice);

        if(activeSoundDevice.equals(soundDeviceChecker.getTargetDeviceName())){
            controller.label_target_dev_status.setText("Active");
            controller.label_target_dev_status.setStyle("-fx-text-fill: green");
        } else {
            controller.label_target_dev_status.setText("Inactive");
            controller.label_target_dev_status.setStyle("-fx-text-fill: red");

            // If there is no current on attempt to turn on the device
            if(!attempt){
                sentShutdownCommand();
            }
        }

    }

    public void sentShutdownCommand() {
        if(!checkMqttConnection()) return;
        if(controller.label_target_dev_status.getText().equals("Inactive")) return;


        controller.statusBar.setText("Shutting down the headphone amplifier");
        this.setStatusMessage("",2);

        //Send the off command to the headphone amplifier
        try{
            mqttHandler.message(configFile.getProperty("mqtt_off"));
        }catch (Exception e){
            controller.statusBar.setText("Error sending the off command");
        }

    }

    public void sentStartCommand() {
        if(!checkMqttConnection()) return;

        // Set our attempt to true to prevent multiple attempts
        attempt = true;

        controller.statusBar.setText("Starting the headphone amplifier");
        this.setStatusMessage("", 2);

        //Send the on command to the headphone amplifier
        try{
            mqttHandler.message(configFile.getProperty("mqtt_on"));
        }catch (Exception e){
            controller.statusBar.setText("Error sending the on command");
        }

        //Delay turn on by 10 seconds
        new java.util.Timer().schedule(
                new java.util.TimerTask() {
                    @Override
                    public void run() {
                        Platform.runLater(() -> soundDeviceChecker.switchToTargetDevice());
                    }
                },
                10000
        );

        // Delay turn on by 12 seconds - set attempt to false as we finished connecting.
        new java.util.Timer().schedule(
                new java.util.TimerTask() {
                    @Override
                    public void run() {
                        Platform.runLater(() -> attempt = false);
                    }
                },
                12000
        );
    }

    public boolean checkMqttConnection() {
        if(mqttHandler == null){
            controller.statusBar.setText("Please connect to MQTT broker first.");
            return false;
        }
        return true;
    }

    public void connectToMqtt() {
        Platform.runLater(() -> {
            try {
                mqttHandler = new MqttHandler(
                        configFile.getProperty("mqtt_url"),
                        username,
                        password,
                        configFile.getProperty("mqtt_topic")
                );
                controller.statusBar.setText("Successfully Connected to MQTT broker!");
                this.setStatusMessage("", 2);
            } catch (MqttException e) {
                e.printStackTrace();
            } catch (ConfigFile.ConfigFileException e) {
                throw new RuntimeException(e);
            }
        });


//        try {
//            mqttHandler = new MqttHandler(
//                    configFile.getProperty("mqtt_url"),
//                    username,
//                    password,
//                    configFile.getProperty("mqtt_topic"),
//                    configFile.getProperty("mqtt_on"),
//                    configFile.getProperty("mqtt_off")
//            );
//            controller.statusBar.setText("Successfully Connected to MQTT broker!");
//            this.setStatusMessage("", 2);
//        } catch (MqttException e) {
//            e.printStackTrace();
//        } catch (ConfigFile.ConfigFileException e) {
//            throw new RuntimeException(e);
//        }
    }

        /**
     * Set the status message after a certain number of seconds
     * @param message   The message to display
     * @param seconds   The number of seconds to display the message
     */
    public void setStatusMessage(String message, int seconds) {
        new java.util.Timer().schedule(
                new java.util.TimerTask() {
                    @Override
                    public void run() {
                        Platform.runLater(() -> controller.statusBar.setText(message));
                    }
                },
                seconds * 1000
        );
    }

    /**
     * Validate the IP address with a port number
     * @param ipPort
     */
    public boolean validateIpWithPort(@NotNull String ipPort) {
        boolean check = ipPort.matches("^(?:[0-9]{1,3}\\.){3}[0-9]{1,3}:[0-9]{1,5}$");
        if (check) {
            controller.statusBar.setText("Invalid IP and Port");
        }
        return check;
    }
}
