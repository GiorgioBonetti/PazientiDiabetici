package it.univr.diabete.dao.impl;

import it.univr.diabete.dao.GlicemiaDAO;
import it.univr.diabete.database.Database;
import it.univr.diabete.model.Glicemia;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class GlicemiaDAOImpl implements GlicemiaDAO {

    @Override
    public List<Glicemia> findByPazienteId(String codiceFiscale) throws Exception {

        String sql = "SELECT * FROM Glicemia WHERE IdPaziente = ? ORDER BY DateTime ASC";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, codiceFiscale);
            ResultSet rs = ps.executeQuery();

            List<Glicemia> list = new ArrayList<>();

            while (rs.next()) {
                Glicemia g = new Glicemia();
                g.setId(rs.getInt("Id"));
                g.setIdPaziente(rs.getString("IdPaziente"));
                g.setValore(rs.getInt("Valore"));
                g.setMomento(rs.getString("Momento"));   // ⬅ AGGIUNTO
                Timestamp ts = rs.getTimestamp("DateTime");
                g.setDataOra(ts.toLocalDateTime());

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
                g.setId(rs.getInt("Id"));
                g.setIdPaziente(rs.getString("fkPaziente"));
                g.setValore(rs.getInt("Valore"));
                g.setDataOra(rs.getTimestamp("DateStamp").toLocalDateTime());
                g.setMomento(rs.getString("ParteGiorno")); // se hai la colonna

                result.add(g);
            }

            return result;
        }
    }
    @Override
    public void insert(Glicemia g) throws Exception {

        String sql = "INSERT INTO Glicemia (fkPaziente, Valore, DateStamp, ParteGiorno) VALUES (?, ?, ?, ?)";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, g.getIdPaziente());
            ps.setInt(2, g.getValore());
            ps.setTimestamp(3, Timestamp.valueOf(g.getDataOra()));
            ps.setString(4, g.getMomento());

            ps.executeUpdate();
        }
    }

    @Override
    public void update(Glicemia g) throws Exception {

        String sql = "UPDATE Glicemia SET Valore=?, ParteGiorno=? WHERE Id=?";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, g.getValore());
            ps.setString(2, g.getMomento());
            ps.setInt(3, g.getId());

            ps.executeUpdate();
        }
    }

    @Override
    public void delete(int id) throws Exception {

        String sql = "DELETE FROM Glicemia WHERE Id=?";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }
}