package it.univr.diabete.dao.impl;

import it.univr.diabete.dao.AssunzioneDAO;
import it.univr.diabete.database.Database;
import it.univr.diabete.model.Assunzione;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AssunzioneDAOImpl implements AssunzioneDAO {

    @Override
    public List<Assunzione> findByPazienteAndTerapiaAndFarmaco(String pazienteId,
                                                               int fkFarmaco, int fkTerapia) throws Exception {

        String sql = """
                SELECT id, fkPaziente, fkFarmaco, fkTerapia, dateStamp, quantitaAssunta
                FROM Assunzione
                WHERE fkPaziente = ? AND fkTerapia = ? AND fkFarmaco = ?
                ORDER BY dateStamp DESC
                """;

        List<Assunzione> result = new ArrayList<>();

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, pazienteId);
            ps.setInt(2, fkFarmaco);
            ps.setInt(3, fkTerapia);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Assunzione a = new Assunzione();
                    a.setId(rs.getInt("id"));
                    a.setFkPaziente(rs.getString("fkPaziente"));
                    a.setFkTerapia(rs.getInt("fkTerapia"));
                    a.setFkFarmaco(rs.getInt("fkFarmaco"));

                    Timestamp ts = rs.getTimestamp("dateStamp");
                    if (ts != null) {
                        a.setDateStamp(ts.toLocalDateTime());
                    }

                    a.setQuantitaAssunta(rs.getInt("quantitaAssunta"));
                    result.add(a);
                }
            }
        }

        return result;
    }

    @Override
    public void insert(Assunzione a) throws Exception {
        String sql = """
        INSERT INTO Assunzione (fkPaziente, fkTerapia, fkFarmaco, dateStamp, quantitaAssunta)
        VALUES (?, ?, ?, ?, ?)
    """;

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, a.getFkPaziente());
            ps.setInt(2, a.getFkTerapia());
            ps.setInt(3, a.getFkFarmaco());
            ps.setTimestamp(4, Timestamp.valueOf(a.getDateStamp()));
            ps.setInt(5, a.getQuantitaAssunta());

            ps.executeUpdate();
        }
    }

    @Override
    public void update(Assunzione a) throws Exception {

        String sql = """
                UPDATE Assunzione
                SET dateStamp = ?, quantitaAssunta = ?
                WHERE id = ?
                """;

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setTimestamp(1, Timestamp.valueOf(a.getDateStamp()));
            ps.setInt(2, a.getQuantitaAssunta());
            ps.setInt(3, a.getId());

            ps.executeUpdate();
        }
    }

    @Override
    public void delete(int id) throws Exception {

        String sql = "DELETE FROM Assunzione WHERE id = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }
}