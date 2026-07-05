package dao;

import config.DBConnection;
import model.MediaRequest;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MediaRequestDAO {

    public boolean insertRequest(MediaRequest req) {
        String sql = "INSERT INTO media_request "
                   + "(id_user, tipe, judul, kategori, isi_pengajuan, id_media_ref) "
                   + "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps =
                DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1,    req.getIdUser());
            ps.setString(2, req.getTipe());
            ps.setString(3, req.getJudul());
            ps.setString(4, req.getKategori());
            ps.setString(5, req.getIsiPengajuan());
            if (req.getIdMediaRef() > 0) ps.setInt(6, req.getIdMediaRef());
            else                          ps.setNull(6, Types.INTEGER);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[MediaRequestDAO.insert] " + e.getMessage());
            return false;
        }
    }

    public List<MediaRequest> getAllRequests(String filterStatus) {
        List<MediaRequest> list = new ArrayList<>();
        String where = (filterStatus == null || filterStatus.equals("Semua"))
                       ? "" : " AND mr.status = ?";
        String sql = "SELECT mr.*, u.nama_lengkap AS nama_user "
                   + "FROM media_request mr "
                   + "JOIN users u ON mr.id_user = u.id_user "
                   + "WHERE 1=1" + where
                   + " ORDER BY mr.created_at DESC";
        try (PreparedStatement ps =
                DBConnection.getConnection().prepareStatement(sql)) {
            if (!where.isEmpty()) ps.setString(1, filterStatus.toLowerCase());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("[MediaRequestDAO.getAll] " + e.getMessage());
        }
        return list;
    }

    public boolean updateStatus(int idRequest, String status) {
        String sql = "UPDATE media_request SET status = ? WHERE id_request = ?";
        try (PreparedStatement ps =
                DBConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2,    idRequest);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[MediaRequestDAO.updateStatus] " + e.getMessage());
            return false;
        }
    }

    private MediaRequest mapRow(ResultSet rs) throws SQLException {
        MediaRequest req = new MediaRequest();
        req.setIdRequest(rs.getInt("id_request"));
        req.setIdUser(rs.getInt("id_user"));
        req.setTipe(rs.getString("tipe"));
        req.setJudul(rs.getString("judul"));
        req.setKategori(rs.getString("kategori"));
        req.setIsiPengajuan(rs.getString("isi_pengajuan"));
        int ref = rs.getInt("id_media_ref");
        req.setIdMediaRef(rs.wasNull() ? 0 : ref);
        req.setStatus(rs.getString("status"));
        req.setCreatedAt(rs.getString("created_at"));
        req.setNamaUser(rs.getString("nama_user"));
        return req;
    }
}