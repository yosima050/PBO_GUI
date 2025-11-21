package config;

import java.sql.Connection;

public class KoneksiDB {
    public static Connection getConnection() {
        return koneksi.getConnection();
    }
}