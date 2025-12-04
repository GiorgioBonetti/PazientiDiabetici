package it.univr.diabete.dao.impl;

import it.univr.diabete.dao.TerapiaDAO;
import it.univr.diabete.database.Database;
import it.univr.diabete.model.Terapia;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TerapiaDAOImpl implements TerapiaDAO {

    @Override
    public List<Terapia> findByPazienteId(int idPaziente) throws Exception {
        String sql = """
                SELECT t.Id,
                       t.IdFarmaco,
                       t.AssunzioniGiornaliere,
                       t.Quantita,
                       t.DataInizio,
                       t.DataFine,
                       t.IdDiabetologo,
                       t.IdPaziente,
                       f.Nome       AS FarmacoNome,
                       f.Dosaggio   AS FarmacoDosaggio
                FROM Terapia t
                JOIN Farmaco f ON t.IdFarmaco = f.Id
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
                    t.setIdFarmaco(rs.getInt("IdFarmaco"));
                    t.setAssunzioniGiornaliere(rs.getInt("AssunzioniGiornaliere"));
                    t.setQuantita(rs.getInt("Quantita"));

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

                    t.setFarmacoNome(rs.getString("FarmacoNome"));
                    t.setFarmacoDosaggio(rs.getDouble("FarmacoDosaggio"));

                    result.add(t);
                }
            }
        }

        return result;
    }
}