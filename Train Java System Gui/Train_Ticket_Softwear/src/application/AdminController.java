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
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class AdminController {

    @FXML private TableView<Train> trainTable;
    @FXML private TableColumn<Train, Integer> trainIdCol;
    @FXML private TableColumn<Train, String> trainNameCol;
    @FXML private TableColumn<Train, String> departureCol;
    @FXML private TableColumn<Train, String> arrivalCol;
    @FXML private TableColumn<Train, Timestamp> departureTimeCol;
    @FXML private TableColumn<Train, Timestamp> arrivalTimeCol;
    @FXML private TableColumn<Train, Integer> seatsCol;
    @FXML private TableColumn<Train, Double> priceCol;

    @FXML private TextField trainNameField;
    @FXML private TextField departureStationField;
    @FXML private TextField arrivalStationField;
    @FXML private DatePicker departureDatePicker;
    @FXML private TextField departureTimeField;
    @FXML private TextField arrivalTimeField;
    @FXML private TextField seatsField;
    @FXML private TextField priceField;

    @FXML private Button backButton;

    private final ObservableList<Train> trainList =
            FXCollections.observableArrayList();

    private Train selectedTrain;

    @FXML
    public void initialize() {
        setupTable();
        loadAllTrains();

        trainTable.getSelectionModel().selectedItemProperty()
                .addListener((obs, oldVal, newVal) -> {
                    if (newVal != null) {
                        selectedTrain = newVal;
                        populateFields(newVal);
                    }
                });
    }

    private void setupTable() {
        trainIdCol.setCellValueFactory(new PropertyValueFactory<>("trainId"));
        trainNameCol.setCellValueFactory(new PropertyValueFactory<>("trainName"));
        departureCol.setCellValueFactory(new PropertyValueFactory<>("departureStation"));
        arrivalCol.setCellValueFactory(new PropertyValueFactory<>("arrivalStation"));
        departureTimeCol.setCellValueFactory(new PropertyValueFactory<>("departureTime"));
        arrivalTimeCol.setCellValueFactory(new PropertyValueFactory<>("arrivalTime"));
        seatsCol.setCellValueFactory(new PropertyValueFactory<>("availableSeats"));
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
    }

    // ===================== ADD =====================

    @FXML
    private void onAddTrain(ActionEvent e) {

        if (!validateInputs()) return;

        try (Connection conn = DBUtil.getConnection()) {

            int depId = getStationId(conn, departureStationField.getText().trim());
            int arrId = getStationId(conn, arrivalStationField.getText().trim());

            if (depId == -1 || arrId == -1) {
                showAlert("Error", "Station not found.", Alert.AlertType.ERROR);
                return;
            }

            conn.setAutoCommit(false);

            String sqlTrain =
                    "INSERT INTO trains(train_name) VALUES(?)";

            PreparedStatement psTrain =
                    conn.prepareStatement(sqlTrain,
                            Statement.RETURN_GENERATED_KEYS);

            psTrain.setString(1, trainNameField.getText().trim());
            psTrain.executeUpdate();

            ResultSet rs = psTrain.getGeneratedKeys();
            rs.next();

            int trainId = rs.getInt(1);

            String sqlSchedule =
                    """
                    INSERT INTO train_schedule
                    (train_id, departure_station_id, arrival_station_id,
                     departure_time, arrival_time, available_seats, price)
                    VALUES (?, ?, ?, ?, ?, ?, ?)
                    """;

            PreparedStatement ps = conn.prepareStatement(sqlSchedule);

            ps.setInt(1, trainId);
            ps.setInt(2, depId);
            ps.setInt(3, arrId);
            ps.setTimestamp(4, createTimestamp(
                    departureDatePicker.getValue(),
                    departureTimeField.getText().trim()));

            ps.setTimestamp(5, createTimestamp(
                    departureDatePicker.getValue(),
                    arrivalTimeField.getText().trim()));

            ps.setInt(6, Integer.parseInt(seatsField.getText().trim()));
            ps.setDouble(7, Double.parseDouble(priceField.getText().trim()));

            ps.executeUpdate();

            conn.commit();

            showAlert("Success", "Train added.", Alert.AlertType.INFORMATION);

            loadAllTrains();
            clearFields();

        } catch (Exception ex) {
            ex.printStackTrace();
            showAlert("Error", ex.getMessage(), Alert.AlertType.ERROR);
        }
    }

    // ===================== UPDATE =====================

    @FXML
    private void onUpdateTrain(ActionEvent e) {

        if (selectedTrain == null) {
            showAlert("Warning", "Select a train first.", Alert.AlertType.WARNING);
            return;
        }

        if (!validateInputs()) return;

        try (Connection conn = DBUtil.getConnection()) {

            int depId = getStationId(conn, departureStationField.getText().trim());
            int arrId = getStationId(conn, arrivalStationField.getText().trim());

            conn.setAutoCommit(false);

            PreparedStatement ps1 = conn.prepareStatement(
                    "UPDATE trains SET train_name=? WHERE train_id=?");

            ps1.setString(1, trainNameField.getText().trim());
            ps1.setInt(2, selectedTrain.getTrainId());
            ps1.executeUpdate();

            PreparedStatement ps2 = conn.prepareStatement(
                    """
                    UPDATE train_schedule
                    SET departure_station_id=?,
                        arrival_station_id=?,
                        departure_time=?,
                        arrival_time=?,
                        available_seats=?,
                        price=?
                    WHERE train_id=?
                    """);

            ps2.setInt(1, depId);
            ps2.setInt(2, arrId);
            ps2.setTimestamp(3, createTimestamp(
                    departureDatePicker.getValue(),
                    departureTimeField.getText().trim()));

            ps2.setTimestamp(4, createTimestamp(
                    departureDatePicker.getValue(),
                    arrivalTimeField.getText().trim()));

            ps2.setInt(5, Integer.parseInt(seatsField.getText().trim()));
            ps2.setDouble(6, Double.parseDouble(priceField.getText().trim()));
            ps2.setInt(7, selectedTrain.getTrainId());

            ps2.executeUpdate();

            conn.commit();

            showAlert("Success", "Train updated.", Alert.AlertType.INFORMATION);

            loadAllTrains();
            clearFields();

        } catch (Exception ex) {
            ex.printStackTrace();
            showAlert("Error", ex.getMessage(), Alert.AlertType.ERROR);
        }
    }

    // ===================== DELETE =====================

    @FXML
    private void onDeleteTrain(ActionEvent e) {

        if (selectedTrain == null) {
            showAlert("Warning", "Select a train first.", Alert.AlertType.WARNING);
            return;
        }

        try (Connection conn = DBUtil.getConnection()) {

            conn.setAutoCommit(false);

            PreparedStatement ps1 = conn.prepareStatement(
                    "DELETE FROM train_schedule WHERE train_id=?");

            ps1.setInt(1, selectedTrain.getTrainId());
            ps1.executeUpdate();

            PreparedStatement ps2 = conn.prepareStatement(
                    "DELETE FROM trains WHERE train_id=?");

            ps2.setInt(1, selectedTrain.getTrainId());
            ps2.executeUpdate();

            conn.commit();

            showAlert("Success", "Train deleted.", Alert.AlertType.INFORMATION);

            loadAllTrains();
            clearFields();

        } catch (Exception ex) {
            ex.printStackTrace();
            showAlert("Error", ex.getMessage(), Alert.AlertType.ERROR);
        }
    }

    // ===================== LOAD TABLE =====================

    private void loadAllTrains() {

        trainList.clear();

        String sql =
                """
                SELECT t.train_id,
                       t.train_name,
                       s1.station_name AS dep,
                       s2.station_name AS arr,
                       ts.departure_time,
                       ts.arrival_time,
                       ts.available_seats,
                       ts.price
                FROM trains t
                JOIN train_schedule ts ON t.train_id = ts.train_id
                JOIN stations s1 ON ts.departure_station_id = s1.station_id
                JOIN stations s2 ON ts.arrival_station_id = s2.station_id
                ORDER BY ts.departure_time DESC
                """;

        try (Connection conn = DBUtil.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {

                Train t = new Train();

                t.setTrainId(rs.getInt("train_id"));
                t.setTrainName(rs.getString("train_name"));
                t.setDepartureStation(rs.getString("dep"));
                t.setArrivalStation(rs.getString("arr"));
                t.setDepartureTime(rs.getTimestamp("departure_time"));
                t.setArrivalTime(rs.getTimestamp("arrival_time"));
                t.setAvailableSeats(rs.getInt("available_seats"));
                t.setPrice(rs.getDouble("price"));

                trainList.add(t);
            }

            trainTable.setItems(trainList);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // ===================== HELPERS =====================

    private int getStationId(Connection conn, String stationName)
            throws SQLException {

        PreparedStatement ps = conn.prepareStatement(
                "SELECT station_id FROM stations WHERE station_name=?");

        ps.setString(1, stationName);

        ResultSet rs = ps.executeQuery();

        if (rs.next()) return rs.getInt(1);

        return -1;
    }

    private Timestamp createTimestamp(LocalDate date, String time) {
        LocalDateTime dt =
                LocalDateTime.of(date, LocalTime.parse(time));
        return Timestamp.valueOf(dt);
    }

    private void populateFields(Train t) {

        trainNameField.setText(t.getTrainName());
        departureStationField.setText(t.getDepartureStation());
        arrivalStationField.setText(t.getArrivalStation());

        departureDatePicker.setValue(
                t.getDepartureTime().toLocalDateTime().toLocalDate());

        departureTimeField.setText(
                t.getDepartureTime().toLocalDateTime()
                        .toLocalTime().toString());

        arrivalTimeField.setText(
                t.getArrivalTime().toLocalDateTime()
                        .toLocalTime().toString());

        seatsField.setText(String.valueOf(t.getAvailableSeats()));
        priceField.setText(String.valueOf(t.getPrice()));
    }

    @FXML
    private void onClearFields(ActionEvent e) {
        clearFields();
    }

    private void clearFields() {
        trainNameField.clear();
        departureStationField.clear();
        arrivalStationField.clear();
        departureDatePicker.setValue(null);
        departureTimeField.clear();
        arrivalTimeField.clear();
        seatsField.clear();
        priceField.clear();
        selectedTrain = null;
    }

    private boolean validateInputs() {

        if (trainNameField.getText().isEmpty() ||
                departureStationField.getText().isEmpty() ||
                arrivalStationField.getText().isEmpty() ||
                departureDatePicker.getValue() == null ||
                departureTimeField.getText().isEmpty() ||
                arrivalTimeField.getText().isEmpty() ||
                seatsField.getText().isEmpty() ||
                priceField.getText().isEmpty()) {

            showAlert("Validation",
                    "Fill all fields.",
                    Alert.AlertType.WARNING);
            return false;
        }

        return true;
    }

    @FXML
    private void onBackToDashboard(ActionEvent e) {
        try {
            Parent root = FXMLLoader.load(
                    getClass().getResource("UserInterface.fxml"));

            Stage stage =
                    (Stage) backButton.getScene().getWindow();

            stage.setScene(new Scene(root));
            stage.setTitle("Dashboard");

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void showAlert(String title,
                           String msg,
                           Alert.AlertType type) {

        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}