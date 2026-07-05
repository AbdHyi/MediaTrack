package dao;

import config.DBConnection;
import model.Notification;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NotificationDAO {

    public boolean insertNotif(Notification notif) {
        String sql = "INSERT INTO notifications "
                   + "(id_user_to, id_user_from, judul_notif, isi, tipe) "
                   + "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps =
                DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1,    notif.getIdUserTo());
            ps.setInt(2,    notif.getIdUserFrom());
            ps.setString(3, notif.getJudulNotif());
            ps.setString(4, notif.getIsi());
            ps.setString(5, notif.getTipe());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[NotificationDAO.insert] " + e.getMessage());
            return false;
        }
    }

    public List<Notification> getNotifsByUser(int idUserTo) {
        List<Notification> list = new ArrayList<>();
        String sql = "SELECT n.*, u.nama_lengkap AS nama_from "
                   + "FROM notifications n "
                   + "JOIN users u ON n.id_user_from = u.id_user "
                   + "WHERE n.id_user_to = ? "
                   + "ORDER BY n.is_read ASC, n.created_at DESC";
        try (PreparedStatement ps =
                DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, idUserTo);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("[NotificationDAO.getByUser] " + e.getMessage());
        }
        return list;
    }

    public int countUnread(int idUserTo) {
        String sql = "SELECT COUNT(*) FROM notifications "
                   + "WHERE id_user_to = ? AND is_read = 0";
        try (PreparedStatement ps =
                DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, idUserTo);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("[NotificationDAO.countUnread] " + e.getMessage());
        }
        return 0;
    }

    public void markAllRead(int idUserTo) {
        String sql = "UPDATE notifications SET is_read = 1 WHERE id_user_to = ?";
        try (PreparedStatement ps =
                DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, idUserTo);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[NotificationDAO.markAllRead] " + e.getMessage());
        }
    }

    private Notification mapRow(ResultSet rs) throws SQLException {
        Notification n = new Notification();
        n.setIdNotif(rs.getInt("id_notif"));
        n.setIdUserTo(rs.getInt("id_user_to"));
        n.setIdUserFrom(rs.getInt("id_user_from"));
        n.setJudulNotif(rs.getString("judul_notif"));
        n.setIsi(rs.getString("isi"));
        n.setTipe(rs.getString("tipe"));
        n.setRead(rs.getBoolean("is_read"));
        n.setCreatedAt(rs.getString("created_at"));
        n.setNamaFrom(rs.getString("nama_from"));
        return n;
    }
}