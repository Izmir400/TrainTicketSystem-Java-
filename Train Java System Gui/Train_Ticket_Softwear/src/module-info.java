module Train_Ticket_Softwear {
	requires javafx.controls;
	requires javafx.fxml;
	requires java.sql;
	requires jdk.internal.md;
	requires javafx.graphics;
	
	opens application to javafx.graphics, javafx.fxml;
	exports application;
	
}
