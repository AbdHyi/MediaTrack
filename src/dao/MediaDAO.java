package dao;

import config.DBConnection;
import model.Media;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MediaDAO {

    public List<Media> getAllMedia() {
        List<Media> list = new ArrayList<>();
        String sql = "SELECT * FROM media ORDER BY id_media";
        try (Statement st = DBConnection.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("[MediaDAO.getAllMedia] " + e.getMessage());
        }
        return list;
    }

    public boolean insertMedia(Media m) {
        String sql = "INSERT INTO media "
                   + "(judul, kategori, tahun_rilis, sutradara_studio, deskripsi, image_url) "
                   + "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps =
                DBConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, m.getJudul());
            ps.setString(2, m.getKategori());
            ps.setInt(3,    m.getTahunRilis());
            ps.setString(4, m.getSutradara());
            ps.setString(5, m.getDeskripsi());
            ps.setString(6, m.getImageUrl());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[MediaDAO.insertMedia] " + e.getMessage());
            return false;
        }
    }

    public boolean updateMedia(Media m) {
        String sql = "UPDATE media "
                   + "SET judul=?, kategori=?, tahun_rilis=?, "
                   + "sutradara_studio=?, deskripsi=?, image_url=? "
                   + "WHERE id_media=?";
        try (PreparedStatement ps =
                DBConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, m.getJudul());
            ps.setString(2, m.getKategori());
            ps.setInt(3,    m.getTahunRilis());
            ps.setString(4, m.getSutradara());
            ps.setString(5, m.getDeskripsi());
            ps.setString(6, m.getImageUrl());
            ps.setInt(7,    m.getIdMedia());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[MediaDAO.updateMedia] " + e.getMessage());
            return false;
        }
    }

    public boolean deleteMedia(int idMedia) {
        String sql = "DELETE FROM media WHERE id_media = ?";
        try (PreparedStatement ps =
                DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, idMedia);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[MediaDAO.deleteMedia] " + e.getMessage());
            return false;
        }
    }

    public List<Media> searchMedia(String keyword, String kategori, String tahun) {
        List<Media> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM media WHERE 1=1");
        if (keyword  != null && !keyword.isEmpty())  sql.append(" AND judul LIKE ?");
        if (kategori != null && !kategori.isEmpty()) sql.append(" AND kategori = ?");
        if (tahun    != null && !tahun.isEmpty())    sql.append(" AND tahun_rilis = ?");
        sql.append(" ORDER BY id_media");
        try (PreparedStatement ps =
                DBConnection.getConnection().prepareStatement(sql.toString())) {
            int i = 1;
            if (keyword  != null && !keyword.isEmpty())  ps.setString(i++, "%" + keyword + "%");
            if (kategori != null && !kategori.isEmpty()) ps.setString(i++, kategori);
            if (tahun    != null && !tahun.isEmpty())    ps.setInt(i++, Integer.parseInt(tahun));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("[MediaDAO.searchMedia] " + e.getMessage());
        }
        return list;
    }

    private Media mapRow(ResultSet rs) throws SQLException {
        return new Media(
            rs.getInt("id_media"),
            rs.getString("judul"),
            rs.getString("kategori"),
            rs.getInt("tahun_rilis"),
            rs.getString("sutradara_studio"),
            rs.getString("deskripsi"),
            rs.getString("image_url")
        );
    }
}