package application;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;

public class UserInterfaceController {

    @FXML
    private Button dashboardButton;

    @FXML
    private Button bookTicketButton;

    @FXML
    private Button logoutButton;

    @FXML
    private AnchorPane mainContent; // optional: area where views will load

    // Handle Dashboard button click
    @FXML
    private void onDashboardClick(ActionEvent event) {
        System.out.println("Dashboard clicked!");
        // TODO: Load dashboard view into mainContent
    }

    // Handle Book Ticket button click
    @FXML
    private void onBookTicketClick(ActionEvent event) {
        System.out.println("Book Ticket clicked!");
        // TODO: Load booking view into mainContent
    }

    // Handle Logout button click
    @FXML
    private void onLogoutClick(ActionEvent event) {
        System.out.println("Logout clicked!");
        // TODO: Perform logout logic (clear session, return to login screen)
        
    }
}