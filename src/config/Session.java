package config;

import model.User;

public class Session {

    private static User currentUser = null;

    // Simpan user setelah login berhasil
    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    // Ambil user yang sedang login
    public static User getCurrentUser() {
        return currentUser;
    }

    // Hapus session saat logout
    public static void logout() {
        currentUser = null;
    }

    public static boolean isLoggedIn() {
        return currentUser != null;
    }

    // Cek apakah user yang login adalah admin
    public static boolean isAdmin() {
        return currentUser != null
            && "admin".equalsIgnoreCase(currentUser.getRole());
    }
}