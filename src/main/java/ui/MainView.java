package ui;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.StackPane;

public class MainView {

    @FXML private Button btnDashboard;
    @FXML private Button btnCache;
    @FXML private Button btnMemory;
    @FXML private StackPane contentArea;
    @FXML private TextArea logArea;

    @FXML
    public void initialize() {
        logArea.appendText("Application started...\n");
    }

    @FXML
    private void showCacheView() {
        logArea.appendText("Cache view opened\n");
    }
}
