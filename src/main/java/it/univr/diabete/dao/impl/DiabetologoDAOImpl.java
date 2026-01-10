package it.univr.diabete.dao.impl;

import it.univr.diabete.database.Database;
import it.univr.diabete.dao.DiabetologoDAO;
import it.univr.diabete.model.Diabetologo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class DiabetologoDAOImpl implements DiabetologoDAO {

    @Override
    public Diabetologo findByEmailAndPassword(String email, String password) throws Exception {
        String sql = """
            SELECT nome, cognome, email, password, numeroTelefono, sesso, laurea
            FROM Diabetologo
            WHERE email = ? AND password = ?
            """;

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);
            ps.setString(2, password);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Diabetologo(
                            rs.getString("nome"),
                            rs.getString("cognome"),
                            rs.getString("email"),
                            rs.getString("password"),
                            rs.getString("numeroTelefono"),
                            rs.getString("sesso"),
                            rs.getString("laurea")
                    );
                }
                return null;
            }
        }
    }
    @Override
    public List<Diabetologo> findAll() throws Exception {
        String sql = "SELECT nome, cognome, email, password, numeroTelefono, sesso, laurea FROM Diabetologo ORDER BY cognome, nome";
        List<Diabetologo> list = new ArrayList<>();

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new Diabetologo(
                        rs.getString("nome"),
                        rs.getString("cognome"),
                        rs.getString("email"),
                        rs.getString("password"),
                        rs.getString("numeroTelefono"),
                        rs.getString("sesso"),
                        rs.getString("laurea")
                ));
            }
        }
        return list;
    }

    @Override
    public void insert(Diabetologo d) throws Exception {
        String sql = """
        INSERT INTO Diabetologo(nome, cognome, email, password, numeroTelefono, sesso, laurea)
        VALUES (?,?,?,?,?,?,?)
    """;
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, d.getNome());
            ps.setString(2, d.getCognome());
            ps.setString(3, d.getEmail());
            ps.setString(4, d.getPassword());
            ps.setString(5, d.getNumeroTelefono());
            ps.setString(6, d.getSesso());
            ps.setString(7, d.getLaurea());
            ps.executeUpdate();
        }
    }

    @Override
    public void update(Diabetologo d) throws Exception {
        String sql = """
        UPDATE Diabetologo
        SET nome = ?,
            cognome = ?,
            password = ?,
            numeroTelefono = ?,
            sesso = ?,
            laurea = ?
        WHERE email = ?
    """;
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, d.getNome());
            ps.setString(2, d.getCognome());
            ps.setString(3, d.getPassword());
            ps.setString(4, d.getNumeroTelefono());
            ps.setString(5, d.getSesso());
            ps.setString(6, d.getLaurea());
            ps.setString(7, d.getEmail());
            ps.executeUpdate();
        }
    }

    @Override
    public void deleteByEmail(String email) throws Exception {
        String sql = "DELETE FROM Diabetologo WHERE email = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.executeUpdate();
        }
    }

}
