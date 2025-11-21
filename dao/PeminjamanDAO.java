import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import config.KoneksiDB;
// import model.*; 

public class PeminjamanDAO {

    // 1. Ambil Data Anggota
    public List<Anggota> getAllAnggota() {
        List<Anggota> list = new ArrayList<>();
        String sql = "SELECT * FROM anggota";
        try (Connection conn = KoneksiDB.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                list.add(new Anggota(
                    rs.getInt("id_anggota"),
                    rs.getString("nama"),
                    rs.getString("alamat"),
                    rs.getString("no_telp")
                ));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // 2. Proses Transaksi Peminjaman
    public boolean prosesPeminjaman(Anggota anggota, Buku buku) {
        Connection conn = KoneksiDB.getConnection();
        try {
            conn.setAutoCommit(false);

            // A. Cek Stok
            if (buku.getStok() <= 0) throw new SQLException("Stok habis!");

            // B. Kurangi Stok Buku
            String sqlKurang = "UPDATE buku SET stok = stok - 1 WHERE id_buku = ?";
            try (PreparedStatement ps = conn.prepareStatement(sqlKurang)) {
                ps.setInt(1, buku.getIdBuku());
                ps.executeUpdate();
            }

            // C. Insert Peminjaman (Status 'Dipinjam' - Huruf Besar)
            String sqlPinjam = "INSERT INTO peminjaman (id_anggota, id_buku, tanggal_pinjam, tanggal_jatuh_tempo, status_peminjaman) VALUES (?, ?, ?, ?, 'Dipinjam')";
            try (PreparedStatement ps = conn.prepareStatement(sqlPinjam)) {
                ps.setInt(1, anggota.getIdAnggota());
                ps.setInt(2, buku.getIdBuku());
                
                LocalDate hariIni = LocalDate.now();
                LocalDate jatuhTempo = hariIni.plusDays(7);
                
                ps.setDate(3, java.sql.Date.valueOf(hariIni));
                ps.setDate(4, java.sql.Date.valueOf(jatuhTempo));
                
                ps.executeUpdate();
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            try { conn.rollback(); } catch (SQLException ex) {}
            e.printStackTrace();
            return false;
        }
    }

    // 3. Ambil Data Peminjaman yang Aktif (Status = 'Dipinjam')
    public List<Object[]> getPeminjamanAktif() {
        List<Object[]> list = new ArrayList<>();
        // Join tabel untuk mengambil nama dan judul
        String sql = "SELECT p.id_pinjam, a.nama, b.judul, p.tanggal_pinjam, p.tanggal_jatuh_tempo, p.id_buku " +
                     "FROM peminjaman p " +
                     "JOIN anggota a ON p.id_anggota = a.id_anggota " +
                     "JOIN buku b ON p.id_buku = b.id_buku " +
                     "WHERE p.status_peminjaman = 'Dipinjam'";
                     
        try (Connection conn = KoneksiDB.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                list.add(new Object[]{
                    rs.getInt("id_pinjam"),
                    rs.getString("nama"),
                    rs.getString("judul"),
                    rs.getDate("tanggal_pinjam"),
                    rs.getDate("tanggal_jatuh_tempo"),
                    rs.getInt("id_buku")
                });
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // 4. Proses Pengembalian Buku
    public boolean kembalikanBuku(int idPinjam, int idBuku, int denda) {
        Connection conn = KoneksiDB.getConnection();
        try {
            conn.setAutoCommit(false); 

            // A. Update Status Peminjaman jadi 'Sudah Dikembalikan'
            String sqlUpdate = "UPDATE peminjaman SET status_peminjaman = 'Sudah Dikembalikan' WHERE id_pinjam = ?";
            try (PreparedStatement ps = conn.prepareStatement(sqlUpdate)) {
                ps.setInt(1, idPinjam);
                ps.executeUpdate();
            }

            // B. Insert ke Tabel Pengembalian (Masuk ke kolom tanggal_kembali)
            String sqlInsert = "INSERT INTO pengembalian (id_pinjam, tanggal_kembali, denda) VALUES (?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sqlInsert)) {
                ps.setInt(1, idPinjam);
                ps.setDate(2, java.sql.Date.valueOf(LocalDate.now()));
                ps.setInt(3, denda);
                ps.executeUpdate();
            }

            // C. Balikin Stok Buku (+1)
            String sqlStok = "UPDATE buku SET stok = stok + 1 WHERE id_buku = ?";
            try (PreparedStatement ps = conn.prepareStatement(sqlStok)) {
                ps.setInt(1, idBuku);
                ps.executeUpdate();
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            try { conn.rollback(); } catch (SQLException ex) {}
            e.printStackTrace();
            return false;
        }
    }

    // 5. Ambil Riwayat Lengkap (Gabungan 4 Tabel)
    public List<Object[]> getRiwayatLengkap() {
        List<Object[]> list = new ArrayList<>();
        // Query ini menggabungkan semua tabel sesuai deskripsi Anda
        String sql = "SELECT p.id_pinjam, a.nama, b.judul, " +
                     "p.tanggal_pinjam, p.tanggal_jatuh_tempo, " +
                     "k.tanggal_kembali, k.denda, p.status_peminjaman " +
                     "FROM peminjaman p " +
                     "JOIN anggota a ON p.id_anggota = a.id_anggota " +
                     "JOIN buku b ON p.id_buku = b.id_buku " +
                     "LEFT JOIN pengembalian k ON p.id_pinjam = k.id_pinjam " + // Pakai LEFT JOIN agar yang belum kembali tetap muncul
                     "ORDER BY p.id_pinjam DESC"; 
                     
        try (Connection conn = KoneksiDB.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                // Logika tampilan tanggal kembali
                Date tglKembali = rs.getDate("tanggal_kembali");
                String strKembali = (tglKembali != null) ? tglKembali.toString() : "-";
                
                // Logika tampilan denda
                int denda = rs.getInt("denda");
                String strDenda = (tglKembali != null) ? "Rp " + denda : "-";

                list.add(new Object[]{
                    rs.getInt("id_pinjam"),
                    rs.getString("nama"),
                    rs.getString("judul"),
                    rs.getDate("tanggal_pinjam"),
                    rs.getDate("tanggal_jatuh_tempo"),
                    strKembali, // Tanggal realisasi kembali
                    strDenda,   // Nominal denda
                    rs.getString("status_peminjaman")
                });
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }
}