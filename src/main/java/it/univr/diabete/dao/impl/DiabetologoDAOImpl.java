package it.univr.diabete.dao.impl;

import it.univr.diabete.database.Database;
import it.univr.diabete.dao.DiabetologoDAO;
import it.univr.diabete.model.Diabetologo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class DiabetologoDAOImpl implements DiabetologoDAO {

    @Override
    public Diabetologo findByEmailAndPassword(String email, String password) throws Exception {
        String sql = """
            SELECT Nome, Cognome, eMail, Password
            FROM Diabetologo
            WHERE eMail = ? AND Password = ?
            """;

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);
            ps.setString(2, password);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Diabetologo(
                            rs.getString("Nome"),
                            rs.getString("Cognome"),
                            rs.getString("eMail"),
                            rs.getString("Password")
                    );
                }
                return null;
            }
        }
    }
}
