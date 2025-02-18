module org.markurion.headphonetray {
    requires javafx.controls;
    requires javafx.fxml;
    requires annotations;
    requires java.sql;
    requires org.eclipse.paho.client.mqttv3;
    requires javafx.swing;


    opens org.markurion.headphonetray to javafx.fxml;
    exports org.markurion.headphonetray;
}