package model;

public class Media {

    private int    idMedia;
    private String judul;
    private String kategori;
    private int    tahunRilis;
    private String sutradara;
    private String deskripsi;
    private String imageUrl;

    public Media() {}

    public Media(int idMedia, String judul, String kategori,
                 int tahunRilis, String sutradara, String deskripsi, String imageUrl) {
        this.idMedia    = idMedia;
        this.judul      = judul;
        this.kategori   = kategori;
        this.tahunRilis = tahunRilis;
        this.sutradara  = sutradara;
        this.deskripsi  = deskripsi;
        this.imageUrl   = imageUrl;
    }

    public int    getIdMedia()    { return idMedia; }
    public String getJudul()      { return judul; }
    public String getKategori()   { return kategori; }
    public int    getTahunRilis() { return tahunRilis; }
    public String getSutradara()  { return sutradara; }
    public String getDeskripsi()  { return deskripsi; }
    public String getImageUrl()   { return imageUrl; }

    public void setIdMedia(int v)      { idMedia    = v; }
    public void setJudul(String v)     { judul      = v; }
    public void setKategori(String v)  { kategori   = v; }
    public void setTahunRilis(int v)   { tahunRilis = v; }
    public void setSutradara(String v) { sutradara  = v; }
    public void setDeskripsi(String v) { deskripsi  = v; }
    public void setImageUrl(String v)  { imageUrl   = v; }

    @Override
    public String toString() { return judul + " (" + tahunRilis + ")"; }
}