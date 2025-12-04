package it.univr.diabete.dao.impl;

import it.univr.diabete.dao.AssunzioneTerapiaDAO;
import it.univr.diabete.database.Database;
import it.univr.diabete.model.AssunzioneTerapia;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AssunzioneTerapiaDAOImpl implements AssunzioneTerapiaDAO {

    @Override
    public List<AssunzioneTerapia> findByPazienteAndTerapia(int pazienteId, int terapiaId) throws Exception {

        String sql = """
                SELECT Id, IdPaziente, IdTerapia, DateStamp, QuantitaAssunta
                FROM AssunzioneTerapia
                WHERE IdPaziente = ? AND IdTerapia = ?
                ORDER BY DateStamp DESC
                """;

        List<AssunzioneTerapia> result = new ArrayList<>();

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, pazienteId);
            ps.setInt(2, terapiaId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    AssunzioneTerapia a = new AssunzioneTerapia();
                    a.setId(rs.getInt("Id"));
                    a.setIdPaziente(rs.getInt("IdPaziente"));
                    a.setIdTerapia(rs.getInt("IdTerapia"));

                    Timestamp ts = rs.getTimestamp("DateStamp"); // 👈 CAMBIA nome colonna se diverso
                    a.setDateStamp(ts.toLocalDateTime());

                    a.setQuantitaAssunta(rs.getInt("QuantitaAssunta")); // 👈 idem se hai altro nome

                    result.add(a);
                }
            }
        }

        return result;
    }

    @Override
    public void insert(AssunzioneTerapia a) throws Exception {

        String sql = """
                INSERT INTO AssunzioneTerapia
                (IdPaziente, IdTerapia, DateStamp, QuantitaAssunta)
                VALUES (?, ?, ?, ?)
                """;

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, a.getIdPaziente());
            ps.setInt(2, a.getIdTerapia());
            ps.setTimestamp(3, Timestamp.valueOf(a.getDateStamp()));
            ps.setInt(4, a.getQuantitaAssunta());

            ps.executeUpdate();
        }
    }

    @Override
    public void update(AssunzioneTerapia a) throws Exception {

        String sql = """
                UPDATE AssunzioneTerapia
                SET DateStamp = ?, QuantitaAssunta = ?
                WHERE Id = ?
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

        String sql = "DELETE FROM AssunzioneTerapia WHERE Id = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }
}