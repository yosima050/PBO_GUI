

public class Anggota {
    private int idAnggota;
    private String nama;
    private String alamat;
    private String noTelp;
    
    public Anggota(int idAnggota, String nama, String alamat, String noTelp) {
        this.idAnggota = idAnggota;
        this.nama = nama;
        this.alamat = alamat;
        this.noTelp = noTelp;
    }

    public int getIdAnggota() { return idAnggota; }
    public String getNama() { return nama; }
    public String getAlamat() { return alamat; }
    public String getNoTelp() { return noTelp; }

    @Override
    public String toString() {
        return nama;
    }
}