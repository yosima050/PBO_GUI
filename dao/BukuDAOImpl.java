import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import config.KoneksiDB;
// import model.Buku; 

public class BukuDAOImpl implements BukuDAO {

    @Override
    public void simpan(Buku buku) {
        String sql = "INSERT INTO buku (jenis_buku, judul, penulis, stok) VALUES (?, ?, ?, ?)";
        try (Connection conn = KoneksiDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, buku.getJenisBuku());
            ps.setString(2, buku.getJudul());
            ps.setString(3, buku.getPenulis());
            ps.setInt(4, buku.getStok());
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
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
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @Override
    public void hapus(int idBuku) {
        String sql = "DELETE FROM buku WHERE id_buku=?";
        try (Connection conn = KoneksiDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idBuku);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @Override
    public Buku getBuku(int idBuku) { return null; }

    @Override
    public List<Buku> getAllBuku() {
        List<Buku> listBuku = new ArrayList<>();
        String sql = "SELECT * FROM buku";
        try (Connection conn = KoneksiDB.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                listBuku.add(new Buku(
                    rs.getInt("id_buku"),
                    rs.getString("jenis_buku"), 
                    rs.getString("judul"),
                    rs.getString("penulis"), 
                    rs.getInt("stok")
                ));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return listBuku;
    }
}