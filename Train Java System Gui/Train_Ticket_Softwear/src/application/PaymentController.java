package application;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.beans.value.ChangeListener;

public class PaymentController {

    @FXML
    private TextField cardNumberField;
    @FXML
    private TextField expiryField;
    @FXML
    private TextField cvvField;
    @FXML
    private TextField nameField;
    @FXML
    private Button payButton;
    @FXML
    private Button printBillButton;

    // Important: This field will hold the selected train data
    private Train selectedTrain;

    @FXML
    private void initialize() {
        // Input restrictions and formatting
        setupCardNumberField();
        setupExpiryField();
        setupCvvField();

        // Disable Pay button until all fields are filled
        setupPayButtonBinding();
    }

    private void setupCardNumberField() {
        cardNumberField.textProperty().addListener((observable, oldValue, newValue) -> {
            String cleaned = newValue.replaceAll("[^\\d\\s]", "");
            if (cleaned.length() > 19) {
                cleaned = cleaned.substring(0, 19);
            }
            cleaned = cleaned.replaceAll("\\s", "");
            StringBuilder formatted = new StringBuilder();
            for (int i = 0; i < cleaned.length(); i++) {
                if (i > 0 && i % 4 == 0) {
                    formatted.append(" ");
                }
                formatted.append(cleaned.charAt(i));
            }
            if (!formatted.toString().equals(newValue)) {
                cardNumberField.setText(formatted.toString());
                cardNumberField.positionCaret(formatted.length());
            }
        });
    }

    private void setupExpiryField() {
        expiryField.textProperty().addListener((obs, old, newVal) -> {
            String cleaned = newVal.replaceAll("[^\\d/]", "");
            if (cleaned.length() > 5) cleaned = cleaned.substring(0, 5);
            if (cleaned.length() == 2 && !cleaned.contains("/")) {
                cleaned += "/";
            }
            if (!cleaned.equals(newVal)) {
                expiryField.setText(cleaned);
                expiryField.positionCaret(cleaned.length());
            }
        });
    }

    private void setupCvvField() {
        cvvField.textProperty().addListener((obs, old, newVal) -> {
            String cleaned = newVal.replaceAll("[^\\d]", "");
            if (cleaned.length() > 4) cleaned = cleaned.substring(0, 4);
            if (!cleaned.equals(newVal)) {
                cvvField.setText(cleaned);
                cvvField.positionCaret(cleaned.length());
            }
        });
    }

    private void setupPayButtonBinding() {
        ChangeListener<String> listener = (obs, old, newVal) -> updatePayButtonState();
        cardNumberField.textProperty().addListener(listener);
        expiryField.textProperty().addListener(listener);
        cvvField.textProperty().addListener(listener);
        nameField.textProperty().addListener(listener);
    }

    private void updatePayButtonState() {
        boolean isFilled = !cardNumberField.getText().trim().isEmpty() &&
                           !expiryField.getText().trim().isEmpty() &&
                           !cvvField.getText().trim().isEmpty() &&
                           !nameField.getText().trim().isEmpty();
        payButton.setDisable(!isFilled);
    }

    @FXML
    private void onPayClick() {
        String cardNumber = cardNumberField.getText().replaceAll("\\s+", "");
        String expiry = expiryField.getText().trim();
        String cvv = cvvField.getText().trim();
        String name = nameField.getText().trim();

        if (cardNumber.isEmpty() || expiry.isEmpty() || cvv.isEmpty() || name.isEmpty()) {
            showAlert("Error", "❌ Please fill all fields.");
            return;
        }

        if (cardNumber.length() < 13 || cardNumber.length() > 19 || !cardNumber.matches("\\d+")) {
            showAlert("Error", "❌ Invalid card number. Must be 13-19 digits.");
            return;
        }

        if (!expiry.matches("(0[1-9]|1[0-2])/\\d{2}")) {
            showAlert("Error", "❌ Expiry date must be in MM/YY format (e.g., 12/28).");
            return;
        }

        if (!cvv.matches("\\d{3,4}")) {
            showAlert("Error", "❌ CVV must be 3 or 4 digits.");
            return;
        }

        if (!isValidLuhn(cardNumber)) {
            showAlert("Error", "❌ Invalid card number.");
            return;
        }

        // Simulate successful payment
        showAlert("Payment Successful", "✅ Payment confirmed for " + name);
    }

    @FXML
    private void onPrintBillClick() {
        if (selectedTrain == null) {
            showAlert("Error", "No train selected for booking.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Bill.fxml"));
            Parent root = loader.load();

            BillController billController = loader.getController();
            billController.setTrain(selectedTrain);

            Stage stage = (Stage) printBillButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Train Ticket Bill");
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Unable to open bill view: " + e.getMessage());
        }
    }

    // Setter method to receive the selected train from previous screen (e.g., booking or dashboard)
    public void setSelectedTrain(Train selectedTrain) {
        this.selectedTrain = selectedTrain;
    }

    private boolean isValidLuhn(String cardNumber) {
        int sum = 0;
        boolean alternate = false;

        for (int i = cardNumber.length() - 1; i >= 0; i--) {
            int digit = Character.getNumericValue(cardNumber.charAt(i));

            if (alternate) {
                digit *= 2;
                if (digit > 9) digit -= 9;
            }
            sum += digit;
            alternate = !alternate;
        }
        return (sum % 10 == 0);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}