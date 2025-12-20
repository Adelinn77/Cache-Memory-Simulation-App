package ui;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.Region;
import model.CacheLine;
import model.CacheMemory;
import model.MainMemory;

import java.util.List;

import utils.AddressDecoder;
import utils.Utils;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;
import java.util.Objects;

import static utils.Utils.BLOCK_SIZE;
import static utils.Utils.PAGE_SIZE_BLOCKS;
import logic.CacheController;
import model.CacheResult;
import model.CacheResultStatus;
import javafx.scene.control.Tooltip;


public class SimulatorController {


    private CacheMemory cache;
    private MainMemory main;
    private CacheController cacheController;

    @FXML private Label titleLabel;

    @FXML private TableView<CacheRow> cacheTable;
    @FXML private TableColumn<CacheRow, String> cIndex, cValid, cTag, cB0, cB1, cB2, cB3;

    @FXML private Pagination memPagination;
    @FXML private TableView<MemRow> memTable;
    @FXML private TableColumn<MemRow, String> mAddr, mB0, mB1, mB2, mB3;
    @FXML private TextField addressBinField;
    @FXML private TextField addressHexField;
    @FXML private TextField tagField, indexField, offsetField;
    @FXML private TextField dataHexField;
    @FXML private Label statusLabel;




    private boolean syncing = false;


    private final ObservableList<CacheRow> cacheRows = FXCollections.observableArrayList();
    private static final double CACHE_TABLE_HEADER = 30;
    private static final int CACHE_COMPACT_MAX_ROWS = 16;



    public void setMemories(CacheMemory cache, MainMemory main) {
        this.cache = cache;
        this.main = main;
        this.cacheController = new CacheController(cache, main);

        titleLabel.setText("Simulation • Cache Memory(" + cache.getCacheSizeInBytes() + "B) • Main Memory(" + main.getSizeInKB() + "KB)");

        int addressSize = main.calculateAddressSize();
        setAddressRaw("0".repeat(addressSize));

        refreshCache();
        setupMainMemoryPagination();
    }


    @FXML
    private void initialize() {
        cIndex.setCellValueFactory(v -> v.getValue().index);
        cValid.setCellValueFactory(v -> v.getValue().valid);
        cTag.setCellValueFactory(v -> v.getValue().tag);
        cB0.setCellValueFactory(v -> v.getValue().b0);
        cB1.setCellValueFactory(v -> v.getValue().b1);
        cB2.setCellValueFactory(v -> v.getValue().b2);
        cB3.setCellValueFactory(v -> v.getValue().b3);

        mAddr.setCellValueFactory(v -> v.getValue().addr);
        mB0.setCellValueFactory(v -> v.getValue().b0);
        mB1.setCellValueFactory(v -> v.getValue().b1);
        mB2.setCellValueFactory(v -> v.getValue().b2);
        mB3.setCellValueFactory(v -> v.getValue().b3);

        styleBitsColumn(cB0);
        styleBitsColumn(cB1);
        styleBitsColumn(cB2);
        styleBitsColumn(cB3);

        styleBitsColumn(mB0);
        styleBitsColumn(mB1);
        styleBitsColumn(mB2);
        styleBitsColumn(mB3);

        styleBitsColumn(cTag);

        cacheTable.setFixedCellSize(28);

        cacheTable.setFixedCellSize(28);

        addressBinField.textProperty().addListener((obs, oldV, newV) -> {
            if (syncing) return;

            if (!newV.matches("[01\\s]*")) {
                syncing = true;
                addressBinField.setText(oldV);
                syncing = false;
                return;
            }

            String raw = normalizeBin(newV);
            String grouped = group4(raw);

            int caret = addressBinField.getCaretPosition();
            syncing = true;
            addressBinField.setText(grouped);
            addressBinField.positionCaret(Math.min(caret, grouped.length()));
            syncing = false;

            addressHexField.setText(raw.isEmpty() ? "" : binToHexFromRaw(raw));
            updateDecodedFields(raw);
        });
    }

    private <S> void styleBitsColumn(TableColumn<S, String> col) {
        col.setCellFactory(tc -> {
            TableCell<S, String> cell = new TableCell<>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty ? null : item);
                }
            };

            cell.setStyle("-fx-font-family: Consolas; -fx-alignment: CENTER; -fx-text-overrun: clip;");
            cell.setWrapText(false);
            return cell;
        });
    }

    private void refreshCache() {
        cacheRows.clear();

        int lines = cache.getNoLines();
        for (int i = 0; i < lines; i++) {
            CacheLine cl = cache.getCacheLines().get(i);
            boolean valid = cl.isValidBit();
            String tag = cl.getTag();
            String data = cl.getData();

            if (data == null || data.length() != 32)
                throw new IllegalArgumentException("Expected 32-bit data, got: " + (data == null ? "null" : data.length()));

            List<String> bytes = bits32To4HexBytes(data);

            cacheRows.add(new CacheRow(
                    String.valueOf(i),
                    valid ? "1" : "0",
                    tag,
                    bytes.get(0), bytes.get(1), bytes.get(2), bytes.get(3)
            ));
        }
        cacheTable.setItems(cacheRows);

        int rowsCount = cacheRows.size();
        double rowH = cacheTable.getFixedCellSize();
        double fitHeight = rowsCount * rowH + CACHE_TABLE_HEADER;

        if (rowsCount <= CACHE_COMPACT_MAX_ROWS) {
            cacheTable.setPrefHeight(fitHeight);
            cacheTable.setMinHeight(Region.USE_PREF_SIZE);
            cacheTable.setMaxHeight(Region.USE_PREF_SIZE);
        }
        else {
            cacheTable.setPrefHeight(Region.USE_COMPUTED_SIZE);
            cacheTable.setMinHeight(0);
            cacheTable.setMaxHeight(Double.MAX_VALUE);

        }
    }

    private void setupMainMemoryPagination() {
        long blocks = main.getMainMemorySizeInBytes()/BLOCK_SIZE;
        int pageCount = (int) Math.ceil(blocks / (double) PAGE_SIZE_BLOCKS);

        memPagination.setPageCount(Math.max(pageCount, 1));
        memPagination.currentPageIndexProperty().addListener((obs, oldV, newV) -> loadMainMemoryPage(newV.intValue()));
        loadMainMemoryPage(0);
    }

    private void loadMainMemoryPage(int pageIndex) {
        long startBlock = (long) pageIndex * PAGE_SIZE_BLOCKS;
        long endBlock = Math.min(startBlock + PAGE_SIZE_BLOCKS, main.getMainMemorySizeInBytes() / BLOCK_SIZE);

        ObservableList<MemRow> rows = FXCollections.observableArrayList();

        for (long blockAddr = startBlock; blockAddr < endBlock; blockAddr++) {
            String data = main.getMainMemoryLines().get((int)blockAddr);
            List<String> bytes = bits32To4HexBytes(data);

            rows.add(new MemRow(
                    "0x" + Long.toHexString(blockAddr * 4).toUpperCase(),
                    bytes.get(0), bytes.get(1), bytes.get(2), bytes.get(3)
            ));
        }
        memTable.setItems(rows);
    }

    @FXML
    private void onBack() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    Objects.requireNonNull(getClass().getResource("/ui/ConfigView.fxml"),
                            "Cannot find /ui/ConfigView.fxml on classpath")
            );
            Parent root = loader.load();

            Stage stage = (Stage) titleLabel.getScene().getWindow();
            stage.getScene().setRoot(root);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    public static class CacheRow {
        SimpleStringProperty index, valid, tag, b0, b1, b2, b3;
        public CacheRow(String index, String valid, String tag, String b0, String b1, String b2, String b3) {
            this.index = new SimpleStringProperty(index);
            this.valid = new SimpleStringProperty(valid);
            this.tag = new SimpleStringProperty(tag);
            this.b0 = new SimpleStringProperty(b0);
            this.b1 = new SimpleStringProperty(b1);
            this.b2 = new SimpleStringProperty(b2);
            this.b3 = new SimpleStringProperty(b3);
        }
    }

    public static class MemRow {
        SimpleStringProperty addr, b0, b1, b2, b3;
        public MemRow(String addr, String b0, String b1, String b2, String b3) {
            this.addr = new SimpleStringProperty(addr);
            this.b0 = new SimpleStringProperty(b0);
            this.b1 = new SimpleStringProperty(b1);
            this.b2 = new SimpleStringProperty(b2);
            this.b3 = new SimpleStringProperty(b3);
        }
    }


    @FXML private void onGoAddress() {
        String raw = normalizeBin(addressBinField.getText());
        int addressSize = main.calculateAddressSize();
        if (!raw.matches("[01]+") || raw.length() != addressSize) {
            titleLabel.setText("Invalid address. Must be " + addressSize + " bits.");
            return;
        }
        titleLabel.setText("Simulation • Cache Memory(" + cache.getCacheSizeInBytes() + "B) • Main Memory(" + main.getSizeInKB() + "KB)");
        long byteAddress = Long.parseLong(raw, 2);
        long blockIndex = byteAddress / BLOCK_SIZE;
        int pageIndex = (int) (blockIndex / PAGE_SIZE_BLOCKS);
        memPagination.setCurrentPageIndex(pageIndex);
        loadMainMemoryPage(pageIndex);
        int rowInPage = (int) (blockIndex % PAGE_SIZE_BLOCKS);
        memTable.getSelectionModel().select(rowInPage);
        memTable.scrollTo(rowInPage);
    }

    @FXML
    private void onRandomAddress() {
        int addressSize = main.calculateAddressSize();
        setAddressRaw(Utils.generateRandomAddress(addressSize));
        titleLabel.setText("Simulation • Cache Memory(" + cache.getCacheSizeInBytes() + "B) • Main Memory(" + main.getSizeInKB() + "KB)");
    }


    @FXML
    private void onRead() {
        String raw = normalizeBin(addressBinField.getText());
        int addressSize = main.calculateAddressSize();

        if (!raw.matches("[01]+") || raw.length() != addressSize) {
            titleLabel.setText("Invalid address. Must be " + addressSize + " bits.");
            return;
        }

        titleLabel.setText("Simulation • Cache Memory(" + cache.getCacheSizeInBytes() +
                "B) • Main Memory(" + main.getSizeInKB() + "KB)");

        long byteAddress = Long.parseLong(raw, 2);
        long blockIndex  = byteAddress / BLOCK_SIZE;

        int pageIndex = (int) (blockIndex / PAGE_SIZE_BLOCKS);
        int rowInPage = (int) (blockIndex % PAGE_SIZE_BLOCKS);

        memPagination.setCurrentPageIndex(pageIndex);
        loadMainMemoryPage(pageIndex);

        CacheResult probe = probeCache(raw);
        boolean hit = probe.getStatus() == CacheResultStatus.CACHE_HIT;
        setStatus(hit, hit ? "Cache hit" : probe.getData());

        String bits8 = cacheController.readDataFromAddress(raw);
        int v = Integer.parseInt(bits8, 2);
        dataHexField.setText(String.format("%02X", v));

        refreshCache();
        loadMainMemoryPage(pageIndex);

        memTable.getSelectionModel().clearAndSelect(rowInPage);
        memTable.scrollTo(rowInPage);
        memTable.requestFocus();
    }




    @FXML
    private void onWrite() {
        String raw = normalizeBin(addressBinField.getText());
        int addressSize = main.calculateAddressSize();

        if (!raw.matches("[01]+") || raw.length() != addressSize) {
            titleLabel.setText("Invalid address. Must be " + addressSize + " bits.");
            return;
        }

        titleLabel.setText("Simulation • Cache Memory(" + cache.getCacheSizeInBytes() +
                "B) • Main Memory(" + main.getSizeInKB() + "KB)");

        long byteAddress = Long.parseLong(raw, 2);
        long blockIndex  = byteAddress / BLOCK_SIZE;

        int pageIndex = (int) (blockIndex / PAGE_SIZE_BLOCKS);
        int rowInPage = (int) (blockIndex % PAGE_SIZE_BLOCKS);

        memPagination.setCurrentPageIndex(pageIndex);
        loadMainMemoryPage(pageIndex);

        CacheResult probe = probeCache(raw);
        boolean hit = probe.getStatus() == CacheResultStatus.CACHE_HIT;
        setStatus(hit, hit ? "Write HIT" : "Write MISS");

        try {
            String dataBits = hexByteToBits8(dataHexField.getText());
            cacheController.writeDataToAddress(raw, dataBits);

            refreshCache();
            loadMainMemoryPage(pageIndex);

            memTable.getSelectionModel().clearAndSelect(rowInPage);
            memTable.scrollTo(rowInPage);
            memTable.requestFocus();

        } catch (Exception e) {
            titleLabel.setText("Write error: " + e.getMessage());
        }
    }



    private static String bits8ToHex(String bits8) {
        if (bits8 == null) return "";
        bits8 = bits8.trim();
        if (bits8.length() != 8) return bits8;
        int v = Integer.parseInt(bits8, 2);
        return String.format("%02X", v);
    }

    private static String hexByteToBits8(String hex) {
        if (hex == null) throw new IllegalArgumentException("Empty data");

        hex = hex.trim().toUpperCase();
        if (hex.startsWith("0X")) hex = hex.substring(2);
        hex = hex.replaceAll("\\s+", "");

        if (!hex.matches("[0-9A-F]{1,2}"))
            throw new IllegalArgumentException("Data must be 1-2 hex digits (00..FF)");

        int v = Integer.parseInt(hex, 16);
        return String.format("%8s", Integer.toBinaryString(v)).replace(' ', '0');
    }

    private static List<String> bits32To4HexBytes(String bits32) {
        if (bits32 == null) return List.of("", "", "", "");
        bits32 = bits32.trim();
        if (bits32.length() != 32) return List.of(bits32, "", "", "");

        return List.of(
                bits8ToHex(bits32.substring(0, 8)),
                bits8ToHex(bits32.substring(8, 16)),
                bits8ToHex(bits32.substring(16, 24)),
                bits8ToHex(bits32.substring(24, 32))
        );
    }

    private void setAddressRaw(String rawBits) {
        rawBits = normalizeBin(rawBits);

        syncing = true;
        addressBinField.setText(group4(rawBits));
        syncing = false;

        addressHexField.setText(rawBits.isEmpty() ? "" : binToHexFromRaw(rawBits));
        updateDecodedFields(rawBits);
    }


    private static String normalizeBin(String s) {
        if (s == null) return "";
        return s.replaceAll("\\s+", "");
    }

    private static String group4(String raw) {
        raw = normalizeBin(raw);
        if (raw.isEmpty()) return "";
        StringBuilder sb = new StringBuilder(raw.length() + raw.length()/4);
        for (int i = 0; i < raw.length(); i++) {
            if (i > 0 && i % 4 == 0) sb.append(' ');
            sb.append(raw.charAt(i));
        }
        return sb.toString();
    }

    private static String binToHexFromRaw(String rawBits) {
        rawBits = normalizeBin(rawBits);
        if (rawBits.isEmpty()) return "";
        long v = Long.parseLong(rawBits, 2);
        int hexDigits = (int) Math.ceil(rawBits.length() / 4.0);
        return "0x" + String.format("%0" + hexDigits + "X", v);
    }


    private void updateDecodedFields(String rawBits) {
        if (cache == null || main == null) return;

        int addressSize = main.calculateAddressSize();
        if (!rawBits.matches("[01]*") || rawBits.length() != addressSize) {
            tagField.setText("");
            indexField.setText("");
            offsetField.setText("");
            addressHexField.setText("");
            return;
        }

        String tag    = AddressDecoder.extractTag(rawBits, cache.getTagSize());
        String index  = AddressDecoder.extractIndex(rawBits, cache.getIndexSize());
        String offset = AddressDecoder.extractOffset(rawBits);

        tagField.setText(group4(tag));
        indexField.setText(index);
        offsetField.setText(offset);

        addressHexField.setText(binToHexFromRaw(rawBits));
    }

    private void setStatus(boolean hit, String detail) {
        statusLabel.getStyleClass().removeAll("status-hit", "status-miss");
        statusLabel.getStyleClass().add(hit ? "status-hit" : "status-miss");
        statusLabel.setText(hit ? "HIT" : "MISS");
        statusLabel.setTooltip(detail == null || detail.isBlank() ? null : new Tooltip(detail));
    }

    private CacheResult probeCache(String addrRaw) {
        String offset = AddressDecoder.extractOffset(addrRaw);
        String index  = AddressDecoder.extractIndex(addrRaw, cache.getIndexSize());
        String tag    = AddressDecoder.extractTag(addrRaw, cache.getTagSize());
        return cache.findInCache(index, offset, tag);
    }


}