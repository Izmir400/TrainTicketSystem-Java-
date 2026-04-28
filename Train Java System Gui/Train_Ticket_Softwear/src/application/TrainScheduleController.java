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
import java.time.LocalDate;

public class TrainScheduleController {

    @FXML private TableView<Train> scheduleTable;
    @FXML private TableColumn<Train, Integer> trainIdCol;
    @FXML private TableColumn<Train, String> trainNameCol;
    @FXML private TableColumn<Train, String> departureStationCol;
    @FXML private TableColumn<Train, String> departureTimeCol;
    @FXML private TableColumn<Train, String> arrivalStationCol;
    @FXML private TableColumn<Train, String> arrivalTimeCol;
    @FXML private TableColumn<Train, Integer> availableSeatsCol;
    @FXML private TableColumn<Train, Double> priceCol;
    @FXML private TableColumn<Train, Void> actionCol;        // For Book button

    @FXML private TextField departureField;
    @FXML private TextField arrivalField;
    @FXML private DatePicker datePicker;
    @FXML private Label statusLabel;

    private final TrainDAO trainDAO = new TrainDAO();

    @FXML
    private void initialize() {
        setupTableColumns();
        datePicker.setValue(LocalDate.now());   // Set today's date by default
        loadAllSchedules();                     // Load all schedules when page opens
    }

    private void setupTableColumns() {
        trainIdCol.setCellValueFactory(new PropertyValueFactory<>("trainId"));
        trainNameCol.setCellValueFactory(new PropertyValueFactory<>("trainName"));
        departureStationCol.setCellValueFactory(new PropertyValueFactory<>("departureStation"));
        arrivalStationCol.setCellValueFactory(new PropertyValueFactory<>("arrivalStation"));
        availableSeatsCol.setCellValueFactory(new PropertyValueFactory<>("availableSeats"));
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));

        // Format departure and arrival time nicely
        departureTimeCol.setCellValueFactory(new PropertyValueFactory<>("departureTime"));
        arrivalTimeCol.setCellValueFactory(new PropertyValueFactory<>("arrivalTime"));

        // Add "Book" button in action column
        addBookButtonToTable();
    }

    private void loadAllSchedules() {
        try {
            ObservableList<Train> trains = FXCollections.observableArrayList(
                trainDAO.getAllTrainSchedules()           // We'll create this method
            );
            scheduleTable.setItems(trains);
            statusLabel.setText("Total trains loaded: " + trains.size());
        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Error loading schedules");
            showAlert("Error", "Failed to load train schedules.", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void searchTrains(ActionEvent event) {
        String from = departureField.getText().trim();
        String to = arrivalField.getText().trim();
        LocalDate date = datePicker.getValue();

        if (date == null) {
            showAlert("Missing Date", "Please select a date.", Alert.AlertType.WARNING);
            return;
        }

        try {
            ObservableList<Train> results = FXCollections.observableArrayList(
                trainDAO.getTrainsForDashboard(from, to, date.toString())
            );

            scheduleTable.setItems(results);
            statusLabel.setText("Found " + results.size() + " trains");

            if (results.isEmpty()) {
                showAlert("No Results", "No trains found for your search.", Alert.AlertType.INFORMATION);
            }
        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Search failed");
            showAlert("Search Error", "Failed to search trains.", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void refreshTable(ActionEvent event) {
        departureField.clear();
        arrivalField.clear();
        datePicker.setValue(LocalDate.now());
        loadAllSchedules();
    }

    @FXML
    private void goBack(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("UserInterface.fxml"));
            Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Dashboard");
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Could not return to dashboard.", Alert.AlertType.ERROR);
        }
    }

    // Add "Book" button in every row
    private void addBookButtonToTable() {
        actionCol.setCellFactory(col -> new TableCell<Train, Void>() {
            private final Button bookButton = new Button("Book");

            {
                bookButton.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white;");
                bookButton.setOnAction(e -> {
                    Train train = getTableView().getItems().get(getIndex());
                    handleBooking(train);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(bookButton);
                }
            }
        });
    }

    private void handleBooking(Train train) {
        // You can open the same booking dialog as in dashboard
        // For simplicity, we'll call the same logic (you can improve later)
        GuestDashboardController tempController = new GuestDashboardController(); // Not ideal, better to extract method
        // Better approach: Show the same dialog directly here

        TextInputDialog dialog = new TextInputDialog("1");
        dialog.setTitle("Book Ticket");
        dialog.setHeaderText("Train: " + train.getTrainName());
        dialog.setContentText("How many seats do you want to book?");

        dialog.showAndWait().ifPresent(seatsStr -> {
            try {
                int seats = Integer.parseInt(seatsStr);
                if (seats < 1) seats = 1;

                double total = (train.getPrice() > 0 ? train.getPrice() : 25.0) * seats;
                showAlert("Booking Initiated", 
                          "Booking for " + seats + " seats on " + train.getTrainName() + 
                          "\nTotal: €" + String.format("%.2f", total), 
                          Alert.AlertType.INFORMATION);

                // TODO: You can call your booking logic here directly

            } catch (NumberFormatException ex) {
                showAlert("Invalid Number", "Please enter a valid number.", Alert.AlertType.ERROR);
            }
        });
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}