import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class AdminDashboard extends JFrame {
    private int adminId;
    private String adminName;

    public AdminDashboard(int userId, String fullName) {
        this.adminId = userId;
        this.adminName = fullName;

        setTitle("PIMS - Administrator Dashboard [" + fullName + "]");
        setSize(1000, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JTabbedPane tabbedPane = new JTabbedPane();

        tabbedPane.addTab("Manage Medicines", createMedicinesPanel());
        tabbedPane.addTab("Manage Suppliers", createSuppliersPanel());
        tabbedPane.addTab("Manage Users", createUsersPanel());
        tabbedPane.addTab("Reports", createReportsPanel());

        JPanel mainPanel = new JPanel(new BorderLayout());
        
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(44, 62, 80));
        JLabel lblHeader = new JLabel("  PIMS Administration Panel", JLabel.LEFT);
        lblHeader.setForeground(Color.WHITE);
        lblHeader.setFont(new Font("Arial", Font.BOLD, 18));
        JButton btnLogout = new JButton("Logout");
        btnLogout.addActionListener(e -> {
            this.dispose();
            new LoginFrame().setVisible(true);
        });
        headerPanel.add(lblHeader, BorderLayout.CENTER);
        headerPanel.add(btnLogout, BorderLayout.EAST);

        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(tabbedPane, BorderLayout.CENTER);

        add(mainPanel);
    }

    // --- TAB 1: MEDICINES CRUD ---
    private JPanel createMedicinesPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        DefaultTableModel model = new DefaultTableModel(new String[]{"ID", "Name", "Company", "Type", "Price", "Stock", "Reorder", "Expiry", "Supplier ID"}, 0);
        JTable table = new JTable(model);

        Runnable loadData = () -> {
            model.setRowCount(0);
            try (Connection conn = DatabaseConnection.getConnection();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT * FROM medicines")) {
                while (rs.next()) {
                    model.addRow(new Object[]{
                            rs.getInt("medicine_id"), rs.getString("name"), rs.getString("company"),
                            rs.getString("medicine_type"), rs.getDouble("price"), rs.getInt("quantity_in_stock"),
                            rs.getInt("reorder_level"), rs.getDate("expiry_date"), rs.getInt("supplier_id")
                    });
                }
            } catch (SQLException e) { e.printStackTrace(); }
        };
        loadData.run();

        JPanel inputPanel = new JPanel(new GridLayout(5, 4, 5, 5));
        JTextField txtName = new JTextField(), txtCompany = new JTextField(), txtType = new JTextField();
        JTextField txtPrice = new JTextField(), txtStock = new JTextField(), txtReorder = new JTextField();
        JTextField txtExpiry = new JTextField("YYYY-MM-DD"), txtSupplier = new JTextField();

        inputPanel.add(new JLabel("Name:")); inputPanel.add(txtName);
        inputPanel.add(new JLabel("Company:")); inputPanel.add(txtCompany);
        inputPanel.add(new JLabel("Type:")); inputPanel.add(txtType);
        inputPanel.add(new JLabel("Price:")); inputPanel.add(txtPrice);
        inputPanel.add(new JLabel("Stock Qty:")); inputPanel.add(txtStock);
        inputPanel.add(new JLabel("Reorder Level:")); inputPanel.add(txtReorder);
        inputPanel.add(new JLabel("Expiry Date:")); inputPanel.add(txtExpiry);
        inputPanel.add(new JLabel("Supplier ID:")); inputPanel.add(txtSupplier);

        JPanel btnPanel = new JPanel();
        JButton btnAdd = new JButton("Add Medicine");
        JButton btnDelete = new JButton("Delete Selected");

        btnAdd.addActionListener(e -> {
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement("INSERT INTO medicines (name, company, medicine_type, price, quantity_in_stock, reorder_level, expiry_date, supplier_id) VALUES (?,?,?,?,?,?,CAST(? AS DATE),?)")) {
                pstmt.setString(1, txtName.getText());
                pstmt.setString(2, txtCompany.getText());
                pstmt.setString(3, txtType.getText());
                pstmt.setDouble(4, Double.parseDouble(txtPrice.getText()));
                pstmt.setInt(5, Integer.parseInt(txtStock.getText()));
                pstmt.setInt(6, Integer.parseInt(txtReorder.getText()));
                pstmt.setString(7, txtExpiry.getText());
                pstmt.setInt(8, Integer.parseInt(txtSupplier.getText()));
                pstmt.executeUpdate();
                loadData.run();
                JOptionPane.showMessageDialog(panel, "Medicine Added!");
            } catch (Exception ex) { JOptionPane.showMessageDialog(panel, "Error: " + ex.getMessage()); }
        });

        btnDelete.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row != -1) {
                int id = (Integer) model.getValueAt(row, 0);
                try (Connection conn = DatabaseConnection.getConnection();
                     PreparedStatement pstmt = conn.prepareStatement("DELETE FROM medicines WHERE medicine_id = ?")) {
                    pstmt.setInt(1, id);
                    pstmt.executeUpdate();
                    loadData.run();
                } catch (SQLException ex) { ex.printStackTrace(); }
            }
        });

        btnPanel.add(btnAdd);
        btnPanel.add(btnDelete);

        JPanel northPanel = new JPanel(new BorderLayout());
        northPanel.add(inputPanel, BorderLayout.CENTER);
        northPanel.add(btnPanel, BorderLayout.SOUTH);

        panel.add(northPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    // --- TAB 2: SUPPLIERS CRUD ---
    private JPanel createSuppliersPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        DefaultTableModel model = new DefaultTableModel(new String[]{"ID", "Name", "Contact Person", "Phone", "Email", "Address"}, 0);
        JTable table = new JTable(model);

        Runnable loadData = () -> {
            model.setRowCount(0);
            try (Connection conn = DatabaseConnection.getConnection();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT * FROM suppliers")) {
                while (rs.next()) {
                    model.addRow(new Object[]{
                            rs.getInt("supplier_id"), rs.getString("name"), rs.getString("contact_person"),
                            rs.getString("phone"), rs.getString("email"), rs.getString("address")
                    });
                }
            } catch (SQLException e) { e.printStackTrace(); }
        };
        loadData.run();

        JPanel inputPanel = new JPanel(new GridLayout(3, 4, 5, 5));
        JTextField txtName = new JTextField(), txtContact = new JTextField();
        JTextField txtPhone = new JTextField(), txtEmail = new JTextField(), txtAddress = new JTextField();

        inputPanel.add(new JLabel("Name:")); inputPanel.add(txtName);
        inputPanel.add(new JLabel("Contact Person:")); inputPanel.add(txtContact);
        inputPanel.add(new JLabel("Phone:")); inputPanel.add(txtPhone);
        inputPanel.add(new JLabel("Email:")); inputPanel.add(txtEmail);
        inputPanel.add(new JLabel("Address:")); inputPanel.add(txtAddress);

        JButton btnAdd = new JButton("Add Supplier");
        btnAdd.addActionListener(e -> {
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement("INSERT INTO suppliers (name, contact_person, phone, email, address) VALUES (?,?,?,?,?)")) {
                pstmt.setString(1, txtName.getText());
                pstmt.setString(2, txtContact.getText());
                pstmt.setString(3, txtPhone.getText());
                pstmt.setString(4, txtEmail.getText());
                pstmt.setString(5, txtAddress.getText());
                pstmt.executeUpdate();
                loadData.run();
                JOptionPane.showMessageDialog(panel, "Supplier Added!");
            } catch (Exception ex) { JOptionPane.showMessageDialog(panel, "Error: " + ex.getMessage()); }
        });

        JPanel top = new JPanel(new BorderLayout());
        top.add(inputPanel, BorderLayout.CENTER);
        top.add(btnAdd, BorderLayout.SOUTH);

        panel.add(top, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    // --- TAB 3: USERS MANAGEMENT ---
    private JPanel createUsersPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        DefaultTableModel model = new DefaultTableModel(new String[]{"ID", "Username", "Role", "Full Name"}, 0);
        JTable table = new JTable(model);

        Runnable loadUsers = () -> {
            model.setRowCount(0);
            try (Connection conn = DatabaseConnection.getConnection();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT user_id, username, role, full_name FROM users")) {
                while (rs.next()) {
                    model.addRow(new Object[]{rs.getInt("user_id"), rs.getString("username"), rs.getString("role"), rs.getString("full_name")});
                }
            } catch (SQLException e) { e.printStackTrace(); }
        };
        loadUsers.run();

        JPanel inputPanel = new JPanel(new GridLayout(2, 4, 5, 5));
        JTextField txtUsername = new JTextField(), txtPassword = new JTextField(), txtFullName = new JTextField();
        JComboBox<String> comboRole = new JComboBox<>(new String[]{"Cashier", "Admin"});

        inputPanel.add(new JLabel("Username:")); inputPanel.add(txtUsername);
        inputPanel.add(new JLabel("Password:")); inputPanel.add(txtPassword);
        inputPanel.add(new JLabel("Full Name:")); inputPanel.add(txtFullName);
        inputPanel.add(new JLabel("Role:")); inputPanel.add(comboRole);

        JButton btnAdd = new JButton("Create User");
        btnAdd.addActionListener(e -> {
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement("INSERT INTO users (username, password, role, full_name) VALUES (?,?,?,?)")) {
                pstmt.setString(1, txtUsername.getText());
                pstmt.setString(2, txtPassword.getText());
                pstmt.setString(3, comboRole.getSelectedItem().toString());
                pstmt.setString(4, txtFullName.getText());
                pstmt.executeUpdate();
                loadUsers.run();
                JOptionPane.showMessageDialog(panel, "User Created!");
            } catch (Exception ex) { JOptionPane.showMessageDialog(panel, "Error: " + ex.getMessage()); }
        });

        JPanel top = new JPanel(new BorderLayout());
        top.add(inputPanel, BorderLayout.CENTER);
        top.add(btnAdd, BorderLayout.SOUTH);

        panel.add(top, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    // --- TAB 4: REPORT GENERATION ---
    private JPanel createReportsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));

        JPanel btnPanel = new JPanel(new FlowLayout());
        JButton btnSales = new JButton("Sales Report");
        JButton btnItemWise = new JButton("Item-Wise Sales Report");
        JButton btnLowStock = new JButton("Low Stock Report");
        JButton btnExpiry = new JButton("Expiry Report (Next 30 Days)");

        btnPanel.add(btnSales);
        btnPanel.add(btnItemWise);
        btnPanel.add(btnLowStock);
        btnPanel.add(btnExpiry);

        JTextArea reportOutput = new JTextArea();
        reportOutput.setFont(new Font("Monospaced", Font.PLAIN, 12));
        reportOutput.setEditable(false);

        btnSales.addActionListener(e -> runReport("SELECT s.sale_id, s.sale_date, s.total_amount, u.full_name FROM sales s LEFT JOIN users u ON s.user_id = u.user_id", reportOutput, "TOTAL SALES REPORT"));
        
        btnItemWise.addActionListener(e -> runReport("SELECT m.name, SUM(si.quantity_sold) as total_qty, SUM(si.quantity_sold * si.price_at_sale) as revenue FROM sale_items si JOIN medicines m ON si.medicine_id = m.medicine_id GROUP BY m.medicine_id, m.name", reportOutput, "ITEM-WISE SALES REPORT"));
        
        btnLowStock.addActionListener(e -> runReport("SELECT medicine_id, name, quantity_in_stock, reorder_level FROM medicines WHERE quantity_in_stock <= reorder_level", reportOutput, "LOW STOCK ALERT REPORT"));
        
        btnExpiry.addActionListener(e -> runReport("SELECT medicine_id, name, quantity_in_stock, expiry_date FROM medicines WHERE expiry_date BETWEEN CURRENT_DATE AND CURRENT_DATE + INTERVAL '30 days'", reportOutput, "MEDICINES EXPIRING IN NEXT 30 DAYS"));

        panel.add(btnPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(reportOutput), BorderLayout.CENTER);
        return panel;
    }

    private void runReport(String query, JTextArea area, String title) {
        StringBuilder sb = new StringBuilder();
        sb.append("========================================================================\n");
        sb.append("                      ").append(title).append("\n");
        sb.append("========================================================================\n\n");

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            for (int i = 1; i <= columnCount; i++) {
                sb.append(String.format("%-20s", metaData.getColumnLabel(i)));
            }
            sb.append("\n------------------------------------------------------------------------\n");

            while (rs.next()) {
                for (int i = 1; i <= columnCount; i++) {
                    sb.append(String.format("%-20s", rs.getString(i)));
                }
                sb.append("\n");
            }

        } catch (SQLException e) {
            sb.append("Error generating report: ").append(e.getMessage());
        }

        area.setText(sb.toString());
    }
}