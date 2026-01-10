package it.univr.diabete.dao.impl;

import it.univr.diabete.dao.PazienteDAO;
import it.univr.diabete.database.Database;
import it.univr.diabete.model.Paziente;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PazienteDAOImpl implements PazienteDAO {

    // --- MAPPER UNICO PER RIGA DI RESULTSET -> Paziente --------------------
    private Paziente mapRow(ResultSet rs) throws SQLException {
        Paziente p = new Paziente();

        p.setNome(rs.getString("nome"));
        p.setCognome(rs.getString("cognome"));

        Date dn = rs.getDate("dataNascita");
        if (dn != null) {
            p.setDataNascita(dn.toLocalDate());
        }

        p.setNumeroTelefono(rs.getString("numeroTelefono"));
        p.setEmail(rs.getString("email"));
        p.setSesso(rs.getString("sesso"));
        p.setCodiceFiscale(rs.getString("codiceFiscale"));
        p.setPassword(rs.getString("password"));
        p.setFkDiabetologo(rs.getString("fKDiabetologo"));

        return p;
    }

    // --- LOGIN -------------------------------------------------------------

    @Override
    public Paziente findByEmailAndPassword(String email, String password) throws Exception {
        String sql = """
                SELECT *
                FROM Paziente
                WHERE email = ? AND password = ?
                """;

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);
            ps.setString(2, password);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
                return null;
            }
        }
    }

    // --- LISTA COMPLETA ----------------------------------------------------

    @Override
    public List<Paziente> findAll() throws Exception {
        String sql = """
        SELECT nome, cognome, email, numeroTelefono, dataNascita, sesso, codiceFiscale, password, fkDiabetologo
        FROM Paziente ORDER BY codiceFiscale DESC
        """;

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            List<Paziente> result = new ArrayList<>();

            while (rs.next()) {
                Paziente p = new Paziente();
                p.setNome(rs.getString("nome"));
                p.setCognome(rs.getString("cognome"));
                p.setEmail(rs.getString("email"));
                p.setNumeroTelefono(rs.getString("numeroTelefono"));

                Date dob = rs.getDate("dataNascita");
                if (dob != null) {
                    p.setDataNascita(dob.toLocalDate());
                }

                p.setSesso(rs.getString("sesso"));
                p.setCodiceFiscale(rs.getString("codiceFiscale"));
                p.setPassword(rs.getString("password"));
                p.setFkDiabetologo(rs.getString("fkDiabetologo"));

                result.add(p);
            }

            return result;
        }
    }
    @Override
    public Paziente findById(String id) throws Exception {
        String sql = """
            SELECT nome, cognome, email, numeroTelefono, dataNascita, sesso, codiceFiscale, password, fkDiabetologo
            FROM Paziente
            WHERE codiceFiscale = ?
            """;

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;

                Paziente p = new Paziente();
                p.setNome(rs.getString("nome"));
                p.setCognome(rs.getString("cognome"));

                if (rs.getDate("dataNascita") != null) {
                    p.setDataNascita(rs.getDate("dataNascita").toLocalDate());
                }

                p.setNumeroTelefono(rs.getString("numeroTelefono"));
                p.setEmail(rs.getString("email"));
                p.setSesso(rs.getString("sesso"));
                p.setCodiceFiscale(rs.getString("codiceFiscale"));
                p.setPassword(rs.getString("password"));
                p.setFkDiabetologo(rs.getString("fkDiabetologo"));

                return p;
            }
        }
    }
    @Override
    public void insert(Paziente p) throws Exception {
        String sql = """
            INSERT INTO Paziente
            (nome, cognome, email, numeroTelefono, dataNascita, sesso, codiceFiscale, password, fkDiabetologo)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, p.getNome());
            ps.setString(2, p.getCognome());
            ps.setString(3, p.getEmail());
            ps.setString(4, p.getNumeroTelefono());

            if (p.getDataNascita() != null) {
                ps.setDate(5, Date.valueOf(p.getDataNascita()));
            } else {
                ps.setNull(5, java.sql.Types.DATE);
            }

            ps.setString(6, p.getSesso());
            ps.setString(7, p.getCodiceFiscale());
            ps.setString(8, p.getPassword());
            ps.setString(9, p.getFkDiabetologo());

            ps.executeUpdate();


        }
    }
    @Override
    public void update(Paziente p) throws Exception {
        String sql = """
            UPDATE Paziente
            SET nome = ?,
                cognome = ?,
                dataNascita = ?,
                numeroTelefono = ?,
                email = ?,
                sesso = ?,
                password = ?,
                fkDiabetologo = ?
            WHERE codiceFiscale = ?
            """;

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, p.getNome());
            ps.setString(2, p.getCognome());

            if (p.getDataNascita() != null) {
                ps.setDate(3, Date.valueOf(p.getDataNascita()));
            } else {
                ps.setNull(3, java.sql.Types.DATE);
            }

            ps.setString(4, p.getNumeroTelefono());
            ps.setString(5, p.getEmail());
            ps.setString(6, p.getSesso());
            ps.setString(7, p.getPassword());
            ps.setString(8, p.getFkDiabetologo());
            ps.setString(9, p.getCodiceFiscale());

            ps.executeUpdate();
        }
    }

    @Override
    public void deleteById(String codiceFiscale) throws Exception {
        String sql = "DELETE FROM Paziente WHERE codiceFiscale = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, codiceFiscale);
            ps.executeUpdate();
        }
    }
}
