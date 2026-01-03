package admin;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.sql.*;

class InventoryWindow extends Frame implements ActionListener, WindowListener {

    TextField searchField;
    Button searchButton, refreshButton, saveButton;
    Label alertLabel;
    JTable table;
    DefaultTableModel tableModel;

    String url = "jdbc:mysql://localhost:3306/ecommerce_db?useSSL=false&serverTimezone=UTC";
    String dbUsername = "root";
    String dbPassword = "";

    public InventoryWindow() {
        setTitle("Inventory Management");
        setLayout(new BorderLayout());

        Label titleLabel = new Label("Inventory", Label.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
        add(titleLabel, BorderLayout.NORTH);

        String[] columns = {"ID", "Product Name", "Price", "Quantity"};
        tableModel = new DefaultTableModel(columns, 0);
        table = new JTable(tableModel);

        add(new JScrollPane(table), BorderLayout.CENTER);

        Panel controlPanel = new Panel();
        searchField = new TextField(15);
        searchButton = new Button("Search");
        refreshButton = new Button("Refresh");
        saveButton = new Button("Save Changes");

        controlPanel.add(new Label("Product:"));
        controlPanel.add(searchField);
        controlPanel.add(searchButton);
        controlPanel.add(refreshButton);
        controlPanel.add(saveButton);

        add(controlPanel, BorderLayout.SOUTH);

        alertLabel = new Label();
        alertLabel.setForeground(Color.RED);
        add(alertLabel, BorderLayout.NORTH);

        searchButton.addActionListener(this);
        refreshButton.addActionListener(this);
        saveButton.addActionListener(this);
        addWindowListener(this);

        loadFromDatabaseToLinkedList();
        displayFromLinkedListToTable();
        checkThresholdAlert();

        setSize(700, 450);
        setVisible(true);
    }

    /* ---------------- DATABASE → LINKEDLIST ---------------- */
    private void loadFromDatabaseToLinkedList() {
        InventoryData.inventory.clear();

        String query = "SELECT * FROM products";
        try (Connection con = DriverManager.getConnection(url, dbUsername, dbPassword);
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(query)) {

            while (rs.next()) {
                String[] item = {
                        rs.getString("id"),
                        rs.getString("name"),
                        String.valueOf(rs.getDouble("price")),
                        String.valueOf(rs.getInt("quantity"))
                };
                InventoryData.inventory.add(item);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /* ---------------- LINKEDLIST → TABLE ---------------- */
    private void displayFromLinkedListToTable() {
        tableModel.setRowCount(0);
        for (String[] item : InventoryData.inventory) {
            tableModel.addRow(item);
        }
    }

    /* ---------------- TABLE → LINKEDLIST ---------------- */
    private void updateLinkedListFromTable() {
        InventoryData.inventory.clear();
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            String[] item = new String[4];
            for (int j = 0; j < 4; j++) {
                item[j] = tableModel.getValueAt(i, j).toString();
            }
            InventoryData.inventory.add(item);
        }
    }

    /* ---------------- LINKEDLIST → DATABASE ---------------- */
    private void saveLinkedListToDatabase() {
        updateLinkedListFromTable();

        String query = "UPDATE products SET name=?, price=?, quantity=? WHERE id=?";
        try (Connection con = DriverManager.getConnection(url, dbUsername, dbPassword);
             PreparedStatement ps = con.prepareStatement(query)) {

            for (String[] item : InventoryData.inventory) {
                ps.setString(1, item[1]);
                ps.setDouble(2, Double.parseDouble(item[2]));
                ps.setInt(3, Integer.parseInt(item[3]));
                ps.setInt(4, Integer.parseInt(item[0]));
                ps.executeUpdate();
            }
            JOptionPane.showMessageDialog(this, "Changes saved to database!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /* ---------------- THRESHOLD ALERT ---------------- */
    private void checkThresholdAlert() {
        int threshold = 5;
        StringBuilder msg = new StringBuilder();

        for (String[] item : InventoryData.inventory) {
            int qty = Integer.parseInt(item[3]);
            if (qty < threshold) {
                msg.append("Low Stock: ").append(item[1]).append(" (").append(qty).append(")  ");
            }
        }
        alertLabel.setText(msg.length() > 0 ? msg.toString() : "All items above threshold");
    }

    /* ---------------- EVENTS ---------------- */
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == refreshButton) {
            saveLinkedListToDatabase();
            loadFromDatabaseToLinkedList();
            displayFromLinkedListToTable();
            checkThresholdAlert();
        }

        if (e.getSource() == saveButton) {
            saveLinkedListToDatabase();
        }

        if (e.getSource() == searchButton) {
            String name = searchField.getText();
            for (String[] item : InventoryData.inventory) {
                if (item[1].equalsIgnoreCase(name)) {
                    JOptionPane.showMessageDialog(this,
                            "Found: " + item[1] + ", Price: " + item[2] + ", Qty: " + item[3]);
                    return;
                }
            }
            JOptionPane.showMessageDialog(this, "Product not found");
        }
    }

    /* ---------------- WINDOW CLOSE ---------------- */
    public void windowClosing(WindowEvent we) {
        saveLinkedListToDatabase();
        dispose();
    }

    public void windowOpened(WindowEvent we) {}
    public void windowClosed(WindowEvent we) {}
    public void windowIconified(WindowEvent we) {}
    public void windowDeiconified(WindowEvent we) {}
    public void windowActivated(WindowEvent we) {}
    public void windowDeactivated(WindowEvent we) {}
}

                String name = rs.getString("name");
                int quantity = rs.getInt("quantity");
                alertMessage.append("Low Stock: ").append(name).append(" (").append(quantity).append(")\n");
            }

            alertLabel.setText(alertMessage.length() > 0 ? alertMessage.toString() : "All items are above threshold.");
        } catch (SQLException ex) {
            ex.printStackTrace();
            alertLabel.setText("Error checking inventory threshold.");
        }
    }

    // Event handling for search and refresh buttons
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == searchButton) {
            String productName = searchField.getText();
            if (!productName.isEmpty()) {
                String result = searchProduct(productName);
                JOptionPane.showMessageDialog(this, result);
            } else {
                JOptionPane.showMessageDialog(this, "Please enter a product name to search.");
            }
        } else if (e.getSource() == refreshButton) {
            loadInventory();
            checkThresholdAlert();
        }
    }

    // Method to search for a product in the database by name
    private String searchProduct(String productName) {
        String url = "jdbc:mysql://localhost:3306/ecommerce_db";
        String dbUsername = "root";
        String dbPassword = "";
        String query = "SELECT * FROM products WHERE name = ?";

        try (Connection conn = DriverManager.getConnection(url, dbUsername, dbPassword);
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, productName);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String name = rs.getString("name");
                double price = rs.getDouble("price");
                int quantity = rs.getInt("quantity");
                return "Product Found: Name = " + name + ", Price = " + price + ", Quantity = " + quantity;
            } else {
                return "Product not found: " + productName;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            return "Error occurred while searching for the product.";
        }
    }

    // Window listener methods
    public void windowClosing(WindowEvent we) {
        dispose();
    }

    public void windowOpened(WindowEvent we) {}
    public void windowClosed(WindowEvent we) {}
    public void windowIconified(WindowEvent we) {}
    public void windowDeiconified(WindowEvent we) {}
    public void windowActivated(WindowEvent we) {}
    public void windowDeactivated(WindowEvent we) {}
}
