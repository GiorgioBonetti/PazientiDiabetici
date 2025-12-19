package it.univr.diabete.dao.impl;

import it.univr.diabete.dao.FarmacoTerapiaDAO;
import it.univr.diabete.database.Database;
import it.univr.diabete.model.Farmaco;
import it.univr.diabete.model.FarmacoTerapia;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FarmacoTerapiaDAOImpl implements FarmacoTerapiaDAO {

    @Override
    public List<FarmacoTerapia> findByTerapiaId(int terapiaId) throws Exception {
        String sql = """
                SELECT tf.fkTerapia,
                       tf.fkFarmaco,
                       tf.assunzioniGiornaliere,
                       tf.quantita,
                       tf.fkVersioneTerapia,
                       f.Nome AS FarmacoNome,
                       f.Marca AS FarmacoMarca
                FROM FarmacoTerapia tf
                JOIN Farmaco f ON f.id = tf.fkFarmaco
                WHERE tf.fkTerapia = ?
                """;

        List<FarmacoTerapia> result = new ArrayList<>();

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, terapiaId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    FarmacoTerapia tf = new FarmacoTerapia();
                    tf.setFkTerapia(rs.getInt("fkTerapia"));
                    tf.setFkFarmaco(rs.getInt("fkFarmaco"));
                    tf.setAssunzioniGiornaliere(rs.getInt("assunzioniGiornaliere"));
                    tf.setQuantita(rs.getInt("quantita"));
                    tf.setFkVersioneTerapia(rs.getInt("fkVersioneTerapia"));


                    Farmaco f = new Farmaco(
                            rs.getInt("id"),
                            rs.getString("nome"),
                            rs.getString("marca"),
                            rs.getDouble("dosaggio")
                    );
                    tf.setFarmaco(f);

                    result.add(tf);
                }
            }
        }

        return result;
    }

    @Override
    public void insert(FarmacoTerapia tf) throws Exception {
        String sql = """
                INSERT INTO FarmacoTerapia
                (fkTerapia, fkFarmaco, assunzioniGiornaliere, quantita, fkVersioneTerapia)
                VALUES (?, ?, ?, ?, ?)
                """;

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, tf.getFkTerapia());
            ps.setInt(2, tf.getFkFarmaco());
            ps.setInt(3, tf.getAssunzioniGiornaliere());
            ps.setInt(4, tf.getQuantita());
            ps.setInt(5, tf.getFkVersioneTerapia());

            ps.executeUpdate();
        }
    }

    @Override
    public void update(FarmacoTerapia tf) throws Exception {
        String sql = """
                UPDATE FarmacoTerapia
                SET assunzioniGiornaliere = ?,
                    quantita = ?
                WHERE fkTerapia = ? AND fkFarmaco = ? AND fkVersioneTerapia = ?
                """;

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, tf.getFkTerapia());
            ps.setInt(2, tf.getFkFarmaco());
            ps.setInt(3, tf.getAssunzioniGiornaliere());
            ps.setInt(4, tf.getQuantita());
            ps.setInt(5, tf.getFkVersioneTerapia());

            ps.executeUpdate();
        }
    }

    @Override
    public void delete(int fkFarmaco, int fkTerapia, int fkVersioneTerapia) throws Exception {
        String sql = "DELETE FROM FarmacoTerapia WHERE fkFarmaco = ? AND fkTerapia = ? AND fkVersioneTerapia = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, fkTerapia);
            ps.setInt(2, fkFarmaco);
            ps.setInt(3, fkVersioneTerapia);

            ps.executeUpdate();
        }
    }
}