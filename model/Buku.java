package model;

public class Buku extends ItemPerpustakaan {
    
    private int idBuku;
    private String jenisBuku;
    private String penulis;
    private int stok;

    public Buku(int idBuku, String jenisBuku, String judul, String penulis, int stok) {
        super(judul);
        this.idBuku = idBuku;
        this.jenisBuku = jenisBuku;
        this.penulis = penulis;
        this.stok = stok;
    }

    public int getIdBuku() { return idBuku; }
    public String getJenisBuku() { return jenisBuku; }
    public String getPenulis() { return penulis; }
    public int getStok() { return stok; }
    public void setStok(int stok) { this.stok = stok; }

    @Override
    public String getDisplayInfo() {
        return "Buku: " + judul + " (" + jenisBuku + ")";
    }

    @Override
    public String toString() {
        return super.getJudul(); 
    }
}