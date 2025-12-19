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
import model.CacheResultStatus;
import model.MainMemory;
import model.MemoryOperationResult;

import java.util.LinkedHashMap;
import java.util.Map;

import static utils.Utils.B;
import static utils.Utils.KB;
import static utils.Utils.BYTE_SIZE;
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
    @FXML private Label statusLabel;
    @FXML private Label addressBitsValue;
    @FXML private Label indexBitsValue;
    @FXML private Label tagBitsValue;
    @FXML private Label offsetBitsValue;
    @FXML private Label operationInfoLabel;
    @FXML private TextArea cacheViewArea;
    @FXML private TextArea memoryViewArea;

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
        statusLabel.setText("Select cache and memory sizes to begin.");
        operationInfoLabel.setText("Awaiting first operation.");
        cacheViewArea.setText("Cache line details will appear here.");
        memoryViewArea.setText("Main memory block details will appear here.");
    }

    @FXML
    private void configureSimulation() {
        String cacheSelection = cacheSizePicker.getValue();
        String memorySelection = memorySizePicker.getValue();

        if (cacheSelection == null || memorySelection == null) {
            statusLabel.setText("Select cache and main memory sizes.");
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
            return;
        }

        cacheController = new CacheController(cacheMemory, mainMemory);
        toggleActionControls(false);
        updateBitLabels();

        statusLabel.setText("Ready to simulate memory operations.");
        operationInfoLabel.setText(String.format("Configured cache (%s) and main memory (%s).", cacheSelection, memorySelection));

        String randomAddress = generateRandomAddress(mainMemory.calculateAddressSize());
        addressField.setText(randomAddress);
        cacheViewArea.setText("No access yet. Choose Read or Write.");
        memoryViewArea.setText("No access yet. Choose Read or Write.");
    }

    @FXML
    private void onGenerateRandomAddress() {
        if (ensureConfigured()) {
            return;
        }

        String randomAddress = generateRandomAddress(mainMemory.calculateAddressSize());
        addressField.setText(randomAddress);
        statusLabel.setText("Generated a new random address.");
    }

    @FXML
    private void handleRead() {
        if (ensureConfigured()) {
            return;
        }

        String address = validatedAddress();
        if (address == null) {
            return;
        }

        MemoryOperationResult result = cacheController.readDataFromAddress(address);
        statusLabel.setText(result.getStatus() == CacheResultStatus.CACHE_HIT
                ? "Read completed (cache hit)."
                : "Read completed (cache miss).");
        updateViews(address, result, false);
    }

    @FXML
    private void handleWrite() {
        if (ensureConfigured()) {
            return;
        }

        String address = validatedAddress();
        if (address == null) {
            return;
        }

        String data = dataField.getText().trim();
        if (data.isEmpty()) {
            statusLabel.setText("Provide 8-bit data to write.");
            return;
        }

        if (!data.matches("[01]{8}")) {
            statusLabel.setText("Data must be 8-bit binary.");
            return;
        }

        MemoryOperationResult result = cacheController.writeDataToAddress(address, data);
        statusLabel.setText(result.getStatus() == CacheResultStatus.CACHE_HIT
                ? "Write completed (cache hit)."
                : "Write completed (cache miss).");
        updateViews(address, result, true);
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
            return true;
        }
        return false;
    }

    private String validatedAddress() {
        String address = addressField.getText().trim();
        if (address.isEmpty()) {
            statusLabel.setText("Enter an address.");
            return null;
        }

        int expectedSize = mainMemory.calculateAddressSize();
        if (!address.matches("[01]+")) {
            statusLabel.setText("Address must be binary.");
            return null;
        }

        if (address.length() > expectedSize) {
            statusLabel.setText(String.format("Address must be at most %d bits.", expectedSize));
            return null;
        }

        if (address.length() < expectedSize) {
            String padded = "0".repeat(expectedSize - address.length()) + address;
            addressField.setText(padded);
            return padded;
        }

        return address;
    }

    private void updateViews(String address, MemoryOperationResult result, boolean isWrite) {
        String statusText = result.getStatus() == CacheResultStatus.CACHE_HIT ? "CACHE HIT" : "CACHE MISS";
        operationInfoLabel.setText(String.format(
                "%s | %s at address %s",
                statusText,
                isWrite ? "WRITE" : "READ",
                formatAddress(address)
        ));

        String cacheDetails = String.format(
                "Cache line index: %d%nValid bit: %s%nTag: %s%nData (by byte):%n%s",
                result.getCacheLineIndex(),
                result.isCacheLineValid() ? "1" : "0",
                result.getCacheLineTag(),
                formatBlock(result.getCacheLineData())
        );
        cacheViewArea.setText(cacheDetails);

        String memoryDetails = String.format(
                "Main memory line: %d%nData (by byte):%n%s",
                result.getMainMemoryLineIndex(),
                formatBlock(result.getMainMemoryBlock())
        );
        memoryViewArea.setText(memoryDetails);
    }

    private String formatBlock(String dataBlock) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < dataBlock.length(); i += BYTE_SIZE) {
            if (i > 0) {
                builder.append(' ');
            }
            builder.append(dataBlock, i, i + BYTE_SIZE);
        }
        return builder.toString();
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
