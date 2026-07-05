package dao;

import config.DBConnection;
import model.ReviewRating;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReviewDAO {

    // Get All (semua review, semua user)
    public List<ReviewRating> getAllReviews() {
        return searchReviews("", "", "");
    }

    // Search & Filter
    // ratingFilter: "" = semua, atau "1".."10"
    public List<ReviewRating> searchReviews(String judulKw, String kategori, String ratingFilter) {
        List<ReviewRating> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT rr.id_review, rr.id_user, rr.id_media, rr.rating, " +
            "       rr.review, rr.tanggal_review, u.username, " +
            "       m.judul, m.kategori, m.image_url " +
            "FROM   review_rating rr " +
            "JOIN   users u ON rr.id_user  = u.id_user " +
            "JOIN   media m ON rr.id_media = m.id_media " +
            "WHERE  1=1");

        if (judulKw  != null && !judulKw.isEmpty())  sql.append(" AND m.judul LIKE ?");
        if (kategori != null && !kategori.isEmpty()) sql.append(" AND m.kategori = ?");
        if (ratingFilter != null && !ratingFilter.isEmpty()) sql.append(" AND rr.rating = ?");
        sql.append(" ORDER BY rr.tanggal_review DESC, rr.id_review DESC");

        try (PreparedStatement ps =
                DBConnection.getConnection().prepareStatement(sql.toString())) {
            int i = 1;
            if (judulKw  != null && !judulKw.isEmpty())  ps.setString(i++, "%" + judulKw + "%");
            if (kategori != null && !kategori.isEmpty()) ps.setString(i++, kategori);
            if (ratingFilter != null && !ratingFilter.isEmpty())
                ps.setInt(i++, Integer.parseInt(ratingFilter));

            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("[ReviewDAO.searchReviews] " + e.getMessage());
        }
        return list;
    }

    // Insert
    public boolean insertReview(ReviewRating rr) {
        String sql =
            "INSERT INTO review_rating (id_user, id_media, rating, review) " +
            "VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps =
                DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1,    rr.getIdUser());
            ps.setInt(2,    rr.getIdMedia());
            ps.setInt(3,    rr.getRating());
            ps.setString(4, rr.getReview());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[ReviewDAO.insertReview] " + e.getMessage());
            return false;
        }
    }

    // Update
    public boolean updateReview(ReviewRating rr) {
        String sql =
            "UPDATE review_rating " +
            "SET id_media=?, rating=?, review=? " +
            "WHERE id_review=?";
        try (PreparedStatement ps =
                DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1,    rr.getIdMedia());
            ps.setInt(2,    rr.getRating());
            ps.setString(3, rr.getReview());
            ps.setInt(4,    rr.getIdReview());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[ReviewDAO.updateReview] " + e.getMessage());
            return false;
        }
    }

    // Delete
    public boolean deleteReview(int idReview) {
        String sql = "DELETE FROM review_rating WHERE id_review = ?";
        try (PreparedStatement ps =
                DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, idReview);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[ReviewDAO.deleteReview] " + e.getMessage());
            return false;
        }
    }

    // Helper
    private ReviewRating mapRow(ResultSet rs) throws SQLException {
        ReviewRating rr = new ReviewRating();
        rr.setIdReview(rs.getInt("id_review"));
        rr.setIdUser(rs.getInt("id_user"));
        rr.setIdMedia(rs.getInt("id_media"));
        rr.setRating(rs.getInt("rating"));
        rr.setReview(rs.getString("review"));
        rr.setTanggalReview(rs.getString("tanggal_review"));
        rr.setUsername(rs.getString("username"));
        rr.setJudulMedia(rs.getString("judul"));
        rr.setKategoriMedia(rs.getString("kategori"));
        rr.setImageUrlMedia(rs.getString("image_url"));
        return rr;
    }
}