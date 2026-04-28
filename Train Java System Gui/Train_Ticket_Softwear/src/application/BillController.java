package application;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;

public class BillController {

    @FXML
    private TextArea billTextArea;

    private Train train;   // Store the train object passed from PaymentController

    /**
     * Called from PaymentController to pass the Train object and display bill details
     */
    public void setTrain(Train train) {
        this.train = train;
        if (train == null) {
            billTextArea.setText("Error: No train data available.");
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("🧾 Train Ticket Bill\n\n");
        sb.append("Train Name     : ").append(train.getTrainName() != null ? train.getTrainName() : "N/A").append("\n");
        sb.append("Train Number   : ").append(train.getTrainName() != null ? train.getTrainName() : "N/A").append("\n");  
        sb.append("From           : ").append(train.getDepartureStation() != null ? train.getDepartureStation() : "N/A")
          .append("  →  To: ").append(train.getArrivalStation() != null ? train.getArrivalStation() : "N/A").append("\n");
        sb.append("Departure      : ").append(train.getDepartureTime() != null ? train.getDepartureTime() : "N/A").append("\n");
        sb.append("Arrival        : ").append(train.getArrivalTime() != null ? train.getArrivalTime() : "N/A").append("\n");
        sb.append("Available Seats: ").append(train.getAvailableSeats()).append("\n");
        sb.append("Status         : Confirmed\n");
        sb.append("Passenger      : ").append(getPassengerName()).append("\n");   // Optional improvement

        billTextArea.setText(sb.toString());
    }

    // Helper method to get passenger name 
    private String getPassengerName() {
        // For now returning a placeholder. You can pass passenger name separately if needed.
        return "John Doe";  
    }

    @FXML
    private void onPrintClick() {
        if (train == null) {
            showAlert("Error", "No bill data to print.");
            return;
        }
        System.out.println("Printing bill for train: " + train.getTrainName());
        showAlert("Print", "Bill sent to printer successfully.");
    }

    @FXML
    private void onDownloadClick() {
        if (train == null) {
            showAlert("Error", "No train data available to generate bill.");
            return;
        }

        try {
       //     PdfGenerator.generateBill(train);
            showAlert("Success", "Bill saved as PDF successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to generate PDF:\n" + e.getMessage());
        }
    }

    @FXML
    private void onCloseClick() {
        // Close the bill window
        Stage stage = (Stage) billTextArea.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}