package it.univr.diabete.dao.impl;

import it.univr.diabete.dao.PatologiaDAO;
import it.univr.diabete.database.Database;
import it.univr.diabete.model.Patologia;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class PatologiaDAOImpl implements PatologiaDAO {

    @Override
    public List<Patologia> findByPaziente(String fkPaziente) throws Exception {
        String sql = """
            SELECT id, nome, dataInizio, fkPaziente
            FROM Patologia
            WHERE fkPaziente = ?
            ORDER BY nome
            """;
        List<Patologia> result = new ArrayList<>();
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, fkPaziente);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Patologia p = new Patologia();
                    p.setId(rs.getInt("id"));
                    p.setNome(rs.getString("nome"));
                    Date di = rs.getDate("dataInizio");
                    if (di != null) {
                        p.setDataInizio(di.toLocalDate());
                    }
                    p.setFkPaziente(rs.getString("fkPaziente"));
                    result.add(p);
                }
            }
        }
        return result;
    }

    @Override
    public void insert(Patologia patologia) throws Exception {
        String sql = """
            INSERT INTO Patologia (nome, dataInizio, fkPaziente)
            VALUES (?, ?, ?)
            """;
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, patologia.getNome());
            if (patologia.getDataInizio() != null) {
                ps.setDate(2, Date.valueOf(patologia.getDataInizio()));
            } else {
                ps.setNull(2, java.sql.Types.DATE);
            }
            ps.setString(3, patologia.getFkPaziente());
            ps.executeUpdate();
        }
    }

    @Override
    public void delete(int id) throws Exception {
        String sql = "DELETE FROM Patologia WHERE id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    @Override
    public boolean existsByPazienteAndNome(String fkPaziente, String nome) throws Exception {
        String sql = """
            SELECT 1
            FROM Patologia
            WHERE fkPaziente = ? AND LOWER(nome) = LOWER(?)
            LIMIT 1
            """;
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, fkPaziente);
            ps.setString(2, nome);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }
}
