package org.markurion.headphonetray;

import org.eclipse.paho.client.mqttv3.*;
import java.util.UUID;
import java.util.concurrent.Callable;

// Read more here
// https://www.baeldung.com/java-mqtt-client

public class MqttHandler {
    IMqttClient publisher;
    String ip,login,password,topic, onMessage, offMessage;

    /**
     * MQTT class init with ip login and pass
     * @param ip
     * @param login
     * @param password
     * @throws MqttException
     */
    MqttHandler(String ip, String login, String password, String topic) throws MqttException {
        this.ip= ip;
        this.login = login;
        this.password = password;
        this.topic = topic;

        String publisherId = UUID.randomUUID().toString();
        publisher = new MqttClient("tcp://"+ip,publisherId,null);
        connect();
    }

    /**
     * Method that handles the connection.
     * @throws MqttException
     */
    public void connect() throws MqttException {
        MqttConnectOptions options = new MqttConnectOptions();
        options.setUserName(login);
        options.setPassword(password.toCharArray());
        options.setAutomaticReconnect(true);
        options.setCleanSession(true);
        options.setConnectionTimeout(10);
        publisher.connect(options);
    }

    public boolean message(String message){
        Postman p = new Postman(publisher, message, topic);
        try {
            p.call();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}

class Postman implements Callable<Void>{
    IMqttClient client;
    String message;
    String topic;

    public Postman(IMqttClient client, String message, String topic) {
        this.client = client;
        this.message = message;
        this.topic = topic;
    }

    @Override
    public Void call() throws Exception {
        if ( !client.isConnected()) {
            return null;
        }
        MqttMessage msg = preparedMessage();
        msg.setQos(2);
        msg.setRetained(true);
        client.publish(topic,msg);
        return null;
    }

    private MqttMessage preparedMessage() {
        byte[] payload = message.getBytes();
        return new MqttMessage(payload);
    }
}