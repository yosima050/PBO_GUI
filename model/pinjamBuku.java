package model;



import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import config.koneksi;

public class pinjamBuku {
    public void prosesPinjam(int idAnggota, int idBuku) {
        Connection conn = koneksi.getConnection();
        try {
            conn.setAutoCommit(false);
            // 2. Kurangi Stok Buku
            String sqlKurangStok = "UPDATE buku SET stok = stok - 1 WHERE id_buku = ?";
            PreparedStatement ps1 = conn.prepareStatement(sqlKurangStok);
            ps1.setInt(1, idBuku);
            ps1.executeUpdate();

            // 3. Catat Peminjaman
            String sqlPinjam = "INSERT INTO peminjaman (id_anggota, id_buku, tanggal_pinjam, status) VALUES (?, ?, NOW(), 'Dipinjam')";
            PreparedStatement ps2 = conn.prepareStatement(sqlPinjam);
            ps2.setInt(1, idAnggota);
            ps2.setInt(2, idBuku);
            ps2.executeUpdate();

            // Jika semua lancar, simpan permanen
            conn.commit();
            System.out.println("Peminjaman Berhasil!");

        } catch (SQLException e) {
            try {
                conn.rollback(); // Batalkan semua jika ada error
                System.out.println("Peminjaman Gagal, stok dikembalikan.");
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }
}