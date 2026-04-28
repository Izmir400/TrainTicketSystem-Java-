package application;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import javafx.scene.Node;

import java.io.IOException;

public class SupportController {

    @FXML
    private TextArea messageArea;

    @FXML
    private ListView<String> messageHistory;

    @FXML
    private void onBackToDashboard(ActionEvent event) {
        try {
            Parent dashboardRoot = FXMLLoader.load(getClass().getResource("/application/UserInterface.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(dashboardRoot));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Nuk u gjet GuestDashboard.fxml");
        }
    }

    @FXML
    private void onSendMessage(ActionEvent event) {
        String message = messageArea.getText();
        if (message == null || message.trim().isEmpty()) {
            messageHistory.getItems().add("⚠ Mesazhi është bosh!");
        } else {
            messageHistory.getItems().add("📩 Sent: " + message);
            messageArea.clear();
        }
    }

    @FXML
    private void onCallSupport(ActionEvent event) {
        messageHistory.getItems().add("📞 Calling support...");
    }
}
