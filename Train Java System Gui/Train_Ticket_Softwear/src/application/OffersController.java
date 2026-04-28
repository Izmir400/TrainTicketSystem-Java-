package application;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Node;

import java.io.IOException;

public class OffersController {

    @FXML
    private void onBackToDashboard(ActionEvent event) {
        try {
            // Ngarko GuestDashboard.fxml
            Parent dashboardRoot = FXMLLoader.load(getClass().getResource("/application/UserInterface.fxml"));

            // Merr skenën aktuale nga butoni
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // Vendos skenën e re
            stage.setScene(new Scene(dashboardRoot));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Nuk u gjet GuestDashboard.fxml");
        }
    }
}
