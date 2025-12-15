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

        p.setNome(rs.getString("Nome"));
        p.setCognome(rs.getString("Cognome"));

        Date dn = rs.getDate("DataNascita");
        if (dn != null) {
            p.setDataNascita(dn.toLocalDate());
        }

        p.setNumeroTelefono(rs.getString("NumeroTelefono"));
        // nel DB la colonna è "eMail"
        p.setEmail(rs.getString("eMail"));
        p.setSesso(rs.getString("Sesso"));
        p.setCodiceFiscale(rs.getString("CodiceFiscale"));
        p.setPassword(rs.getString("Password"));
        p.setIdDiabetologo(rs.getString("fKDiabetologo"));

        return p;
    }

    // --- LOGIN -------------------------------------------------------------

    @Override
    public Paziente findByEmailAndPassword(String email, String password) throws Exception {
        String sql = """
                SELECT *
                FROM Paziente
                WHERE eMail = ? AND Password = ?
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
        SELECT Nome,
               Cognome,
               eMail,
               NumeroTelefono,
               DataNascita,
               Sesso,
               CodiceFiscale,
               Password,
               fkDiabetologo
        FROM Paziente ORDER BY CodiceFiscale DESC
        """;

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            List<Paziente> result = new ArrayList<>();

            while (rs.next()) {
                Paziente p = new Paziente();
                p.setNome(rs.getString("Nome"));
                p.setCognome(rs.getString("Cognome"));
                p.setEmail(rs.getString("eMail"));          // occhio a eMail vs Email
                p.setNumeroTelefono(rs.getString("NumeroTelefono"));

                Date dob = rs.getDate("DataNascita");
                if (dob != null) {
                    p.setDataNascita(dob.toLocalDate());
                }

                p.setSesso(rs.getString("Sesso"));
                p.setCodiceFiscale(rs.getString("CodiceFiscale"));
                p.setPassword(rs.getString("Password"));
                p.setIdDiabetologo(rs.getString("fkDiabetologo"));

                result.add(p);
            }

            return result;
        }
    }
    @Override
    public Paziente findById(String id) throws Exception {
        String sql = """
            SELECT Nome, Cognome, DataNascita, NumeroTelefono,
                   eMail, Sesso, CodiceFiscale, Password, fkDiabetologo
            FROM Paziente
            WHERE CodiceFiscale = ?
            """;

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;

                Paziente p = new Paziente();
                p.setNome(rs.getString("Nome"));
                p.setCognome(rs.getString("Cognome"));

                if (rs.getDate("DataNascita") != null) {
                    p.setDataNascita(rs.getDate("DataNascita").toLocalDate());
                }

                p.setNumeroTelefono(rs.getString("NumeroTelefono"));
                p.setEmail(rs.getString("eMail"));
                p.setSesso(rs.getString("Sesso"));
                p.setCodiceFiscale(rs.getString("CodiceFiscale"));
                p.setPassword(rs.getString("Password"));
                p.setIdDiabetologo(rs.getString("fkDiabetologo"));

                return p;
            }
        }
    }
    @Override
    public void insert(Paziente p) throws Exception {
        String sql = """
            INSERT INTO Paziente
            (Nome, Cognome, DataNascita, NumeroTelefono,
             eMail, Sesso, CodiceFiscale, Password, fkDiabetologo)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, p.getNome());
            ps.setString(2, p.getCognome());

            if (p.getDataNascita() != null) {
                ps.setDate(3, Date.valueOf(p.getDataNascita()));
            } else {
                ps.setNull(3, java.sql.Types.DATE);
            }

            ps.setString(4, p.getNumeroTelefono());
            ps.setString(5, p.getEmail());          // colonna eMail
            ps.setString(6, p.getSesso());
            ps.setString(7, p.getCodiceFiscale());
            ps.setString(8, p.getPassword());
            ps.setString(9, p.getIdDiabetologo());

            ps.executeUpdate();


        }
    }
    @Override
    public void update(Paziente p) throws Exception {
        String sql = """
            UPDATE Paziente
            SET Nome = ?,
                Cognome = ?,
                DataNascita = ?,
                NumeroTelefono = ?,
                eMail = ?,
                Sesso = ?,
                Password = ?,
                fkDiabetologo = ?
            WHERE CodiceFiscale = ?
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
            ps.setString(7, p.getCodiceFiscale());
            ps.setString(8, p.getPassword());
            ps.setString(9, p.getIdDiabetologo());

            ps.executeUpdate();
        }
    }
}