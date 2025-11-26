package model;

public abstract class ItemPerpustakaan {
    
    protected String judul; 

    public ItemPerpustakaan(String judul) {
        this.judul = judul;
    }

    public String getJudul() {
        return judul;
    }

    public abstract String getDisplayInfo();
}