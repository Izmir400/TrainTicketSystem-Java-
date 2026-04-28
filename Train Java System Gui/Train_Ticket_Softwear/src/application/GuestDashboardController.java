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
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

public class GuestDashboardController {

    // Sidebar Buttons
    @FXML private Button homeButton;
    @FXML private Button searchTrainsButton;
    @FXML private Button bookingsButton;
    @FXML private Button offersButton;
    @FXML private Button supportButton;
    @FXML private Button logoutButton;

    // Quick Search
    @FXML private TextField fromField;
    @FXML private TextField toField;
    @FXML private DatePicker datePicker;
    @FXML private Button searchButton;

    // Table
    @FXML private TableView<Train> trainTable;
    @FXML private TableColumn<Train, String> trainNameCol;
    @FXML private TableColumn<Train, String> departureCol;
    @FXML private TableColumn<Train, String> arrivalCol;
    @FXML private TableColumn<Train, Timestamp> departureTimeCol;
    @FXML private TableColumn<Train, Timestamp> arrivalTimeCol;
    @FXML private TableColumn<Train, Integer> seatsCol;

    private final TrainDAO trainDAO = new TrainDAO();

    @FXML
    private void initialize() {
        setupTableColumns();
        datePicker.setValue(LocalDate.now());

        // Load today's trains automatically when dashboard opens
        loadDefaultTrains();

        // Double-click row to book
        trainTable.setRowFactory(tv -> {
            TableRow<Train> row = new TableRow<>();
            row.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2 && !row.isEmpty()) {
                    openBookingForSelectedTrain();
                }
            });
            return row;
        });
    }

    // Load trains for today (Dashboard startup)
    private void loadDefaultTrains() {
        try {
            String today = LocalDate.now().toString();
            ObservableList<Train> trains = FXCollections.observableArrayList(
                trainDAO.getTrainsForDashboard("", "", today)
            );

            trainTable.setItems(trains);

            if (trains.isEmpty()) {
                showAlert("No Trains Available", 
                          "No trains found for today.\nTry searching for other dates.", 
                          Alert.AlertType.INFORMATION);
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Loading Error", "Failed to load trains from database.", Alert.AlertType.ERROR);
        }
    }

    private void setupTableColumns() {
        trainNameCol.setCellValueFactory(new PropertyValueFactory<>("trainName"));
        departureCol.setCellValueFactory(new PropertyValueFactory<>("departureStation"));
        arrivalCol.setCellValueFactory(new PropertyValueFactory<>("arrivalStation"));
        departureTimeCol.setCellValueFactory(new PropertyValueFactory<>("departureTime"));
        arrivalTimeCol.setCellValueFactory(new PropertyValueFactory<>("arrivalTime"));
        seatsCol.setCellValueFactory(new PropertyValueFactory<>("availableSeats"));
    }

    // Booking logic used by double-click and "Generate Bill" button
 // Updated - Now actually books the ticket in database
    //-----------------Booking Method------------
 // ==================== FULL BOOKING METHOD WITH SEAT CLASS ====================
    private void openBookingForSelectedTrain() {
        Train selectedTrain = trainTable.getSelectionModel().getSelectedItem();
        if (selectedTrain == null) {
            showAlert("No Selection", "Please select a train first!", Alert.AlertType.WARNING);
            return;
        }

        // Step 1: Ask for number of seats
        TextInputDialog seatsDialog = new TextInputDialog("1");
        seatsDialog.setTitle("Book Tickets");
        seatsDialog.setHeaderText("How many seats do you want to book?");
        seatsDialog.setContentText("Number of seats:");
        
        Optional<String> seatsResult = seatsDialog.showAndWait();
        if (seatsResult.isEmpty()) return;

        int numSeats;
        try {
            numSeats = Integer.parseInt(seatsResult.get().trim());
            if (numSeats < 1) numSeats = 1;
        } catch (Exception e) {
            showAlert("Invalid Input", "Please enter a valid number of seats.", Alert.AlertType.ERROR);
            return;
        }

        // Step 2: Ask for Seat Class (This was missing in last version)
        ChoiceDialog<String> classDialog = new ChoiceDialog<>("Economy", "Economy", "Business", "First Class");
        classDialog.setTitle("Select Seat Class");
        classDialog.setHeaderText("Choose your preferred class");
        classDialog.setContentText("Seat Class:");

        Optional<String> classResult = classDialog.showAndWait();
        String seatClass = classResult.orElse("Economy");

        // Calculate total cost
        double pricePerSeat = selectedTrain.getPrice() > 0 ? selectedTrain.getPrice() : 25.0;
        double totalCost = pricePerSeat * numSeats;

        // Current username (change if needed)
        String currentUsername = "izmir";

        BookingDAO bookingDAO = new BookingDAO();

        boolean booked = bookingDAO.bookTicket(
            selectedTrain.getTrainId(), 
            currentUsername, 
            numSeats, 
            totalCost
        );

        try {
            generateTextBill(selectedTrain, seatClass, numSeats, totalCost);

            if (booked) {
                showAlert("Booking Successful!", 
                          "You booked " + numSeats + " " + seatClass + " seats.\nTotal: €" + String.format("%.2f", totalCost),
                          Alert.AlertType.INFORMATION);
            } else {
                showAlert("Booking Failed", "Could not save booking to database.", Alert.AlertType.ERROR);
            }
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to generate bill file.", Alert.AlertType.ERROR);
        }
    }

    // ==================== Text Bill Generator ====================
    private void generateTextBill(Train train, String seatClass, int numSeats, double totalCost) throws IOException {
        String fileName = "Train_Bill_" + System.currentTimeMillis() + ".txt";
        File file = new File(fileName);

        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            writer.println("=====================================================");
            writer.println("               TRAIN TICKET BILL                    ");
            writer.println("=====================================================");
            writer.println();
            writer.println("Date : " + LocalDate.now());
            writer.println("Time : " + LocalTime.now().withNano(0));
            writer.println();
            writer.println("-----------------------------------------------------");
            writer.println("TRAIN DETAILS");
            writer.println("-----------------------------------------------------");
            writer.println(String.format("%-15s : %s", "Train Name", train.getTrainName()));
            writer.println(String.format("%-15s : %s", "From", train.getDepartureStation()));
            writer.println(String.format("%-15s : %s", "To", train.getArrivalStation()));
            writer.println(String.format("%-15s : %s", "Departure", train.getDepartureTime()));
            writer.println(String.format("%-15s : %s", "Arrival", train.getArrivalTime()));
            writer.println(String.format("%-15s : %d", "Available Seats", train.getAvailableSeats()));
            writer.println();
            writer.println("-----------------------------------------------------");
            writer.println("BOOKING DETAILS");
            writer.println("-----------------------------------------------------");
            writer.println(String.format("%-15s : %s", "Seat Class", seatClass));
            writer.println(String.format("%-15s : %d", "Number of Seats", numSeats));
            writer.println(String.format("%-15s : $%.2f", "Price per Seat", 
                           train.getPrice() > 0 ? train.getPrice() : 25.0));
            writer.println("-----------------------------------------------------");
            writer.println(String.format("%-15s : $%.2f", "TOTAL COST", totalCost));
            writer.println();
            writer.println("=====================================================");
            writer.println("Thank you for booking with us!");
            writer.println("Safe Journey ✈️");
            writer.println("=====================================================");
        }

        System.out.println("Bill saved: " + file.getAbsolutePath());

        // Auto-open in Notepad on Windows
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            try {
                Runtime.getRuntime().exec("notepad.exe \"" + file.getAbsolutePath() + "\"");
            } catch (Exception e) {
                System.out.println("Could not open Notepad automatically.");
            }
        }
    }

    // ==================== Alert Methods ====================
    private void showAlert(String title, String message) {
        showAlert(title, message, Alert.AlertType.INFORMATION);
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // ==================== Search ====================
    @FXML
    private void onSearchClick(ActionEvent event) {
        String from = fromField.getText().trim();
        String to = toField.getText().trim();
        LocalDate selectedDate = datePicker.getValue();

        if (selectedDate == null) {
            showAlert("Missing Date", "Please select a date.", Alert.AlertType.WARNING);
            return;
        }

        try {
            ObservableList<Train> results = FXCollections.observableArrayList(
                trainDAO.getTrainsForDashboard(from, to, selectedDate.toString())
            );

            trainTable.setItems(results);

            if (results.isEmpty()) {
                showAlert("No Results", 
                          "No trains found from " + from + " to " + to + " on " + selectedDate,
                          Alert.AlertType.INFORMATION);
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Search Error", "Failed to search trains.", Alert.AlertType.ERROR);
        }
    }

    // ==================== Generate Bill Button ====================
    @FXML
    private void onPdfBillClick(ActionEvent event) {
        openBookingForSelectedTrain();
    }

    // ==================== Sidebar Navigation ====================
    @FXML private void onHomeClick(ActionEvent event) {
        showAlert("Home", "You are already on the dashboard.");
    }

    @FXML private void onSearchTrainsClick(ActionEvent event) {
        fromField.requestFocus();
    }

    @FXML private void onBookingsClick(ActionEvent event) {
        loadView("Bookings.fxml", "My Bookings");
    }

    @FXML private void onOffersClick(ActionEvent event) {
        loadView("Offers.fxml", "Special Offers");
    }

    @FXML private void onSupportClick(ActionEvent event) {
        loadView("Support.fxml", "Customer Support");
    }

    @FXML private void onLogoutClick(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("LogInPage.fxml"));
            Stage stage = (Stage) logoutButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Login");
        } catch (IOException e) {
            showAlert("Error", "Could not return to login page.", Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    // ==================== FIXED: This was missing ====================
    @FXML 
    private void onTrainScheduleClick(ActionEvent event) {
        loadView("TrainSchedule.fxml", "Train Schedule");
    }

    @FXML private void onBookTicketsClick(ActionEvent event) {
        showAlert("Book Tickets", "Search for trains first, then select a train and click Generate Bill or double-click the row.");
    }

    @FXML private void onTravelGuidesClick(ActionEvent event) {
        loadView("TravelGuides.fxml", "Travel Guides");
    }

    @FXML private void onPaymentClick(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Payment.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Payment");
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Loading Error", "Could not load Payment screen.", Alert.AlertType.ERROR);
        }
    }

    @FXML private void onAdminLoginClick(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("Admin.fxml"));
            Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Admin Login");
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Could not load Admin Login.", Alert.AlertType.ERROR);
        }
    }

    // Helper method for loading FXML views
    private void loadView(String fxmlFile, String title) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlFile));
            Stage stage = (Stage) homeButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(title);
        } catch (IOException e) {
            showAlert("Navigation Error", "Could not load " + fxmlFile, Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }
}