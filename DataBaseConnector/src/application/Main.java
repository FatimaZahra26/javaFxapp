package application;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.print.JobSettings;
import javafx.print.PageLayout;
import javafx.print.PageOrientation;
import javafx.print.Paper;
import javafx.print.PrintResolution;
import javafx.print.Printer;
import javafx.print.PrinterAttributes;
import javafx.print.PrinterJob;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.sql.*;

import javax.imageio.ImageIO;



public class Main extends Application {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/ensao";
    private static final String USER_NAME = "root";
    private static final String PASSWORD = "";
    private static final String TABLE_NAME = "student";

    private TableView<Person> tableView = new TableView<>();
    private TextField firstnameField = new TextField();
    private TextField lastField = new TextField();
    private TextField emailField = new TextField();
    private Button addButton = new Button("Add");
    private Button updateButton = new Button("Update");
    private Button deleteButton = new Button("Delete");
    Button printButton = new Button("Print");
    Button viewProfileButton = new Button("Voir profil");
    private ObservableList<Person> personList = FXCollections.observableArrayList();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        BorderPane borderPane = new BorderPane();

        TableColumn<Person, String> firstnameColumn = new TableColumn<>("firstName");
        firstnameColumn.setCellValueFactory(new PropertyValueFactory<>("firstName"));

        TableColumn<Person, String> lastnameColumn = new TableColumn<>("LastName");
        lastnameColumn.setCellValueFactory(new PropertyValueFactory<>("lastName"));

        TableColumn<Person, String> emailColumn = new TableColumn<>("email");
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        
       ;
        tableView.getColumns().addAll(firstnameColumn, lastnameColumn, emailColumn);

        borderPane.setCenter(tableView);

        GridPane formPane = new GridPane();
        formPane.setPadding(new Insets(10));
        formPane.setHgap(5);
        formPane.setVgap(10);

        formPane.add(new Label("FirstName: "), 0, 0);
        formPane.add(firstnameField, 1, 0);

        formPane.add(new Label("LastName: "), 0, 1);
        formPane.add(lastField, 1, 1);

        formPane.add(new Label("Email: "), 0, 2);
        formPane.add(emailField, 1, 2);

        HBox buttonPane = new HBox();
        buttonPane.setSpacing(5);
        buttonPane.setPadding(new Insets(10));
        buttonPane.getChildren().addAll(addButton, updateButton, deleteButton,viewProfileButton);

        VBox rightPane = new VBox();
        rightPane.setSpacing(10);
        rightPane.setPadding(new Insets(10));
        rightPane.getChildren().addAll(formPane, buttonPane);

        borderPane.setRight(rightPane);

        addButton.setOnAction(event -> {
            addPerson();
        });
        updateButton.setOnAction(event -> {
            updatePerson();
        });

        deleteButton.setOnAction(event -> {
            deletePerson();
        });
        printButton.setOnAction(e -> printToPDF1(printButton));
        viewProfileButton.setOnAction(e -> showViewProfileScene());
        tableView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                firstnameField.setText(newValue.getFirstName());
                lastField.setText(String.valueOf(newValue.getLastName()));
                emailField.setText(newValue.getEmail());
            }
        });
        Scene scene = new Scene(borderPane, 600, 400);
        scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());

        primaryStage.setScene(scene);
        primaryStage.show();

        try {
            Connection connection = DriverManager.getConnection(DB_URL, USER_NAME, PASSWORD);
            Statement statement = connection.createStatement();
            String query = "SELECT * FROM " + TABLE_NAME;
            ResultSet resultSet = statement.executeQuery(query);

            while (resultSet.next()) {
                String firstname = resultSet.getString("firstname");
                String lastname = resultSet.getString("lastname");
                String email = resultSet.getString("email");
                Person person = new Person(firstname, lastname, email);
                personList.add(person);
            }
            tableView.setItems(personList);
            resultSet.close();
            statement.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    private void addPerson() {
        String firstname = firstnameField.getText().trim();
        String lastname = lastField.getText().trim();
        String email = emailField.getText().trim();

        if (firstname.isEmpty() || lastname.isEmpty() || email.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Please fill in all fields.");
            alert.showAndWait();
            return;
        }

    

        Person person = new Person(firstname, lastname, email);

        try {
            Connection connection = DriverManager.getConnection(DB_URL, USER_NAME, PASSWORD);
            PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO " + TABLE_NAME + " (firstname, lastname, email) VALUES (?,?,?)");
            preparedStatement.setString(1, person.getFirstName());
            preparedStatement.setString(2, person.getLastName());
            preparedStatement.setString(3, person.getEmail());
            preparedStatement.executeUpdate();

            personList.add(person);

            preparedStatement.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
     
        clearFields();
    }

    private void clearFields() {
        firstnameField.clear();
        lastField.clear();
        emailField.clear();
    }
    private void updatePerson() {
        Person selectedPerson = tableView.getSelectionModel().getSelectedItem();

        if (selectedPerson == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Please select a person.");
            alert.showAndWait();
            return;
        }

        String firstname = firstnameField.getText().trim();
        String lastname = lastField.getText().trim();
        String email = emailField.getText().trim();

        if (firstname.isEmpty() || lastname.isEmpty() || email.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Please fill in all fields.");
            alert.showAndWait();
            return;
        }

        

        Person updatedPerson = new Person(firstname,lastname , email);

        try {
            Connection connection = DriverManager.getConnection(DB_URL, USER_NAME, PASSWORD);
            PreparedStatement preparedStatement = connection.prepareStatement("UPDATE " + TABLE_NAME + " SET firstname =?, lastname =?, email =? WHERE firstname = ? AND lastname = ? AND email = ?");
            preparedStatement.setString(1, updatedPerson.getFirstName());
            preparedStatement.setString(2, updatedPerson.getLastName());
            preparedStatement.setString(3, updatedPerson.getEmail());
            preparedStatement.setString(4, selectedPerson.getFirstName());
            preparedStatement.setString(5, selectedPerson.getLastName());
            preparedStatement.setString(6, selectedPerson.getEmail());
            preparedStatement.executeUpdate();

            int index = personList.indexOf(selectedPerson);
            personList.set(index, updatedPerson);

            preparedStatement.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        clearFields();
    }
    private void deletePerson() {
        Person selectedPerson = tableView.getSelectionModel().getSelectedItem();

        if (selectedPerson == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Please select a person.");
            alert.showAndWait();
            return;
        }

        try {
            Connection connection = DriverManager.getConnection(DB_URL, USER_NAME, PASSWORD);
            PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM " + TABLE_NAME + " WHERE firstname = ? AND lastname = ? AND email = ?");
            preparedStatement.setString(1, selectedPerson.getFirstName());
            preparedStatement.setString(2, selectedPerson.getLastName());
            preparedStatement.setString(3, selectedPerson.getEmail());
            preparedStatement.executeUpdate();

            personList.remove(selectedPerson);

            preparedStatement.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        clearFields();
    }
    private void printSummary() {
        Person selectedStudent = tableView.getSelectionModel().getSelectedItem();
        if (selectedStudent != null) {
            Printer printer = Printer.getDefaultPrinter();
            PageLayout pageLayout = printer.createPageLayout(Paper.A4, PageOrientation.PORTRAIT, Printer.MarginType.DEFAULT);

            PrinterJob printerJob = PrinterJob.createPrinterJob(printer);
            if (printerJob != null && printerJob.showPrintDialog(tableView.getScene().getWindow())) {
                printStudentSummary(selectedStudent, printerJob, pageLayout);
                printerJob.endJob();
            }
        }
    }
    private void printStudentSummary(Person student, PrinterJob printerJob, PageLayout pageLayout) {
        JobSettings jobSettings = printerJob.getJobSettings();
        jobSettings.setPageLayout(pageLayout);
        jobSettings.setJobName("Student Summary");

        PrinterAttributes printerAttributes = printerJob.getPrinter().getPrinterAttributes();
        PrintResolution printResolution = printerAttributes.getDefaultPrintResolution();
        double scaleFactor = printResolution.getFeedResolution() / 72.0;

        double printableWidth = pageLayout.getPrintableWidth();
        double printableHeight = pageLayout.getPrintableHeight();

        double marginLeft = pageLayout.getLeftMargin() * scaleFactor;
        double marginTop = pageLayout.getTopMargin() * scaleFactor;

        double contentWidth = printableWidth - (marginLeft + pageLayout.getRightMargin() * scaleFactor);
        double contentHeight = printableHeight - (marginTop + pageLayout.getBottomMargin() * scaleFactor);

        double x = marginLeft;
        double y = marginTop;

        try {
            printerJob.printPage(pageLayout, createStudentSummaryNode(student, contentWidth, contentHeight));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Node createStudentSummaryNode(Person student, double contentWidth, double contentHeight) {
        VBox summaryLayout = new VBox(10);
        summaryLayout.setPadding(new Insets(10));

        Label titleLabel = new Label("Student Summary");
        titleLabel.setFont(Font.font(18));

        Label nameLabel = new Label("Name: " + student.getFirstName());
        Label addressLabel = new Label("Address: " + student.getLastName());
        Label coursesLabel = new Label("Courses: " + student.getEmail());

        summaryLayout.getChildren().addAll(titleLabel, nameLabel, addressLabel, coursesLabel);

        // Adjust the layout size to fit the printable area
        summaryLayout.setPrefWidth(contentWidth);
        summaryLayout.setPrefHeight(contentHeight);
        summaryLayout.setMaxSize(contentWidth, contentHeight);

        return summaryLayout;
    }
    private void showViewProfileScene() {
        Person selectedStudent = tableView.getSelectionModel().getSelectedItem();
        if (selectedStudent != null) {
            Stage profileStage = new Stage();
            profileStage.setTitle("Profil de l'étudiant");

            
            Label nameLabel = new Label("FirstName: " + selectedStudent.getFirstName());
            Label addressLabel = new Label("LastName: " + selectedStudent.getLastName());
            Label coursesLabel = new Label("Email: " + selectedStudent.getEmail());

            VBox profileLayout = new VBox(10, nameLabel, addressLabel, coursesLabel, printButton);
            profileLayout.setPadding(new Insets(10));

            Scene profileScene = new Scene(profileLayout);
            profileStage.setScene(profileScene);
            profileStage.show();
        }
    }
    private void printToPDF1(Button nodeToPrint) {
        PrinterJob printerJob = PrinterJob.createPrinterJob();
        if (printerJob != null) {
            Printer printer = Printer.getDefaultPrinter();
            PageLayout pageLayout = printer.createPageLayout(Paper.A4, PageOrientation.PORTRAIT, Printer.MarginType.HARDWARE_MINIMUM);

            printerJob.setPrinter(printer);
            printerJob.getJobSettings().setPageLayout(pageLayout);


            boolean printDialogShown = printerJob.showPrintDialog(nodeToPrint.getScene().getWindow());
            if (printDialogShown) {
                boolean success = printerJob.printPage(nodeToPrint);
                if (success) {
                    printerJob.endJob();

                    File outputFile = new File("output.pdf");
                    try {
                        WritableImage snapshot = nodeToPrint.snapshot(new SnapshotParameters(), null);
                        ImageIO.write(SwingFXUtils.fromFXImage(snapshot, null), "png", outputFile);
                        System.out.println("Le PDF a été créé avec succès.");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }




   
}
