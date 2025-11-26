package dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import config.KoneksiDB;
import model.Buku;

public class BukuDAOImpl implements BukuDAO {

    @Override
    public void simpan(Buku buku) {
        // pastikan SQL TIDAK memasukkan id_buku
        String sql = "INSERT INTO buku (jenis_buku, judul, penulis, stok) VALUES (?, ?, ?, ?)";
        try (Connection conn = KoneksiDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, buku.getJenisBuku());
            ps.setString(2, buku.getJudul());
            ps.setString(3, buku.getPenulis());
            ps.setInt(4, buku.getStok());
            ps.executeUpdate();

            // ambil id yang di-generate DB (opsional: set ke object jika ada setter)
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int newId = rs.getInt(1);
                    // jika model punya setter setIdBuku(int), panggil di sini:
                    // buku.setIdBuku(newId);
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Gagal menyimpan buku: " + ex.getMessage(), ex);
        }
    }

    @Override
    public void update(Buku buku) {
        String sql = "UPDATE buku SET jenis_buku=?, judul=?, penulis=?, stok=? WHERE id_buku=?";
        try (Connection conn = KoneksiDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, buku.getJenisBuku());
            ps.setString(2, buku.getJudul());
            ps.setString(3, buku.getPenulis());
            ps.setInt(4, buku.getStok());
            ps.setInt(5, buku.getIdBuku());
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new RuntimeException("Gagal mengupdate buku: " + ex.getMessage(), ex);
        }
    }

    @Override
    public void hapus(int idBuku) {
        String sql = "DELETE FROM buku WHERE id_buku = ?";
        try (Connection conn = KoneksiDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idBuku);
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new RuntimeException("Gagal menghapus buku: " + ex.getMessage(), ex);
        }
    }

    @Override
    public Buku getBuku(int idBuku) {
        String sql = "SELECT id_buku, jenis_buku, judul, penulis, stok FROM buku WHERE id_buku = ?";
        try (Connection conn = KoneksiDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idBuku);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Buku(
                        rs.getInt("id_buku"),
                        rs.getString("jenis_buku"),
                        rs.getString("judul"),
                        rs.getString("penulis"),
                        rs.getInt("stok")
                    );
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Gagal mengambil buku: " + ex.getMessage(), ex);
        }
        return null;
    }

    @Override
    public List<Buku> getAllBuku() {
        List<Buku> list = new ArrayList<>();
        String sql = "SELECT id_buku, jenis_buku, judul, penulis, stok FROM buku ORDER BY id_buku DESC";
        try (Connection conn = KoneksiDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new Buku(
                    rs.getInt("id_buku"),
                    rs.getString("jenis_buku"),
                    rs.getString("judul"),
                    rs.getString("penulis"),
                    rs.getInt("stok")
                ));
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Gagal mengambil daftar buku: " + ex.getMessage(), ex);
        }
        return list;
    }
}