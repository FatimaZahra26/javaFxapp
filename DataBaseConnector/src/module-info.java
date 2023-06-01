module DataBaseConnector {
	requires javafx.controls;
	requires javafx.base;
	requires javafx.graphics;
	requires java.sql;
	requires java.desktop;
	requires javafx.swing;
	
	opens application to javafx.graphics, javafx.fxml;
	exports application;
}
