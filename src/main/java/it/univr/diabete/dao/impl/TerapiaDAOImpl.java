package it.univr.diabete.dao.impl;

import it.univr.diabete.dao.TerapiaDAO;
import it.univr.diabete.database.Database;
import it.univr.diabete.model.Terapia;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TerapiaDAOImpl implements TerapiaDAO {

    @Override
    public List<Terapia> findByPazienteId(String codiceFiscale) throws Exception {
        String sql = """
                SELECT t.id,
                       t.versione,
                       t.dataInizio,
                       t.dataFine,
                       t.fkDiabetologo,
                       t.fkPaziente
                FROM Terapia t
                WHERE t.fkPaziente = ?
                and t.versione = (select max(t2.versione)
                                    from Terapia t2
                                    where t2.fkPaziente = t.fkPaziente
                                    and t2.id = t.id)
                ORDER BY t.dataInizio DESC
                """;

        List<Terapia> result = new ArrayList<>();

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, codiceFiscale);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Terapia t = new Terapia();
                    t.setId(rs.getInt("id"));
                    t.setVersione(rs.getInt("versione"));

                    Date dInizio = rs.getDate("dataInizio");
                    if (dInizio != null) {
                        t.setDataInizio(dInizio.toLocalDate());
                    }

                    Date dFine = rs.getDate("dataFine");
                    if (dFine != null) {
                        t.setDataFine(dFine.toLocalDate());
                    }

                    t.setFkDiabetologo(rs.getString("fkDiabetologo"));
                    t.setFkPaziente(rs.getString("fkPaziente"));

                    result.add(t);
                }
            }
        }

        return result;
    }

    @Override
    public void insert(Terapia t) throws Exception {
        String sql = """
                INSERT INTO Terapia
                    (versione, dataInizio, dataFine, fkDiabetologo, fkPaziente)
                VALUES (?, ?, ?, ?, ?)
                """;

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            // 1) Nome terapia
            ps.setInt(1, 1);

            // 2) Data inizio
            if (t.getDataInizio() != null) {
                ps.setDate(2, Date.valueOf(t.getDataInizio()));
            } else {
                ps.setNull(2, Types.DATE);
            }

            // 3) Data fine
            if (t.getDataFine() != null) {
                ps.setDate(3, Date.valueOf(t.getDataFine()));
            } else {
                ps.setNull(3, Types.DATE);
            }

            // 4) Id diabetologo
            ps.setString(4, t.getFkDiabetologo());

            // 5) Id paziente
            ps.setString(5, t.getFkPaziente());

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    t.setId(rs.getInt(1));
                }
            }
        }
    }

    @Override
    public void update(Terapia t) throws Exception {
        String sql = """
                INSERT INTO Terapia
                    (versione, dataInizio, dataFine, fkDiabetologo, fkPaziente)
                VALUES (?, ?, ?, ?, ?)
                """;

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            // 1) Nome terapia
            ps.setInt(1, t.getVersione() + 1);

            // 2) Data inizio
            if (t.getDataInizio() != null) {
                ps.setDate(2, Date.valueOf(t.getDataInizio()));
            } else {
                ps.setNull(2, Types.DATE);
            }

            // 3) Data fine
            if (t.getDataFine() != null) {
                ps.setDate(3, Date.valueOf(t.getDataFine()));
            } else {
                ps.setNull(3, Types.DATE);
            }

            // 4) Id diabetologo
            ps.setString(4, t.getFkDiabetologo());

            // 5) Id paziente
            ps.setString(5, t.getFkPaziente());

            ps.executeUpdate();
        }
    }
}