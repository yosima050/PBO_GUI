package model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import config.koneksi;

public class kembalikanBuku {
    public void prosesKembalikan(int idPeminjaman, int idBuku, int denda) {
        Connection conn = koneksi.getConnection();
        try {
            conn.setAutoCommit(false);

            // 1. Update Status di Tabel Peminjaman
            String sqlUpdateStatus = "UPDATE peminjaman SET status = 'Selesai' WHERE id_peminjaman = ?";
            PreparedStatement ps1 = conn.prepareStatement(sqlUpdateStatus);
            ps1.setInt(1, idPeminjaman);
            ps1.executeUpdate();

            // 2. Catat ke Tabel Pengembalian
            String sqlKembali = "INSERT INTO pengembalian (id_peminjaman, tanggal_kembali, denda) VALUES (?, NOW(), ?)";
            PreparedStatement ps2 = conn.prepareStatement(sqlKembali);
            ps2.setInt(1, idPeminjaman);
            ps2.setInt(2, denda); // Denda bisa 0 kalau tepat waktu
            ps2.executeUpdate();

            // 3. Kembalikan Stok Buku
            String sqlTambahStok = "UPDATE buku SET stok = stok + 1 WHERE id_buku = ?";
            PreparedStatement ps3 = conn.prepareStatement(sqlTambahStok);
            ps3.setInt(1, idBuku);
            ps3.executeUpdate();

            conn.commit();
            System.out.println("Pengembalian Berhasil!");

        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }
}