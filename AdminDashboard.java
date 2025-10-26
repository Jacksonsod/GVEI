import java.awt.*;
import java.awt.event.*;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.ArrayList;

public class AdminDashboard extends Frame implements ActionListener {

    private TextField txtSearch;
    private Button btnRefresh;
    private Button btnApprove;
    private Button btnReject;
    private Button btnExport;
    private Button btnLogout;
    private List offerList;
    private Label lblStats;
    private StatsCanvas statsCanvas;

    // ✅ Connection method using MySQL and database gvei
    private Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/gvei",
                    "root",
                    ""
            );
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL Driver not found: " + e.getMessage());
        }
    }

    // Represents one record in exchange_offers
    private static class OfferRow {
        int offerId, vehicleId, ownerId;
        String plate, status;
        double value, subsidy;

        OfferRow(int offerId, int vehicleId, String plate, double value, double subsidy, String status, int ownerId) {
            this.offerId = offerId;
            this.vehicleId = vehicleId;
            this.plate = plate;
            this.value = value;
            this.subsidy = subsidy;
            this.status = status;
            this.ownerId = ownerId;
        }

        @Override
        public String toString() {
            return String.format("#%d | Plate:%s | Value:%.2f | Subsidy:%.0f%% | %s",
                    offerId, plate, value, subsidy, status);
        }
    }

    private java.util.List<OfferRow> currentRows = new ArrayList<>();

    public AdminDashboard() {

        setTitle("Admin Dashboard ~ GVEI");
        setSize(900, 600);
        setLayout(null);

        Label lblWelcome = new Label("Welcome to Admin Dashboard");
        lblWelcome.setFont(new Font("Arial", Font.BOLD, 16));
        lblWelcome.setBounds(30, 40, 400, 24);
        add(lblWelcome);

        txtSearch = new TextField();
        txtSearch.setBounds(30, 80, 260, 24);
        add(txtSearch);

        btnRefresh = new Button("Search/Refresh");
        btnApprove = new Button("Approve");
        btnReject = new Button("Reject");
        btnExport = new Button("Export CSV");
        btnLogout = new Button("Logout");

        btnRefresh.setBounds(300, 80, 110, 24);
        btnApprove.setBounds(420, 80, 80, 24);
        btnReject.setBounds(505, 80, 70, 24);
        btnExport.setBounds(580, 80, 90, 24);
        btnLogout.setBounds(675, 80, 80, 24);

        add(btnRefresh);
        add(btnApprove);
        add(btnReject);
        add(btnExport);
        add(btnLogout);

        offerList = new List();
        offerList.setBounds(30, 120, 640, 350);
        add(offerList);

        lblStats = new Label("");
        lblStats.setBounds(30, 480, 640, 24);
        add(lblStats);

        statsCanvas = new StatsCanvas();
        statsCanvas.setBounds(690, 120, 180, 200);
        add(statsCanvas);

        btnRefresh.addActionListener(this);
        btnApprove.addActionListener(this);
        btnReject.addActionListener(this);
        btnExport.addActionListener(this);
        btnLogout.addActionListener(this);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                dispose();
                System.exit(0);
            }
        });

        loadOffers();
        updateStats();
        setVisible(true);
    }

    private void loadOffers() {
        currentRows.clear();
        offerList.removeAll();
        String q = txtSearch.getText().trim();
        String sql = "SELECT eo.offer_id, eo.vehicle_id, v.plate_no, eo.exchange_value, eo.subsidy_percent, eo.status, v.owner_id " +
                "FROM exchange_offers eo JOIN vehicles v ON eo.vehicle_id = v.vehicle_id " +
                "LEFT JOIN users u ON v.owner_id = u.user_id " +
                "WHERE (? = '' OR v.plate_no LIKE ? OR u.name LIKE ? OR u.email LIKE ?) " +
                "ORDER BY eo.offer_id DESC";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, q);
            String like = "%" + q + "%";
            ps.setString(2, like);
            ps.setString(3, like);
            ps.setString(4, like);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    OfferRow row = new OfferRow(
                            rs.getInt(1),
                            rs.getInt(2),
                            rs.getString(3),
                            rs.getDouble(4),
                            rs.getDouble(5),
                            rs.getString(6),
                            rs.getInt(7)
                    );
                    currentRows.add(row);
                    offerList.add(row.toString());
                }
            }
        } catch (SQLException ex) {
            offerList.add("Error: " + ex.getMessage());
        }
    }

    private void updateStats() {
        String sql = "SELECT " +
                "SUM(CASE WHEN status='Approved' THEN 1 ELSE 0 END) AS approved, " +
                "SUM(CASE WHEN status='Pending' THEN 1 ELSE 0 END) AS pending, " +
                "SUM(CASE WHEN status='Rejected' THEN 1 ELSE 0 END) AS rejected, " +
                "COUNT(*) AS total, " +
                "COALESCE(SUM(CASE WHEN status='Approved' THEN subsidy_percent ELSE 0 END),0) AS total_subsidy " +
                "FROM exchange_offers";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                int approved = rs.getInt("approved");
                int pending = rs.getInt("pending");
                int rejected = rs.getInt("rejected");
                int total = rs.getInt("total");
                double totalSubsidy = rs.getDouble("total_subsidy");
                double carbonReduction = approved * 2.3; // Example calculation
                lblStats.setText(String.format(
                        "Total: %d | Approved: %d | Pending: %d | Rejected: %d | Total Subsidy %%: %.0f | Carbon reduction ~ %.1f tCO2",
                        total, approved, pending, rejected, totalSubsidy, carbonReduction));
                statsCanvas.setData(approved, pending, rejected);
                statsCanvas.repaint();
            }
        } catch (SQLException ex) {
            lblStats.setText("Stats error: " + ex.getMessage());
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnRefresh) {
            loadOffers();
            updateStats();
        } else if (e.getSource() == btnApprove) {
            changeStatus("Approved");
        } else if (e.getSource() == btnReject) {
            changeStatus("Rejected");
        } else if (e.getSource() == btnExport) {
            exportCsv();
        } else if (e.getSource() == btnLogout) {
            dispose();
            new Login(); // ← assumes your Login.java exists
        }
    }

    private void changeStatus(String status) {
        int idx = offerList.getSelectedIndex();
        if (idx < 0 || idx >= currentRows.size()) return;
        OfferRow row = currentRows.get(idx);
        String sql = "UPDATE exchange_offers SET status=? WHERE offer_id=?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, row.offerId);
            ps.executeUpdate();
            loadOffers();
            updateStats();
        } catch (SQLException ex) {
            showErrorDialog("Error updating status: " + ex.getMessage());
        }
    }

    private void exportCsv() {
        FileDialog fd = new FileDialog(this, "Export CSV", FileDialog.SAVE);
        fd.setFile("offers.csv");
        fd.setVisible(true);
        String dir = fd.getDirectory();
        String file = fd.getFile();
        if (dir == null || file == null) return;
        String path = dir + file;
        try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(path), StandardCharsets.UTF_8))) {
            pw.println("offer_id,vehicle_id,plate,exchange_value,subsidy_percent,status,owner_id");
            for (OfferRow r : currentRows) {
                pw.printf("%d,%d,%s,%.2f,%.0f%%,%s,%d%n",
                        r.offerId, r.vehicleId, r.plate, r.value, r.subsidy, r.status, r.ownerId);
            }
        } catch (Exception ex) {
            showErrorDialog("Export failed: " + ex.getMessage());
        }
    }

    private void showErrorDialog(String message) {
        Dialog d = new Dialog(this, "Error", true);
        d.setLayout(new FlowLayout());
        d.add(new Label(message));
        Button b = new Button("Close");
        b.addActionListener(ae -> d.dispose());
        d.add(b);
        d.setSize(400, 120);
        d.setVisible(true);
    }

    static class StatsCanvas extends Canvas {
        int approved = 0, pending = 0, rejected = 0;
        void setData(int a, int p, int r) { approved = a; pending = p; rejected = r; }

        @Override
        public void paint(Graphics g) {
            int w = getWidth(), h = getHeight();
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, w, h);
            int max = Math.max(1, Math.max(approved, Math.max(pending, rejected)));
            int barWidth = Math.max(20, (w - 100) / 3);
            int x1 = 20, x2 = x1 + barWidth + 30, x3 = x2 + barWidth + 30;
            int h1 = (int) ((h - 40) * (approved / (double) max));
            int h2 = (int) ((h - 40) * (pending / (double) max));
            int h3 = (int) ((h - 40) * (rejected / (double) max));

            g.setColor(Color.GREEN);
            g.fillRect(x1, h - 20 - h1, barWidth, h1);
            g.setColor(Color.ORANGE);
            g.fillRect(x2, h - 20 - h2, barWidth, h2);
            g.setColor(Color.RED);
            g.fillRect(x3, h - 20 - h3, barWidth, h3);

            g.setColor(Color.BLACK);
            g.drawString("Approved", x1, h - 5);
            g.drawString("Pending", x2, h - 5);
            g.drawString("Rejected", x3, h - 5);
        }
    }

    // ✅ To run directly for testing:
    public static void main(String[] args) {
        new AdminDashboard();
    }
}
