package ui;

import exceptions.MainMemoryAddressSizeNotSet;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import model.CacheMemory;
import model.MainMemory;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;

public class ConfigController {

    @FXML private ComboBox<String> mainMemCombo;
    @FXML private ComboBox<String> cacheCombo;
    @FXML private Label hintLabel;

    @FXML
    public void initialize() {
        mainMemCombo.getItems().addAll("8 KB", "16 KB", "32 KB", "64 KB", "128 KB", "256 KB", "512 KB");
        cacheCombo.getItems().addAll("16 B", "32 B", "64 B", "128 B", "256 B", "512 B");

        mainMemCombo.getSelectionModel().select("64 KB");
        cacheCombo.getSelectionModel().select("32 B");
    }

    @FXML
    private void onReset() {
        mainMemCombo.getSelectionModel().select("64 KB");
        cacheCombo.getSelectionModel().select("32 B");
        hintLabel.setText("Pick sizes for main memory and cache memory, then start the simulation.");
    }

    @FXML
    private void onStart() {
        long mainMemBytes = parseToBytes(mainMemCombo.getValue());
        long cacheBytes   = parseToBytes(cacheCombo.getValue());

        MainMemory mm = new MainMemory(mainMemBytes);
        CacheMemory cm = new CacheMemory(cacheBytes);

        cm.setMainMemoryAddressSize(mm.calculateAddressSize());
        try {
            cm.buildCacheMemory();
            mm.setTagSize(cm.getTagSize());
        } catch (MainMemoryAddressSizeNotSet e) {
            e.printStackTrace();
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(
                    Objects.requireNonNull(getClass().getResource("/ui/SimulatorView.fxml"),
                            "Cannot find /ui/SimulatorView.fxml on classpath")
            );

            Parent root = loader.load();

            SimulatorController controller = loader.getController();
            controller.setMemories(cm, mm);

            Stage stage = (Stage) hintLabel.getScene().getWindow();
            Scene scene = stage.getScene();
            scene.setRoot(root);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    private long parseToBytes(String s) {
        String[] parts = s.trim().split("\\s+");
        long value = Integer.parseInt(parts[0]);
        String unit = parts[1].toUpperCase();

        return switch (unit) {
            case "B" -> value;
            case "KB" -> value * 1024;
            case "MB" -> value * 1024 * 1024;
            default -> throw new IllegalArgumentException("Unknown unit: " + unit);
        };
    }
}
