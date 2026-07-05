package dao;

import config.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReportDAO {

    // Hitung total baris sebuah tabel
    public int countTotal(String table) {
        String sql = "SELECT COUNT(*) FROM " + table;
        try (Statement st = DBConnection.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("[ReportDAO.countTotal] " + e.getMessage());
        }
        return 0;
    }

    // Laporan 1: Rating rata-rata per media (semua media, LEFT JOIN)
    public List<Object[]> getRatingPerMedia() {
        List<Object[]> list = new ArrayList<>();
        String sql =
            "SELECT m.judul, m.kategori, m.tahun_rilis, " +
            "       COUNT(rr.id_review)       AS jml_review, " +
            "       ROUND(AVG(rr.rating), 1)  AS avg_rating " +
            "FROM   media m " +
            "LEFT JOIN review_rating rr ON m.id_media = rr.id_media " +
            "GROUP BY m.id_media, m.judul, m.kategori, m.tahun_rilis " +
            "ORDER BY avg_rating DESC, jml_review DESC";
        try (Statement st = DBConnection.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Object avg = rs.getObject("avg_rating");
                list.add(new Object[]{
                    rs.getString("judul"),
                    rs.getString("kategori"),
                    rs.getInt("tahun_rilis"),
                    rs.getInt("jml_review"),
                    avg != null ? avg.toString() : "–"
                });
            }
        } catch (SQLException e) {
            System.err.println("[ReportDAO.getRatingPerMedia] " + e.getMessage());
        }
        return list;
    }

    // Laporan 2: Top 10 media terbaik (hanya yang punya review)
    public List<Object[]> getTop10Media() {
        List<Object[]> list = new ArrayList<>();
        String sql =
            "SELECT m.judul, m.kategori, m.tahun_rilis, " +
            "       COUNT(rr.id_review)       AS jml_review, " +
            "       ROUND(AVG(rr.rating), 1)  AS avg_rating " +
            "FROM   media m " +
            "JOIN   review_rating rr ON m.id_media = rr.id_media " +
            "GROUP BY m.id_media, m.judul, m.kategori, m.tahun_rilis " +
            "ORDER BY avg_rating DESC, jml_review DESC " +
            "LIMIT 10";
        try (Statement st = DBConnection.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            int rank = 1;
            while (rs.next()) {
                list.add(new Object[]{
                    rank++,
                    rs.getString("judul"),
                    rs.getString("kategori"),
                    rs.getInt("tahun_rilis"),
                    rs.getInt("jml_review"),
                    rs.getDouble("avg_rating")
                });
            }
        } catch (SQLException e) {
            System.err.println("[ReportDAO.getTop10Media] " + e.getMessage());
        }
        return list;
    }

    // Laporan 3: Rekap jumlah media & review per kategori
    public List<Object[]> getRekapKategori() {
        List<Object[]> list = new ArrayList<>();
        String sql =
            "SELECT m.kategori, " +
            "       COUNT(DISTINCT m.id_media) AS jml_media, " +
            "       COUNT(rr.id_review)        AS total_review, " +
            "       ROUND(AVG(rr.rating), 1)   AS avg_rating " +
            "FROM   media m " +
            "LEFT JOIN review_rating rr ON m.id_media = rr.id_media " +
            "GROUP BY m.kategori " +
            "ORDER BY m.kategori";
        try (Statement st = DBConnection.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Object avg = rs.getObject("avg_rating");
                list.add(new Object[]{
                    rs.getString("kategori"),
                    rs.getInt("jml_media"),
                    rs.getInt("total_review"),
                    avg != null ? avg.toString() : "–"
                });
            }
        } catch (SQLException e) {
            System.err.println("[ReportDAO.getRekapKategori] " + e.getMessage());
        }
        return list;
    }
}