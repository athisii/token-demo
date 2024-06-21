package com.cdac.tokendemo;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import lombok.Getter;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class App extends Application {
    // serial.port.file=/dev/ttyUSB0 -> to customise serial port, pass it as program argument
    private static final String SERIAL_PORT_PROGRAM_ARG = "serial.port.file";
    @Getter
    private static String serialPortFile = "/dev/ttyUSB0"; // default value, if no program is passed

    // reader.card=Mantra Reader (1.00) 00 00  -> to customise Reader Card, pass it as program argument
    private static final String READER_CARD_PROGRAM_ARG = "reader.card"; // default value, if no program is passed
    @Getter
    private static String readerCard = "Mantra Reader (1.00) 00 00";

    // reader.token=Mantra Reader (1.00) 01 00 -> to customise Reader Token, pass it as program argument
    private static final String READER_TOKEN_PROGRAM_ARG = "reader.token"; // default value, if no program is passed
    @Getter
    private static String readerToken = "Mantra Reader (1.00) 01 00";  // default value, if no program is passed

    // card.api.service.restart.command=systemctl restart EnrollmentStationServices
    private static final String CARD_API_SERVICE_RESTART_COMMAND_ARG = "card.api.service.restart.command"; // default value, if no program is passed
    @Getter
    private static String cardApiServiceRestartCommand = "Mantra Reader (1.00) 01 00";  // default value, if no program is passed

    // GLOBAL THREAD POOL for the application.
    private static final ExecutorService executorService;

    static {
        int processorCount = Runtime.getRuntime().availableProcessors();
        executorService = Executors.newFixedThreadPool(Math.min(processorCount, 3));
    }

    @Override
    public void start(Stage stage) throws IOException {
        stage.setOnCloseRequest(event -> {
            event.consume();
            Platform.exit();
            System.exit(0);
        });

        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("/fxml/main.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1024, 768);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource(getCssFileName())).toExternalForm());
        stage.setTitle("Token Demo");
        stage.initStyle(StageStyle.UNDECORATED);
        stage.setResizable(false);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        for (String arg : args) {
            // serial.port.file=/dev/ttyUSB0
            if (SERIAL_PORT_PROGRAM_ARG.equalsIgnoreCase(arg)) {
                String[] split = arg.split("=");
                if (split.length == 2) {
                    serialPortFile = split[1];
                }
            }
            //
            else if (READER_CARD_PROGRAM_ARG.equalsIgnoreCase(arg)) {
                String[] split = arg.split("=");
                if (split.length == 2) {
                    readerCard = split[1];
                }
            }
            // reader.token=Mantra Reader (1.00) 01 00
            else if (READER_TOKEN_PROGRAM_ARG.equalsIgnoreCase(arg)) {
                String[] split = arg.split("=");
                if (split.length == 2) {
                    readerToken = split[1];
                }
            }
            // card.api.service.restart.command=systemctl restart EnrollmentStationServices
            else if (CARD_API_SERVICE_RESTART_COMMAND_ARG.equalsIgnoreCase(arg)) {
                String[] split = arg.split("=");
                if (split.length == 2) {
                    cardApiServiceRestartCommand = split[1];
                }
            }
        }
        launch();
    }

    public static String getCssFileName() {
        return "/style/base.css";
    }

    public static ExecutorService getThreadPool() {
        return executorService;
    }
}