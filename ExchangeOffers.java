import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class ExchangeOffers extends Frame {
    private Choice vehicleChoice;
    private TextField inputExchangeValue, inputSubsidyPercent, inputStatus;
    private Label lblMessage;

    public ExchangeOffers() {
        setLayout(null);
        setTitle("Exchange Offers");
        setSize(500, 350);

        // --- Labels ---
        Label lblVehicle = new Label("Vehicle ID:");
        Label lblExchangeValue = new Label("Exchange Value:");
        Label lblSubsidy = new Label("Subsidy Percent:");
        Label lblStatus = new Label("Status:");

        lblVehicle.setBounds(40, 70, 150, 25);
        lblExchangeValue.setBounds(40, 100, 150, 25);
        lblSubsidy.setBounds(40, 130, 150, 25);
        lblStatus.setBounds(40, 160, 150, 25);

        add(lblVehicle);
        add(lblExchangeValue);
        add(lblSubsidy);
        add(lblStatus);

        // --- Input Fields ---
        vehicleChoice = new Choice();
        inputExchangeValue = new TextField();
        inputSubsidyPercent = new TextField();
        inputStatus = new TextField("Pending"); // default

        vehicleChoice.setBounds(190, 70, 200, 25);
        inputExchangeValue.setBounds(190, 100, 200, 25);
        inputSubsidyPercent.setBounds(190, 130, 200, 25);
        inputStatus.setBounds(190, 160, 200, 25);

        add(vehicleChoice);
        add(inputExchangeValue);
        add(inputSubsidyPercent);
        add(inputStatus);

        // --- Register Button ---
        Button btnRegister = new Button("Register");
        btnRegister.setBackground(Color.DARK_GRAY);
        btnRegister.setForeground(Color.WHITE);
        btnRegister.setFont(new Font("Serif", Font.BOLD, 16));
        btnRegister.setBounds(190, 210, 120, 35);
        add(btnRegister);

        // --- Message Label ---
        lblMessage = new Label("");
        lblMessage.setBounds(40, 260, 400, 25);
        add(lblMessage);

        // Load vehicle IDs from database
        loadVehicleIds();

        // Register Button Action
        btnRegister.addActionListener(e -> insertOffer());

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                dispose();
            }
        });

        setVisible(true);
    }

    private void loadVehicleIds() {
        try (Connection conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/gvei", "root", "");
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT vehicle_id, plate_no FROM vehicles")) {

            while (rs.next()) {
                vehicleChoice.add(rs.getInt("vehicle_id") + " - " + rs.getString("plate_no"));
            }
        } catch (SQLException ex) {
            lblMessage.setText("Error loading vehicles: " + ex.getMessage());
        }
    }

    private void insertOffer() {
        if (vehicleChoice.getItemCount() == 0) {
            lblMessage.setText("No vehicles found!");
            return;
        }

        String selectedVehicle = vehicleChoice.getSelectedItem();
        int vehicleId = Integer.parseInt(selectedVehicle.split(" - ")[0]);
        double exchangeValue = Double.parseDouble(inputExchangeValue.getText());
        double subsidyPercent = Double.parseDouble(inputSubsidyPercent.getText());
        String status = inputStatus.getText();

        String sql = "INSERT INTO exchange_offers (vehicle_id, exchange_value, subsidy_percent, status) VALUES (?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/gvei", "root", "");
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, vehicleId);
            ps.setDouble(2, exchangeValue);
            ps.setDouble(3, subsidyPercent);
            ps.setString(4, status);

            int rows = ps.executeUpdate();
            if (rows > 0) {
                lblMessage.setText("Exchange offer registered successfully!");
                inputExchangeValue.setText("");
                inputSubsidyPercent.setText("");
                inputStatus.setText("Pending");
            }

        } catch (SQLException ex) {
            lblMessage.setText("Database Error: " + ex.getMessage());
        } catch (NumberFormatException ex) {
            lblMessage.setText("Enter valid numeric values.");
        }
    }

    public static void main(String[] args) {
        new ExchangeOffers();
    }
}
