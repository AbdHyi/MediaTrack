package model;

public class MediaRequest {

    private int    idRequest;
    private int    idUser;
    private String tipe;
    private String judul;
    private String kategori;
    private String isiPengajuan;
    private int    idMediaRef;
    private String status;
    private String createdAt;
    private String namaUser;

    public MediaRequest() {}

    public int    getIdRequest()    { return idRequest; }
    public int    getIdUser()       { return idUser; }
    public String getTipe()         { return tipe; }
    public String getJudul()        { return judul; }
    public String getKategori()     { return kategori; }
    public String getIsiPengajuan() { return isiPengajuan; }
    public int    getIdMediaRef()   { return idMediaRef; }
    public String getStatus()       { return status; }
    public String getCreatedAt()    { return createdAt; }
    public String getNamaUser()     { return namaUser; }

    public void setIdRequest(int v)       { idRequest    = v; }
    public void setIdUser(int v)          { idUser       = v; }
    public void setTipe(String v)         { tipe         = v; }
    public void setJudul(String v)        { judul        = v; }
    public void setKategori(String v)     { kategori     = v; }
    public void setIsiPengajuan(String v) { isiPengajuan = v; }
    public void setIdMediaRef(int v)      { idMediaRef   = v; }
    public void setStatus(String v)       { status       = v; }
    public void setCreatedAt(String v)    { createdAt    = v; }
    public void setNamaUser(String v)     { namaUser     = v; }
}