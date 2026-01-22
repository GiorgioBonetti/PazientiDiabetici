package it.univr.diabete.dao.impl;

import it.univr.diabete.dao.FarmacoDAO;
import it.univr.diabete.database.Database;
import it.univr.diabete.model.Farmaco;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FarmacoDAOImpl implements FarmacoDAO {

    @Override
    public List<Farmaco> findAll() throws Exception {
        String sql = "SELECT id, nome, marca FROM Farmaco ORDER BY nome";

        List<Farmaco> result = new ArrayList<>();

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Farmaco f = new Farmaco(
                        rs.getInt("id"),
                        rs.getString("nome"),
                        rs.getString("marca"));
                result.add(f);
            }
        }

        return result;
    }

    @Override
    public Farmaco findById(int id) throws Exception {
        String sql = "SELECT id, nome, marca FROM Farmaco WHERE id = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Farmaco(
                            rs.getInt("id"),
                            rs.getString("nome"),
                            rs.getString("marca"));
                }
            }
        }

        return null;
    }

    @Override
    public void insert(Farmaco f) throws Exception {
        String sql = """
                INSERT INTO Farmaco (nome, marca)
                VALUES (?, ?)
                """;

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, f.getNome());
            ps.setString(2, f.getMarca() != null ? f.getMarca() : "");

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    f.setId(rs.getInt(1));
                }
            }
        }
    }

    @Override
    public void update(Farmaco f) throws Exception {
        String sql = """
                UPDATE Farmaco
                SET nome = ?, marca = ?
                WHERE id = ?
                """;

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, f.getNome());
            ps.setString(2, f.getMarca() != null ? f.getMarca() : "");
            ps.setInt(3, f.getId());

            ps.executeUpdate();
        }
    }

    @Override
    public void delete(int id) throws Exception {
        String sql = "DELETE FROM Farmaco WHERE id = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }
}