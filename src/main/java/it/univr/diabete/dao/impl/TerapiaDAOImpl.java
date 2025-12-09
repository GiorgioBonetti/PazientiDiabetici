package it.univr.diabete.dao.impl;

import it.univr.diabete.dao.TerapiaDAO;
import it.univr.diabete.database.Database;
import it.univr.diabete.model.Terapia;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TerapiaDAOImpl implements TerapiaDAO {

    @Override
    public List<Terapia> findByPazienteId(int idPaziente) throws Exception {
        String sql = """
                SELECT t.Id,
                       t.Nome,
                       t.DataInizio,
                       t.DataFine,
                       t.IdDiabetologo,
                       t.IdPaziente
                FROM Terapia t
                WHERE t.IdPaziente = ?
                ORDER BY t.DataInizio DESC
                """;

        List<Terapia> result = new ArrayList<>();

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idPaziente);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Terapia t = new Terapia();
                    t.setId(rs.getInt("Id"));
                    t.setNome(rs.getString("Nome"));

                    Date dInizio = rs.getDate("DataInizio");
                    if (dInizio != null) {
                        t.setDataInizio(dInizio.toLocalDate());
                    }

                    Date dFine = rs.getDate("DataFine");
                    if (dFine != null) {
                        t.setDataFine(dFine.toLocalDate());
                    }

                    t.setIdDiabetologo(rs.getInt("IdDiabetologo"));
                    t.setIdPaziente(rs.getInt("IdPaziente"));

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
                    (Nome, DataInizio, DataFine, IdDiabetologo, IdPaziente)
                VALUES (?, ?, ?, ?, ?)
                """;

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            // 1) Nome terapia
            ps.setString(1, t.getNome());

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
            ps.setInt(4, t.getIdDiabetologo());

            // 5) Id paziente
            ps.setInt(5, t.getIdPaziente());

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
                UPDATE Terapia
                SET Nome = ?,
                    DataInizio = ?,
                    DataFine = ?,
                    IdDiabetologo = ?,
                    IdPaziente = ?
                WHERE Id = ?
                """;

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            // 1) Nome terapia
            ps.setString(1, t.getNome());

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
            ps.setInt(4, t.getIdDiabetologo());

            // 5) Id paziente
            ps.setInt(5, t.getIdPaziente());

            // 6) Id della terapia da aggiornare
            ps.setInt(6, t.getId());

            ps.executeUpdate();
        }
    }
}