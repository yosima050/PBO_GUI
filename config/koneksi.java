package config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import javax.swing.JOptionPane;

public class koneksi {
    private static Connection conn;

    public static Connection getConnection() {
        try {
            if (conn == null || conn.isClosed()) {
                try {
                    String DATABASE_NAME = "perpustakaan";
                    String url = "jdbc:mysql://localhost:3306/" + DATABASE_NAME + "?useSSL=false&serverTimezone=UTC";
                    String user = "root";
                    String pass = "";

                    Class.forName("com.mysql.cj.jdbc.Driver");
                    
                    conn = DriverManager.getConnection(url, user, pass);
                    
                } catch (ClassNotFoundException e) {
                    JOptionPane.showMessageDialog(null, "Driver MySQL tidak ditemukan!\n" + e.getMessage());
                } catch (SQLException e) {
                    JOptionPane.showMessageDialog(null, "Gagal koneksi ke database!\n" + e.getMessage());
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return conn;
    }

    public static void main(String[] args) {
        if (getConnection() != null) {
            System.out.println("Koneksi berhasil.");
        }
    }
}