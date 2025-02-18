package org.markurion.headphonetray;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class ConfigFile {
    private String configFilePath;
    private Properties properties;
    private boolean propertiesLoaded;

    /**
     * Custom exception for the ConfigFile class
     */
    public class ConfigFileException extends Exception {
        public ConfigFileException(String message) {
            super(message);
        }
    }

    /**
     * Constructor
     */
    public ConfigFile() {
        String appDirectory = System.getProperty("user.dir");
        configFilePath = appDirectory + "\\" + "config.properties";

        if( checkConfig() ){
            properties = new Properties();
            try {
                properties.load(new FileInputStream(configFilePath));
                propertiesLoaded = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Check if the config file exists
     * @return
     */
    boolean checkConfig(){
        // Create a config file if it doesn't exist
        if(!new File(configFilePath).isFile()){
            try {
                new File(configFilePath).createNewFile();
                properties = new Properties();
                properties.setProperty("mqtt_url", "");
                properties.setProperty("mqtt_topic", "");
                properties.setProperty("mqtt_on", "");
                properties.setProperty("mqtt_off", "");
                properties.setProperty("device", "");
                properties.setProperty("checkBox", "");
                properties.setProperty("username", "");
                properties.setProperty("password", "");
                properties.store(new FileOutputStream(configFilePath), null);
                return true;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }

    /**
     * Set a property
     * @param key
     * @param value
     * @return
     * @throws ConfigFileException
     */
    public boolean setProperty(String key, String value) throws ConfigFileException {
        if (propertiesLoaded) {
            properties.setProperty(key, value);
            return true;
        }
        throw new ConfigFileException("Properties not loaded");
    }

    /**
     * Get the value of a property
     * @param key
     * @return
     * @throws ConfigFileException
     */
    public String getProperty(String key) throws ConfigFileException {
        if (propertiesLoaded) {
            if(properties.getProperty(key) == null){
                throw new ConfigFileException("Key: "+ key +" not found");
            }
            return properties.getProperty(key);
        }
        throw new ConfigFileException("Properties not loaded");
    }

    /**
     * Save the properties to the file
     * @return
     */
    public boolean save(){
        if (propertiesLoaded) {
            try {
                properties.store(new FileOutputStream(configFilePath), null);
                return true;
            } catch (IOException e) {
                throw new RuntimeException("Error saving properties file", e);
            }
        }
        return false;
    }

    public static void main(String[] args) {
        ConfigFile configFile = new ConfigFile();
        try {
//            configFile.setProperty("test2", "aaa");
//            configFile.setProperty("test", "abc");
//            configFile.save();

            System.out.println(configFile.getProperty("test2"));
            System.out.println(configFile.getProperty("test"));
            System.out.println(configFile.getProperty("test324234"));

        } catch (ConfigFileException e) {
            e.printStackTrace();
        }
    }

}
