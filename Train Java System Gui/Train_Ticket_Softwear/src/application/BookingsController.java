package application;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import java.io.IOException;
import java.sql.Timestamp;

public class BookingsController {

    @FXML private TableView<Booking> bookingsTable;
    @FXML private TableColumn<Booking, Integer> bookingIdCol;
    @FXML private TableColumn<Booking, String> trainNameCol;
    @FXML private TableColumn<Booking, String> routeCol;
    @FXML private TableColumn<Booking, Timestamp> dateCol;
    @FXML private TableColumn<Booking, Integer> seatsCol;
    @FXML private TableColumn<Booking, String> statusCol;

    private final BookingDAO bookingDAO = new BookingDAO();
    private String currentUsername = "izmir";   // Default for testing

    @FXML
    private void initialize() {
        setupTableColumns();
        loadBookings();
    }

    private void setupTableColumns() {
        bookingIdCol.setCellValueFactory(new PropertyValueFactory<>("bookingId"));
        trainNameCol.setCellValueFactory(new PropertyValueFactory<>("trainName"));
        routeCol.setCellValueFactory(new PropertyValueFactory<>("route"));
        dateCol.setCellValueFactory(new PropertyValueFactory<>("departureTime"));
        seatsCol.setCellValueFactory(new PropertyValueFactory<>("seatsBooked"));
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
    }

    private void loadBookings() {
        ObservableList<Booking> bookingsList = FXCollections.observableArrayList(
            bookingDAO.getBookingsByUsername(currentUsername)
        );

        bookingsTable.setItems(bookingsList);

        if (bookingsList.isEmpty()) {
            showAlert("No Bookings Yet", "You don't have any bookings at the moment.", Alert.AlertType.INFORMATION);
        } else {
            System.out.println("Loaded " + bookingsList.size() + " bookings for user: " + currentUsername);
        }
    }

    @FXML
    private void onCancelBooking(ActionEvent event) {
        Booking selected = bookingsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("No Selection", "Please select a booking to cancel.", Alert.AlertType.WARNING);
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, 
            "Are you sure you want to cancel Booking #" + selected.getBookingId() + "?");
        confirm.setTitle("Cancel Booking");

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            boolean success = bookingDAO.cancelBooking(selected.getBookingId(), currentUsername);
            if (success) {
                showAlert("Cancelled", "Booking cancelled successfully.", Alert.AlertType.INFORMATION);
                loadBookings();   // Refresh
            } else {
                showAlert("Error", "Failed to cancel the booking.", Alert.AlertType.ERROR);
            }
        }
    }

    @FXML
    private void onBackToDashboard(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("UserInterface.fxml"));
            Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Guest Dashboard");
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Cannot return to dashboard.", Alert.AlertType.ERROR);
        }
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showAlert(String title, String message) {
        showAlert(title, message, Alert.AlertType.INFORMATION);
    }

    // This method will be called when navigating from Dashboard
    public void setUsername(String username) {
        this.currentUsername = username;
        if (bookingsTable != null) {
            loadBookings();
        }
    }
}