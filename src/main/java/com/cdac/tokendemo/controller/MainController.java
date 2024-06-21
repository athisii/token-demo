package com.cdac.tokendemo.controller;

import com.cdac.tokendemo.App;
import com.cdac.tokendemo.dto.CRWaitForConnectResDto;
import com.cdac.tokendemo.dto.DynamicFile;
import com.cdac.tokendemo.exception.ConnectionTimeoutException;
import com.cdac.tokendemo.exception.GenericException;
import com.cdac.tokendemo.exception.NoReaderOrCardException;
import com.cdac.tokendemo.logging.ApplicationLog;
import com.cdac.tokendemo.util.Asn1CardTokenUtil;
import com.cdac.tokendemo.util.TokenDispenserUtil;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import org.bouncycastle.util.Strings;
import org.bouncycastle.util.encoders.Hex;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.cdac.tokendemo.util.Asn1CardTokenUtil.*;

public class MainController {
    private static final Logger LOGGER = ApplicationLog.getLogger(MainController.class);
    private static final int TOKEN_DROP_SLEEP_TIME_IN_SEC = 7;

    @FXML
    private TextField inputTextField;
    @FXML
    private Button writeDataBtn;
    @FXML
    private Label tokenReadLabel;
    @FXML
    private Button readDataBtn;
    @FXML
    private Label messageLabel;

    public void initialize() {
        writeDataBtn.setOnAction(event -> writeDataBtnAction());
        readDataBtn.setOnAction(event -> readDataBtnAction());
        inputTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!messageLabel.getText().isBlank()) {
                messageLabel.setText("");
            }
        });
    }

    private void writeDataBtnAction() {
        LOGGER.info("***writeDataBtnAction called.");
        if (inputTextField.getText().isBlank()) {
            messageLabel.setText("Please enter a valid text for writing to token.");
            return;
        }
        disableControls(inputTextField, writeDataBtn, readDataBtn);
        App.getThreadPool().execute(this::startWriting);
    }

    private void readDataBtnAction() {
        LOGGER.info("***readDataBtnAction called.");
        disableControls(inputTextField, writeDataBtn, readDataBtn);
        App.getThreadPool().execute(this::startReading);
    }

    private void startWriting() {
        //dispenses token on card writer
        if (!TokenDispenserUtil.dispenseToken()) {
            updateUi("Kindly check the Token Dispenser and try again.");
            enableControls(inputTextField, writeDataBtn, readDataBtn);
            return;
        }
        try {
            startWriteProcedureCall();
        } catch (GenericException | NoReaderOrCardException ex) {
            updateUi(ex.getMessage());
            enableControls(inputTextField, writeDataBtn, readDataBtn);
            return;
        } catch (ConnectionTimeoutException ex) {
            updateUi("Something went wrong. Kindly check Card API service.");
            enableControls(inputTextField, writeDataBtn, readDataBtn);
            return;
        }
        updateUi("Data written successfully.");
        enableControls(inputTextField, writeDataBtn, readDataBtn);
    }

    private void startReading() {
        try {
            startReadProcedureCall();
        } catch (GenericException | NoReaderOrCardException ex) {
            updateUi(ex.getMessage());
            enableControls(inputTextField, writeDataBtn, readDataBtn);
        } catch (ConnectionTimeoutException ex) {
            updateUi("Something went wrong. Kindly check Card API service.");
            enableControls(inputTextField, writeDataBtn, readDataBtn);
        }
    }

    private void startWriteProcedureCall() {
        /*
            DeInitialize
            Initialize
            waitForConnect - card
            selectApp - card
            waitForConnect - token
            selectApp - token
            read data(static) - token
            read cert - card
            verify cert - token handle
            pki auth - (token handle, card handle)
            token write:
                  1. dynamic data
         */
        LOGGER.log(Level.INFO, () -> "***MainController: Calling deInitialize API.");
        Asn1CardTokenUtil.deInitialize();
        LOGGER.log(Level.INFO, () -> "***MainController: Calling initialize API.");
        Asn1CardTokenUtil.initialize();
        // setup reader; need to add a delay for some milliseconds
        try {
            LOGGER.log(Level.INFO, () -> "***Card: Sleeping for " + SLEEP_TIME_BEFORE_WAIT_FOR_CONNECT_CALL_IN_MIL_SEC + " milliseconds before waitFocConnect API call.");
            Thread.sleep(SLEEP_TIME_BEFORE_WAIT_FOR_CONNECT_CALL_IN_MIL_SEC);
        } catch (InterruptedException e) {
            LOGGER.log(Level.SEVERE, "****BeforeWaitSleep: Interrupted while sleeping.");
            Thread.currentThread().interrupt();
        }
        LOGGER.log(Level.INFO, () -> "***Card: Calling waitForConnect API.");
        CRWaitForConnectResDto crWaitForConnectResDto = Asn1CardTokenUtil.waitForConnect(MANTRA_CARD_READER_NAME);
        // already handled for non-zero error code
        byte[] decodedHexCsn = Base64.getDecoder().decode(crWaitForConnectResDto.getCsn());
        if (decodedHexCsn.length != crWaitForConnectResDto.getCsnLength()) {
            LOGGER.log(Level.INFO, () -> "****CSNError: Decoded bytes size not matched with response length.");
            throw new GenericException("Decoded bytes size not matched with response length.");
        }
        int cardHandle = crWaitForConnectResDto.getHandle();
        Asn1CardTokenUtil.selectApp(CARD_TYPE_NUMBER, cardHandle);

        // setup writer; need to add a delay for some milliseconds
        try {
            LOGGER.log(Level.INFO, () -> "***Token: Sleeping for " + SLEEP_TIME_BEFORE_WAIT_FOR_CONNECT_CALL_IN_MIL_SEC + " milliseconds before waitFocConnect API call.");
            Thread.sleep(SLEEP_TIME_BEFORE_WAIT_FOR_CONNECT_CALL_IN_MIL_SEC);
        } catch (InterruptedException e) {
            LOGGER.log(Level.SEVERE, "****BeforeWaitSleep: Interrupted while sleeping.");
            Thread.currentThread().interrupt();
        }
        updateUi("Preparing the token for data writing. Please wait.");

        try {
            Thread.sleep(TimeUnit.SECONDS.toMillis(TOKEN_DROP_SLEEP_TIME_IN_SEC));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        LOGGER.log(Level.INFO, () -> "***Token: Calling waitForConnect API.");
        crWaitForConnectResDto = Asn1CardTokenUtil.waitForConnect(MANTRA_CARD_WRITER_NAME);
        decodedHexCsn = Base64.getDecoder().decode(crWaitForConnectResDto.getCsn());
        if (decodedHexCsn.length != crWaitForConnectResDto.getCsnLength()) {
            LOGGER.log(Level.INFO, () -> "CSNError: Decoded bytes size not matched with response length.");
            throw new GenericException("Decoded bytes size not matched with response length.");
        }
        String tokenCsn = Strings.fromByteArray(Hex.encode(decodedHexCsn));
        LOGGER.info(() -> "***TokenCSN: " + tokenCsn);
        int tokenHandle = crWaitForConnectResDto.getHandle();
        LOGGER.log(Level.INFO, () -> "***Token: Calling selectApp API.");
        Asn1CardTokenUtil.selectApp(TOKEN_TYPE_NUMBER, tokenHandle);
        LOGGER.log(Level.INFO, () -> "***Token: Calling readData API to read static data to get the token number.");

        byte[] asn1EncodedTokenStaticData = Asn1CardTokenUtil.readBufferedData(tokenHandle, CardTokenFileType.STATIC);
        String tokenNumber = new String(extractFromAsn1EncodedStaticData(asn1EncodedTokenStaticData, 1), StandardCharsets.UTF_8);
        LOGGER.info(() -> "***TokenNumber: " + tokenNumber);

        // read cert now
        LOGGER.log(Level.INFO, () -> "***Card: Calling readData API for reading system certificate.");
        byte[] systemCertificate = Asn1CardTokenUtil.readBufferedData(cardHandle, CardTokenFileType.SYSTEM_CERTIFICATE);

        LOGGER.log(Level.INFO, () -> "***Token: Calling verifyCertificate API: handle=token");
        Asn1CardTokenUtil.verifyCertificate(tokenHandle, WHICH_TRUST, WHICH_CERTIFICATE, systemCertificate);

        LOGGER.log(Level.INFO, () -> "***Token: Calling pkiAuth API: handle1=token, handle2=card");
        Asn1CardTokenUtil.pkiAuth(tokenHandle, cardHandle);

        DynamicFile dynamicFile = new DynamicFile();
        dynamicFile.setLabourName(inputTextField.getText());
        Asn1CardTokenUtil.encodeAndStoreDynamicFile(tokenHandle, dynamicFile);
    }

    private void startReadProcedureCall() {
        LOGGER.log(Level.INFO, () -> "***MainController: Calling deInitialize API.");
        Asn1CardTokenUtil.deInitialize();
        LOGGER.log(Level.INFO, () -> "***MainController: Calling initialize API.");
        Asn1CardTokenUtil.initialize();
        // setup reader; need to add a delay for some milliseconds
        try {
            LOGGER.log(Level.INFO, () -> "***Card: Sleeping for " + SLEEP_TIME_BEFORE_WAIT_FOR_CONNECT_CALL_IN_MIL_SEC + " milliseconds before waitForConnect API call.");
            Thread.sleep(SLEEP_TIME_BEFORE_WAIT_FOR_CONNECT_CALL_IN_MIL_SEC);
        } catch (InterruptedException e) {
            LOGGER.log(Level.SEVERE, "****BeforeWaitSleep: Interrupted while sleeping.");
            Thread.currentThread().interrupt();
        }
        LOGGER.log(Level.INFO, () -> "***Card: Calling waitForConnect API.");
        CRWaitForConnectResDto crWaitForConnectResDto = Asn1CardTokenUtil.waitForConnect(MANTRA_CARD_READER_NAME);
        // already handled for non-zero error code
        byte[] decodedHexCsn = Base64.getDecoder().decode(crWaitForConnectResDto.getCsn());
        if (decodedHexCsn.length != crWaitForConnectResDto.getCsnLength()) {
            LOGGER.log(Level.INFO, () -> "****CSNError: Decoded bytes size not matched with response length.");
            throw new GenericException("Decoded bytes size not matched with response length.");
        }
        int cardHandle = crWaitForConnectResDto.getHandle();
        Asn1CardTokenUtil.selectApp(CARD_TYPE_NUMBER, cardHandle);

        // setup writer; need to add a delay for some milliseconds
        try {
            LOGGER.log(Level.INFO, () -> "***Token: Sleeping for " + SLEEP_TIME_BEFORE_WAIT_FOR_CONNECT_CALL_IN_MIL_SEC + " milliseconds before waitForConnect API call.");
            Thread.sleep(SLEEP_TIME_BEFORE_WAIT_FOR_CONNECT_CALL_IN_MIL_SEC);
        } catch (InterruptedException e) {
            LOGGER.log(Level.SEVERE, "****BeforeWaitSleep: Interrupted while sleeping.");
            Thread.currentThread().interrupt();
        }

        LOGGER.log(Level.INFO, () -> "***Token: Calling waitForConnect API.");
        crWaitForConnectResDto = Asn1CardTokenUtil.waitForConnect(MANTRA_CARD_WRITER_NAME);
        decodedHexCsn = Base64.getDecoder().decode(crWaitForConnectResDto.getCsn());
        if (decodedHexCsn.length != crWaitForConnectResDto.getCsnLength()) {
            LOGGER.log(Level.INFO, () -> "CSNError: Decoded bytes size not matched with response length.");
            throw new GenericException("Decoded bytes size not matched with response length.");
        }
        String tokenCsn = Strings.fromByteArray(Hex.encode(decodedHexCsn));
        LOGGER.info(() -> "***TokenCSN: " + tokenCsn);
        int tokenHandle = crWaitForConnectResDto.getHandle();
        LOGGER.log(Level.INFO, () -> "***Token: Calling selectApp API.");
        Asn1CardTokenUtil.selectApp(TOKEN_TYPE_NUMBER, tokenHandle);
        LOGGER.log(Level.INFO, () -> "***Token: Calling readData API to read dynamic data.");
        byte[] asn1EncodedTokenDynamicData = Asn1CardTokenUtil.readBufferedData(tokenHandle, CardTokenFileType.DYNAMIC_FILE);

        /*
          Dynamic File ANS1 structure:
                0. User Category ID
                1 Name
                2 Gender
                3 Date of Birth
                4 Blood Group
                5 Nationality
                6 Unit (Issuance unit)
                7 Unique ID - To identify the person
                8 Contractor ID
         */

        String labourName = new String(extractFromAsn1EncodedStaticData(asn1EncodedTokenDynamicData, 1), StandardCharsets.UTF_8);
        Platform.runLater(() -> {
            tokenReadLabel.setText(labourName);
            messageLabel.setText("Data read successfully.");
        });
        enableControls(inputTextField, writeDataBtn, readDataBtn);
    }

    private void disableControls(Node... nodes) {
        for (Node node : nodes) {
            node.setDisable(true);
        }
    }

    private void enableControls(Node... nodes) {
        for (Node node : nodes) {
            node.setDisable(false);
        }
    }

    public void updateUi(String message) {
        Platform.runLater(() -> messageLabel.setText(message));
    }
}