package it.univr.diabete.dao.impl;

import it.univr.diabete.dao.SintomoDAO;
import it.univr.diabete.database.Database;
import it.univr.diabete.model.Sintomo;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class SintomoDAOImpl implements SintomoDAO {

    @Override
    public List<Sintomo> findByPazienteAndDate(String fkPaziente, LocalDate date) throws Exception {
        String sql = """
            SELECT id, descrizione, dataInizio, dataFine, `intensità`, frequenza,
                   noteAggiuntive, fkPaziente, datestamp
            FROM Sintomo
            WHERE fkPaziente = ? AND DATE(datestamp) = ?
            ORDER BY datestamp DESC
            """;
        List<Sintomo> result = new ArrayList<>();
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, fkPaziente);
            ps.setDate(2, Date.valueOf(date));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Sintomo s = mapRow(rs);
                    result.add(s);
                }
            }
        }
        return result;
    }

    @Override
    public Sintomo findLatestByPaziente(String fkPaziente) throws Exception {
        String sql = """
            SELECT id, descrizione, dataInizio, dataFine, `intensità`, frequenza,
                   noteAggiuntive, fkPaziente, datestamp
            FROM Sintomo
            WHERE fkPaziente = ?
            ORDER BY datestamp DESC
            LIMIT 1
            """;
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, fkPaziente);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    @Override
    public void insert(Sintomo s) throws Exception {
        String sql = """
            INSERT INTO Sintomo (descrizione, dataInizio, dataFine, `intensità`,
                                 frequenza, noteAggiuntive, fkPaziente, datestamp)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, s.getDescrizione());
            if (s.getDataInizio() != null) {
                ps.setDate(2, Date.valueOf(s.getDataInizio()));
            } else {
                ps.setNull(2, java.sql.Types.DATE);
            }
            if (s.getDataFine() != null) {
                ps.setDate(3, Date.valueOf(s.getDataFine()));
            } else {
                ps.setNull(3, java.sql.Types.DATE);
            }
            ps.setInt(4, s.getIntensita());
            ps.setString(5, s.getFrequenza());
            ps.setString(6, s.getNoteAggiuntive());
            ps.setString(7, s.getFkPaziente());
            if (s.getDateStamp() != null) {
                ps.setTimestamp(8, Timestamp.valueOf(s.getDateStamp()));
            } else {
                ps.setTimestamp(8, Timestamp.valueOf(java.time.LocalDateTime.now()));
            }
            ps.executeUpdate();
        }
    }

    private Sintomo mapRow(ResultSet rs) throws Exception {
        Sintomo s = new Sintomo();
        s.setId(rs.getInt("id"));
        s.setDescrizione(rs.getString("descrizione"));
        Date di = rs.getDate("dataInizio");
        if (di != null) {
            s.setDataInizio(di.toLocalDate());
        }
        Date df = rs.getDate("dataFine");
        if (df != null) {
            s.setDataFine(df.toLocalDate());
        }
        s.setIntensita(rs.getInt("intensità"));
        s.setFrequenza(rs.getString("frequenza"));
        s.setNoteAggiuntive(rs.getString("noteAggiuntive"));
        s.setFkPaziente(rs.getString("fkPaziente"));
        Timestamp ts = rs.getTimestamp("datestamp");
        if (ts != null) {
            s.setDateStamp(ts.toLocalDateTime());
        }
        return s;
    }
}
