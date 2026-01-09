package it.univr.diabete.dao.impl;

import it.univr.diabete.dao.TerapiaDAO;
import it.univr.diabete.database.Database;
import it.univr.diabete.model.Terapia;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TerapiaDAOImpl implements TerapiaDAO {

    @Override
    public List<Terapia> findByPazienteId(String fkPaziente) throws Exception {
        String sql = """
                SELECT t.id,
                       t.nome,
                       t.dataInizio,
                       t.dataFine,
                       t.ultimaModifica,
                       t.fkDiabetologo,
                       t.fkPaziente
                FROM Terapia t
                WHERE t.fkPaziente = ?
                ORDER BY t.dataInizio DESC
                """;

        List<Terapia> result = new ArrayList<>();

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, fkPaziente);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Terapia t = new Terapia();
                    t.setId(rs.getInt("id"));
                    t.setNome(rs.getString("nome"));

                    Date dInizio = rs.getDate("dataInizio");
                    if (dInizio != null) t.setDataInizio(dInizio.toLocalDate());

                    Date dFine = rs.getDate("dataFine");
                    if (dFine != null) t.setDataFine(dFine.toLocalDate());

                    Timestamp ts = rs.getTimestamp("ultimaModifica");
                    if (ts != null) t.setUltimaModifica(ts.toLocalDateTime());

                    // Se nel tuo model sono int cambia qui sotto (vedi nota)
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
                    (nome, dataInizio, dataFine, ultimaModifica, fkDiabetologo, fkPaziente)
                VALUES (?, ?, ?, NOW(), ?, ?)
                """;

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            // 1) Nome terapia
            ps.setString(1, t.getNome());

            // 2) Data inizio
            if (t.getDataInizio() != null) ps.setDate(2, Date.valueOf(t.getDataInizio()));
            else ps.setNull(2, Types.DATE);

            // 3) Data fine
            if (t.getDataFine() != null) ps.setDate(3, Date.valueOf(t.getDataFine()));
            else ps.setNull(3, Types.DATE);

            // 4) FK diabetologo
            ps.setString(4, t.getFkDiabetologo());

            // 5) FK paziente
            ps.setString(5, t.getFkPaziente());

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) t.setId(rs.getInt(1));
            }

            // opzionale: tieni l'ultimaModifica in memoria
            t.setUltimaModifica(LocalDateTime.now());
        }
    }

    @Override
    public void update(Terapia t) throws Exception {
        String sql = """
                UPDATE Terapia
                SET nome = ?,
                    dataInizio = ?,
                    dataFine = ?,
                    ultimaModifica = NOW(),
                    fkDiabetologo = ?,
                    fkPaziente = ?
                WHERE id = ?
                """;

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, t.getNome());

            if (t.getDataInizio() != null) ps.setDate(2, Date.valueOf(t.getDataInizio()));
            else ps.setNull(2, Types.DATE);

            if (t.getDataFine() != null) ps.setDate(3, Date.valueOf(t.getDataFine()));
            else ps.setNull(3, Types.DATE);

            ps.setString(4, t.getFkDiabetologo());
            ps.setString(5, t.getFkPaziente());

            ps.setInt(6, t.getId());

            ps.executeUpdate();

            t.setUltimaModifica(LocalDateTime.now());
        }
    }
}