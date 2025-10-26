import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class MyOffers extends Frame {
    private int userId;
    private List offerList;
    private Label lblMessage;

    public MyOffers(int userId) {
        this.userId = userId;

        setTitle("My Exchange Offers");
        setSize(640, 400);
        setLayout(null);

        Label lblTitle = new Label("Latest Offers (status)");
        lblTitle.setBounds(30, 30, 400, 25);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 14));
        add(lblTitle);

        offerList = new List();
        offerList.setBounds(30, 60, 570, 260);
        add(offerList);

        lblMessage = new Label("");
        lblMessage.setBounds(30, 330, 500, 25);
        add(lblMessage);

        Button btnClose = new Button("Close");
        btnClose.setBounds(520, 330, 80, 25);
        btnClose.addActionListener(e -> dispose());
        add(btnClose);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                dispose();
            }
        });

        loadOffers(); // Load offers when frame is created

        setVisible(true);
    }

    private void loadOffers() {
        offerList.removeAll();
        lblMessage.setText("");

        try {
            // 1️⃣ Load JDBC Driver
            Class.forName("com.mysql.cj.jdbc.Driver");

            // 2️⃣ Connect to database
            Connection conn = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/gvei", "root", ""
            );

            // 3️⃣ Prepare SQL
            String sql = "SELECT eo.offer_id, v.plate_no, eo.exchange_value, eo.subsidy_percent, eo.status " +
                    "FROM exchange_offers eo JOIN vehicles v ON eo.vehicle_id = v.vehicle_id " +
                    "WHERE v.owner_id = ? ORDER BY eo.offer_id DESC";

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);

            ResultSet rs = ps.executeQuery();

            boolean hasData = false;
            while (rs.next()) {
                hasData = true;
                String row = String.format(
                        "Offer #%d | Plate: %s | Value: %.2f | Subsidy: %.0f%% | %s",
                        rs.getInt("offer_id"),
                        rs.getString("plate_no"),
                        rs.getDouble("exchange_value"),
                        rs.getDouble("subsidy_percent"),
                        rs.getString("status")
                );
                offerList.add(row);
            }

            if (!hasData) {
                lblMessage.setText("No exchange offers found.");
            }

            rs.close();
            ps.close();
            conn.close();

        } catch (ClassNotFoundException e) {
            lblMessage.setText("MySQL Driver not found!");
        } catch (SQLException e) {
            lblMessage.setText("DB Error: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        new MyOffers(1); // Replace 1 with the actual user ID
    }
}
