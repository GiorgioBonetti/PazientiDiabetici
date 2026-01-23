package it.univr.diabete.dao.impl;

import it.univr.diabete.dao.GlicemiaDAO;
import it.univr.diabete.database.Database;
import it.univr.diabete.model.Glicemia;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GlicemiaDAOImpl implements GlicemiaDAO {

    @Override
    public List<Glicemia> findByPazienteId(String codiceFiscale) throws Exception {

        String sql = "SELECT * FROM Glicemia WHERE fkPaziente = ? ORDER BY dateStamp ";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, codiceFiscale);
            ResultSet rs = ps.executeQuery();

            List<Glicemia> list = new ArrayList<>();

            while (rs.next()) {
                Glicemia g = new Glicemia();
                g.setId(rs.getInt("id"));
                g.setFkPaziente(rs.getString("fkPaziente"));
                g.setValore(rs.getInt("valore"));
                g.setParteGiorno(rs.getString("parteGiorno"));
                Timestamp ts = rs.getTimestamp("dateStamp");
                g.setDateStamp(ts.toLocalDateTime());

                list.add(g);
            }

            return list;
        }
    }
    @Override
    public List<Glicemia> findAll() throws Exception {
        String sql = "SELECT * FROM Glicemia";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            List<Glicemia> result = new ArrayList<>();

            while (rs.next()) {
                Glicemia g = new Glicemia();
                g.setId(rs.getInt("id"));
                g.setFkPaziente(rs.getString("fkPaziente"));
                g.setValore(rs.getInt("valore"));
                g.setDateStamp(rs.getTimestamp("dateStamp").toLocalDateTime());
                g.setParteGiorno(rs.getString("parteGiorno"));

                result.add(g);
            }

            return result;
        }
    }

    @Override
    public List<Glicemia> findByPazienteIdAndDate(String codiceFiscale, java.time.LocalDate day) throws Exception {
        String sql = """
            SELECT * FROM Glicemia
            WHERE fkPaziente = ? AND dateStamp BETWEEN ? AND ?
            ORDER BY dateStamp
            """;
        java.time.LocalDateTime start = day.atStartOfDay();
        java.time.LocalDateTime end = day.plusDays(1).atStartOfDay().minusSeconds(1);

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, codiceFiscale);
            ps.setTimestamp(2, Timestamp.valueOf(start));
            ps.setTimestamp(3, Timestamp.valueOf(end));

            try (ResultSet rs = ps.executeQuery()) {
                List<Glicemia> list = new ArrayList<>();
                while (rs.next()) {
                    Glicemia g = new Glicemia();
                    g.setId(rs.getInt("id"));
                    g.setFkPaziente(rs.getString("fkPaziente"));
                    g.setValore(rs.getInt("valore"));
                    g.setParteGiorno(rs.getString("parteGiorno"));
                    Timestamp ts = rs.getTimestamp("dateStamp");
                    if (ts != null) {
                        g.setDateStamp(ts.toLocalDateTime());
                    }
                    list.add(g);
                }
                return list;
            }
        }
    }

    @Override
    public List<Glicemia> findByPazienteIdAndDateRange(String codiceFiscale, java.time.LocalDate start, java.time.LocalDate end) throws Exception {
        String sql = """
            SELECT * FROM Glicemia
            WHERE fkPaziente = ? AND dateStamp BETWEEN ? AND ?
            ORDER BY dateStamp
            """;
        java.time.LocalDateTime startTs = start.atStartOfDay();
        java.time.LocalDateTime endTs = end.plusDays(1).atStartOfDay().minusSeconds(1);

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, codiceFiscale);
            ps.setTimestamp(2, Timestamp.valueOf(startTs));
            ps.setTimestamp(3, Timestamp.valueOf(endTs));

            try (ResultSet rs = ps.executeQuery()) {
                List<Glicemia> list = new ArrayList<>();
                while (rs.next()) {
                    Glicemia g = new Glicemia();
                    g.setId(rs.getInt("id"));
                    g.setFkPaziente(rs.getString("fkPaziente"));
                    g.setValore(rs.getInt("valore"));
                    g.setParteGiorno(rs.getString("parteGiorno"));
                    Timestamp ts = rs.getTimestamp("dateStamp");
                    if (ts != null) {
                        g.setDateStamp(ts.toLocalDateTime());
                    }
                    list.add(g);
                }
                return list;
            }
        }
    }
    @Override
    public void insert(Glicemia g) throws Exception {

        String sql = "INSERT INTO Glicemia (fkPaziente, valore, dateStamp, parteGiorno) VALUES (?, ?, ?, ?)";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, g.getFkPaziente());
            ps.setInt(2, g.getValore());
            ps.setTimestamp(3, Timestamp.valueOf(g.getDateStamp()));
            ps.setString(4, g.getParteGiorno());

            ps.executeUpdate();
        }
    }

    @Override
    public void update(Glicemia g) throws Exception {

        String sql = "UPDATE Glicemia SET valore=?, parteGiorno=? WHERE id=?";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, g.getValore());
            ps.setString(2, g.getParteGiorno());
            ps.setInt(3, g.getId());

            ps.executeUpdate();
        }
    }

    @Override
    public void delete(int id) throws Exception {

        String sql = "DELETE FROM Glicemia WHERE id=?";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }
}
