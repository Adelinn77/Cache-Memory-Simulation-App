package ui;

import exceptions.MainMemoryAddressSizeNotSet;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import logic.CacheController;
import model.CacheMemory;
import model.MainMemory;

import java.util.LinkedHashMap;
import java.util.Map;

import static utils.Utils.B;
import static utils.Utils.KB;
import static utils.Utils.OFFSET_SIZE;
import static utils.Utils.generateRandomAddress;

public class MainView {

    private static final Map<String, Integer> CACHE_SIZE_OPTIONS = new LinkedHashMap<>() {{
        put("16 B", 16 * B);
        put("32 B", 32 * B);
        put("64 B", 64 * B);
        put("128 B", 128 * B);
    }};

    private static final Map<String, Integer> MAIN_MEMORY_SIZE_OPTIONS = new LinkedHashMap<>() {{
        put("64 KB", 64 * KB);
        put("128 KB", 128 * KB);
        put("256 KB", 256 * KB);
        put("512 KB", 512 * KB);
    }};

    @FXML private ComboBox<String> cacheSizePicker;
    @FXML private ComboBox<String> memorySizePicker;
    @FXML private TextField addressField;
    @FXML private TextField dataField;
    @FXML private Button randomAddressButton;
    @FXML private Button readButton;
    @FXML private Button writeButton;
    @FXML private TextArea logArea;
    @FXML private Label statusLabel;
    @FXML private Label addressBitsValue;
    @FXML private Label indexBitsValue;
    @FXML private Label tagBitsValue;
    @FXML private Label offsetBitsValue;

    private CacheController cacheController;
    private CacheMemory cacheMemory;
    private MainMemory mainMemory;

    @FXML
    public void initialize() {
        cacheSizePicker.setItems(FXCollections.observableArrayList(CACHE_SIZE_OPTIONS.keySet()));
        memorySizePicker.setItems(FXCollections.observableArrayList(MAIN_MEMORY_SIZE_OPTIONS.keySet()));

        cacheSizePicker.getSelectionModel().selectFirst();
        memorySizePicker.getSelectionModel().selectFirst();

        toggleActionControls(true);
        logArea.appendText("Welcome! Configure the simulator to begin.\n");
    }

    @FXML
    private void configureSimulation() {
        String cacheSelection = cacheSizePicker.getValue();
        String memorySelection = memorySizePicker.getValue();

        if (cacheSelection == null || memorySelection == null) {
            statusLabel.setText("Select cache and main memory sizes.");
            appendLog("Please choose values for both cache size and main memory size.");
            return;
        }

        int cacheSizeBytes = CACHE_SIZE_OPTIONS.get(cacheSelection);
        int mainMemorySizeBytes = MAIN_MEMORY_SIZE_OPTIONS.get(memorySelection);

        mainMemory = new MainMemory(mainMemorySizeBytes);
        cacheMemory = new CacheMemory(cacheSizeBytes);
        cacheMemory.setMainMemoryAddressSize(mainMemory.calculateAddressSize());

        try {
            cacheMemory.buildCacheMemory();
            mainMemory.setTagSize(cacheMemory.getTagSize());
        } catch (MainMemoryAddressSizeNotSet e) {
            statusLabel.setText("Configuration error. Check sizes and retry.");
            appendLog(e.getMessage());
            return;
        }

        cacheController = new CacheController(cacheMemory, mainMemory);
        toggleActionControls(false);
        updateBitLabels();

        statusLabel.setText("Ready to simulate memory operations.");
        appendLog(String.format("Configured cache (%s) and main memory (%s).", cacheSelection, memorySelection));

        String randomAddress = generateRandomAddress(mainMemory.calculateAddressSize());
        addressField.setText(randomAddress);
        appendLog("Starting address: " + formatAddress(randomAddress));
    }

    @FXML
    private void onGenerateRandomAddress() {
        if (!ensureConfigured()) {
            return;
        }

        String randomAddress = generateRandomAddress(mainMemory.calculateAddressSize());
        addressField.setText(randomAddress);
        appendLog("New random address: " + formatAddress(randomAddress));
    }

    @FXML
    private void handleRead() {
        if (!ensureConfigured()) {
            return;
        }

        String address = validatedAddress();
        if (address == null) {
            return;
        }

        String data = cacheController.readDataFromAddress(address);
        statusLabel.setText("Read completed.");
        appendLog(String.format("Read byte %s from %s", data, formatAddress(address)));
    }

    @FXML
    private void handleWrite() {
        if (!ensureConfigured()) {
            return;
        }

        String address = validatedAddress();
        if (address == null) {
            return;
        }

        String data = dataField.getText().trim();
        if (data.isEmpty()) {
            statusLabel.setText("Provide 8-bit data to write.");
            appendLog("Write aborted: data field is empty.");
            return;
        }

        if (!data.matches("[01]{8}")) {
            statusLabel.setText("Data must be 8-bit binary.");
            appendLog("Write aborted: data must be exactly 8 binary digits.");
            return;
        }

        cacheController.writeDataToAddress(address, data);
        statusLabel.setText("Write completed.");
        appendLog(String.format("Wrote byte %s to %s", data, formatAddress(address)));
    }

    private void toggleActionControls(boolean disable) {
        addressField.setDisable(disable);
        dataField.setDisable(disable);
        randomAddressButton.setDisable(disable);
        readButton.setDisable(disable);
        writeButton.setDisable(disable);
    }

    private void updateBitLabels() {
        addressBitsValue.setText(String.valueOf(mainMemory.calculateAddressSize()));
        indexBitsValue.setText(String.valueOf(cacheMemory.getIndexSize()));
        offsetBitsValue.setText(String.valueOf(OFFSET_SIZE));
        tagBitsValue.setText(String.valueOf(cacheMemory.getTagSize()));
    }

    private boolean ensureConfigured() {
        if (cacheController == null || cacheMemory == null || mainMemory == null) {
            statusLabel.setText("Configure cache and memory first.");
            appendLog("Action blocked: simulator is not configured yet.");
            return false;
        }
        return true;
    }

    private String validatedAddress() {
        String address = addressField.getText().trim();
        if (address.isEmpty()) {
            statusLabel.setText("Enter an address.");
            appendLog("Operation aborted: address is empty.");
            return null;
        }

        int expectedSize = mainMemory.calculateAddressSize();
        if (!address.matches("[01]+")) {
            statusLabel.setText("Address must be binary.");
            appendLog("Operation aborted: address must contain only 0s and 1s.");
            return null;
        }

        if (address.length() != expectedSize) {
            statusLabel.setText(String.format("Address must be %d bits.", expectedSize));
            appendLog(String.format("Operation aborted: address must be %d bits long.", expectedSize));
            return null;
        }

        return address;
    }

    private void appendLog(String message) {
        logArea.appendText(message + "\n");
    }

    private String formatAddress(String address) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < address.length(); i++) {
            if (i > 0 && i % 4 == 0) {
                builder.append(' ');
            }
            builder.append(address.charAt(i));
        }
        return builder.toString();
    }
}
