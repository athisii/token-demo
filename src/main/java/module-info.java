module com.cdac.tokendemo {
    requires java.logging;
    requires java.net.http;

    requires javafx.controls;
    requires javafx.fxml;


    requires serial.port.token.dispenser;
    requires org.bouncycastle.provider;

    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.annotation;
    requires com.fasterxml.jackson.databind;

    opens com.cdac.tokendemo to javafx.fxml;
    exports com.cdac.tokendemo;
    exports com.cdac.tokendemo.controller;
    opens com.cdac.tokendemo.controller to javafx.fxml;

    requires static lombok;
}