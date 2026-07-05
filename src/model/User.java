package model;

public class User {

    private int    idUser;
    private String username;
    private String password;
    private String namaLengkap;
    private String role;

    // ── Konstruktor kosong (diperlukan DAO) ─────────────
    public User() {}

    // ── Konstruktor lengkap ──────────────────────────────
    public User(int idUser, String username, String password,
                String namaLengkap, String role) {
        this.idUser      = idUser;
        this.username    = username;
        this.password    = password;
        this.namaLengkap = namaLengkap;
        this.role        = role;
    }

    // ── Getters ──────────────────────────────────────────
    public int    getIdUser()      { return idUser; }
    public String getUsername()    { return username; }
    public String getPassword()    { return password; }
    public String getNamaLengkap() { return namaLengkap; }
    public String getRole()        { return role; }

    // ── Setters ──────────────────────────────────────────
    public void setIdUser(int idUser)           { this.idUser      = idUser; }
    public void setUsername(String username)    { this.username    = username; }
    public void setPassword(String password)    { this.password    = password; }
    public void setNamaLengkap(String nama)     { this.namaLengkap = nama; }
    public void setRole(String role)            { this.role        = role; }

    @Override
    public String toString() {
        return namaLengkap + " [" + role + "]";
    }
}