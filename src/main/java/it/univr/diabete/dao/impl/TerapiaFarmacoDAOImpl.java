package it.univr.diabete.dao.impl;

import it.univr.diabete.dao.TerapiaFarmacoDAO;
import it.univr.diabete.database.Database;
import it.univr.diabete.model.Farmaco;
import it.univr.diabete.model.TerapiaFarmaco;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TerapiaFarmacoDAOImpl implements TerapiaFarmacoDAO {

    @Override
    public List<TerapiaFarmaco> findByTerapiaId(int terapiaId) throws Exception {
        String sql = """
                SELECT tf.Id,
                       tf.IdTerapia,
                       tf.IdFarmaco,
                       tf.AssunzioniGiornaliere,
                       tf.QuantitaAssunzione,
                       f.Nome AS FarmacoNome,
                       f.Marca AS FarmacoMarca
                FROM TerapiaFarmaco tf
                JOIN Farmaco f ON f.Id = tf.IdFarmaco
                WHERE tf.IdTerapia = ?
                """;

        List<TerapiaFarmaco> result = new ArrayList<>();

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, terapiaId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    TerapiaFarmaco tf = new TerapiaFarmaco();
                    tf.setId(rs.getInt("Id"));
                    tf.setIdTerapia(rs.getInt("IdTerapia"));
                    tf.setIdFarmaco(rs.getInt("IdFarmaco"));
                    tf.setAssunzioniGiornaliere(rs.getInt("AssunzioniGiornaliere"));
                    tf.setQuantitaAssunzione(rs.getInt("QuantitaAssunzione"));


                    Farmaco f = new Farmaco(
                            rs.getInt("IdFarmaco"),
                            rs.getString("FarmacoNome"),
                            rs.getString("FarmacoMarca")
                    );
                    tf.setFarmaco(f);

                    result.add(tf);
                }
            }
        }

        return result;
    }

    @Override
    public void insert(TerapiaFarmaco tf) throws Exception {
        String sql = """
                INSERT INTO TerapiaFarmaco
                (IdTerapia, IdFarmaco, AssunzioniGiornaliere, QuantitaAssunzione)
                VALUES (?, ?, ?, ?)
                """;

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, tf.getIdTerapia());
            ps.setInt(2, tf.getIdFarmaco());
            ps.setInt(3, tf.getAssunzioniGiornaliere());
            ps.setInt(4, tf.getQuantitaAssunzione());


            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    tf.setId(rs.getInt(1));
                }
            }
        }
    }

    @Override
    public void update(TerapiaFarmaco tf) throws Exception {
        String sql = """
                UPDATE TerapiaFarmaco
                SET IdTerapia = ?,
                    IdFarmaco = ?,
                    AssunzioniGiornaliere = ?,
                    QuantitaAssunzione = ?
           
                WHERE Id = ?
                """;

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, tf.getIdTerapia());
            ps.setInt(2, tf.getIdFarmaco());
            ps.setInt(3, tf.getAssunzioniGiornaliere());
            ps.setInt(4, tf.getQuantitaAssunzione());

            ps.setInt(6, tf.getId());

            ps.executeUpdate();
        }
    }

    @Override
    public void delete(int id) throws Exception {
        String sql = "DELETE FROM TerapiaFarmaco WHERE Id = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }
}