package dao;

import config.DBConnection;
import model.User;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    // Login
    public User login(String username, String password) {
        if (DBConnection.getConnection() == null) {
            javax.swing.JOptionPane.showMessageDialog(null,
                "Koneksi database gagal. Cek MySQL.", "Error",
                javax.swing.JOptionPane.ERROR_MESSAGE);
            return null;
        }
        String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
        try (PreparedStatement ps =
                DBConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) {
            System.err.println("[UserDAO.login] " + e.getMessage());
        }
        return null;
    }

    // Get All
    public List<User> getAllUsers() {
        List<User> list = new ArrayList<>();
        String sql = "SELECT * FROM users ORDER BY id_user";
        try (Statement st  = DBConnection.getConnection().createStatement();
             ResultSet rs  = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("[UserDAO.getAllUsers] " + e.getMessage());
        }
        return list;
    }

    // Insert
    public boolean insertUser(User user) {
        String sql = "INSERT INTO users (username, password, nama_lengkap, role) "
                   + "VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps =
                DBConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPassword());
            ps.setString(3, user.getNamaLengkap());
            ps.setString(4, user.getRole());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[UserDAO.insertUser] " + e.getMessage());
            return false;
        }
    }

    // Update
    // Jika user.getPassword() == null → password tidak diubah
    public boolean updateUser(User user) {
        String sql;
        if (user.getPassword() == null) {
            sql = "UPDATE users SET username=?, nama_lengkap=?, role=? "
                + "WHERE id_user=?";
            try (PreparedStatement ps =
                    DBConnection.getConnection().prepareStatement(sql)) {
                ps.setString(1, user.getUsername());
                ps.setString(2, user.getNamaLengkap());
                ps.setString(3, user.getRole());
                ps.setInt(4, user.getIdUser());
                return ps.executeUpdate() > 0;
            } catch (SQLException e) {
                System.err.println("[UserDAO.updateUser] " + e.getMessage());
                return false;
            }
        } else {
            sql = "UPDATE users SET username=?, password=?, nama_lengkap=?, role=? "
                + "WHERE id_user=?";
            try (PreparedStatement ps =
                    DBConnection.getConnection().prepareStatement(sql)) {
                ps.setString(1, user.getUsername());
                ps.setString(2, user.getPassword());
                ps.setString(3, user.getNamaLengkap());
                ps.setString(4, user.getRole());
                ps.setInt(5, user.getIdUser());
                return ps.executeUpdate() > 0;
            } catch (SQLException e) {
                System.err.println("[UserDAO.updateUser] " + e.getMessage());
                return false;
            }
        }
    }

    // Delete
    public boolean deleteUser(int idUser) {
        String sql = "DELETE FROM users WHERE id_user = ?";
        try (PreparedStatement ps =
                DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, idUser);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[UserDAO.deleteUser] " + e.getMessage());
            return false;
        }
    }

    // Cek Username Unik
    // excludeId = 0 saat Tambah, = id_user saat Ubah
    public boolean isUsernameExists(String username, int excludeId) {
        String sql = "SELECT id_user FROM users WHERE username = ? AND id_user != ?";
        try (PreparedStatement ps =
                DBConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setInt(2, excludeId);
            return ps.executeQuery().next();
        } catch (SQLException e) {
            System.err.println("[UserDAO.isUsernameExists] " + e.getMessage());
            return false;
        }
    }

    // Helper
    private User mapRow(ResultSet rs) throws SQLException {
        return new User(
            rs.getInt("id_user"),
            rs.getString("username"),
            rs.getString("password"),
            rs.getString("nama_lengkap"),
            rs.getString("role")
        );
    }

    // ── Get Profile Picture ───────────────────────────────────────────────────
    public byte[] getProfilePic(int idUser) {
        String sql = "SELECT profile_pic FROM users WHERE id_user = ?";
        try (PreparedStatement ps =
                DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, idUser);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getBytes("profile_pic");
        } catch (SQLException e) {
            System.err.println("[UserDAO.getProfilePic] " + e.getMessage());
        }
        return null;
    }

    // ── Update Profile Picture (null = hapus / reset default) ─────────────────
    public boolean updateProfilePic(int idUser, byte[] data) {
        String sql = "UPDATE users SET profile_pic = ? WHERE id_user = ?";
        try (PreparedStatement ps =
                DBConnection.getConnection().prepareStatement(sql)) {
            if (data != null) ps.setBytes(1, data);
            else              ps.setNull(1, java.sql.Types.BLOB);
            ps.setInt(2, idUser);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[UserDAO.updateProfilePic] " + e.getMessage());
            return false;
        }
    }

}