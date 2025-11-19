package config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import javax.swing.JOptionPane;

public class koneksi {
    private static Connection conn;

    public static Connection getConnection() {
        if (conn == null) {
            try {
                String url = "";
                String user = "root";
                String pass = "";
                
                // Load the MySQL JDBC driver
                Class.forName("com.mysql.cj.jdbc.Driver");
                
                // Attempt to establish a connection
                conn = DriverManager.getConnection(url, user, pass);
                
                // Optional: Notify user of successful connection
                // JOptionPane.showMessageDialog(null, "Koneksi ke Database Berhasil!");
                
            } catch (ClassNotFoundException e) {
                JOptionPane.showMessageDialog(null, "Driver MySQL tidak ditemukan! Pastikan library (JAR) sudah ditambahkan.\n" + e.getMessage(), "Connection Error", JOptionPane.ERROR_MESSAGE);
                System.exit(0);
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(null, "Koneksi ke database gagal! Periksa URL, username, dan password.\n" + e.getMessage(), "Connection Error", JOptionPane.ERROR_MESSAGE);
                System.exit(0);
            }
        }
        return conn;
    }

    public static void main(String[] args) {
        // Test the connection
        if (getConnection() != null) {
            System.out.println("Koneksi berhasil diuji.");
        } else {
            System.out.println("Koneksi gagal diuji.");
        }
    }
}
