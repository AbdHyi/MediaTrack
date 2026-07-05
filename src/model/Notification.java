package model;

public class Notification {

    private int     idNotif;
    private int     idUserTo;
    private int     idUserFrom;
    private String  judulNotif;
    private String  isi;
    private String  tipe;
    private boolean isRead;
    private String  createdAt;
    private String  namaFrom;

    public Notification() {}

    public int     getIdNotif()    { return idNotif; }
    public int     getIdUserTo()   { return idUserTo; }
    public int     getIdUserFrom() { return idUserFrom; }
    public String  getJudulNotif() { return judulNotif; }
    public String  getIsi()        { return isi; }
    public String  getTipe()       { return tipe; }
    public boolean isRead()        { return isRead; }
    public String  getCreatedAt()  { return createdAt; }
    public String  getNamaFrom()   { return namaFrom; }

    public void setIdNotif(int v)       { idNotif    = v; }
    public void setIdUserTo(int v)      { idUserTo   = v; }
    public void setIdUserFrom(int v)    { idUserFrom = v; }
    public void setJudulNotif(String v) { judulNotif = v; }
    public void setIsi(String v)        { isi        = v; }
    public void setTipe(String v)       { tipe       = v; }
    public void setRead(boolean v)      { isRead     = v; }
    public void setCreatedAt(String v)  { createdAt  = v; }
    public void setNamaFrom(String v)   { namaFrom   = v; }
}