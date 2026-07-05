package model;

public class ReviewRating {

    private int    idReview;
    private int    idUser;
    private int    idMedia;
    private int    rating;
    private String review;
    private String tanggalReview;

    // Field display dari JOIN — tidak disimpan ke DB
    private String username;
    private String judulMedia;
    private String kategoriMedia;
    private String imageUrlMedia;

    public ReviewRating() {}

    public int    getIdReview()      { return idReview; }
    public int    getIdUser()        { return idUser; }
    public int    getIdMedia()       { return idMedia; }
    public int    getRating()        { return rating; }
    public String getReview()        { return review; }
    public String getTanggalReview() { return tanggalReview; }
    public String getUsername()      { return username; }
    public String getJudulMedia()    { return judulMedia; }
    public String getKategoriMedia() { return kategoriMedia; }
    public String getImageUrlMedia() { return imageUrlMedia; }

    public void setIdReview(int v)        { idReview      = v; }
    public void setIdUser(int v)          { idUser        = v; }
    public void setIdMedia(int v)         { idMedia       = v; }
    public void setRating(int v)          { rating        = v; }
    public void setReview(String v)       { review        = v; }
    public void setTanggalReview(String v){ tanggalReview = v; }
    public void setUsername(String v)     { username      = v; }
    public void setJudulMedia(String v)   { judulMedia    = v; }
    public void setKategoriMedia(String v){ kategoriMedia = v; }
    public void setImageUrlMedia(String v){ imageUrlMedia = v; }
}