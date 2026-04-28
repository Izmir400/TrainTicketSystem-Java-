package application;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button signupButton; // <-- add this
  

    @FXML
    private void onLoginClick() {
        String user = usernameField.getText();
        String pass = passwordField.getText();

        if (user.isEmpty() || pass.isEmpty()) {
            showAlert("Error", "Please enter both username and password");
            return;
        }

        try (Connection conn = DBUtil.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(
                "SELECT * FROM users WHERE username=? AND password=?");
            stmt.setString(1, user);
            stmt.setString(2, pass);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                showAlert("Login Successful", "Welcome " + user);
                FXMLLoader loader = new FXMLLoader(getClass().getResource("UserInterface.fxml"));
                Parent root = loader.load();

                // Pass username to DashboardController if needed
                GuestDashboardController controller = loader.getController();  

                Stage stage = (Stage) usernameField.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.show();
            } else {
                showAlert("Login Failed", "Invalid credentials");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Database connection failed");
        }
    }

    @FXML
    private void onSignupClick() throws IOException {
        // Make sure the filename matches exactly: Signup.fxml
        Parent root = FXMLLoader.load(getClass().getResource("/application/SingUp.fxml"));
        Stage stage = (Stage) signupButton.getScene().getWindow();
        stage.setScene(new Scene(root));
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK);
        alert.setTitle(title);
        alert.showAndWait();
    }
}
