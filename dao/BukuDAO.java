import java.util.List;
//import model.Buku;

public interface BukuDAO {
    void simpan(Buku buku);
    void update(Buku buku);
    void hapus(int idBuku);
    Buku getBuku(int idBuku);
    List<Buku> getAllBuku();
}