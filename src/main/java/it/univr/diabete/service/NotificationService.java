package it.univr.diabete.service;

import it.univr.diabete.dao.AssunzioneDAO;
import it.univr.diabete.dao.DiabetologoDAO;
import it.univr.diabete.dao.FarmacoTerapiaDAO;
import it.univr.diabete.dao.GlicemiaDAO;
import it.univr.diabete.dao.MessageDAO;
import it.univr.diabete.dao.NotificationDAO;
import it.univr.diabete.dao.PazienteDAO;
import it.univr.diabete.dao.TerapiaDAO;
import it.univr.diabete.dao.impl.AssunzioneDAOImpl;
import it.univr.diabete.dao.impl.DiabetologoDAOImpl;
import it.univr.diabete.dao.impl.FarmacoTerapiaDAOImpl;
import it.univr.diabete.dao.impl.GlicemiaDAOImpl;
import it.univr.diabete.dao.impl.MessageDAOImpl;
import it.univr.diabete.dao.impl.NotificationDAOImpl;
import it.univr.diabete.dao.impl.PazienteDAOImpl;
import it.univr.diabete.dao.impl.TerapiaDAOImpl;
import it.univr.diabete.model.Assunzione;
import it.univr.diabete.model.Diabetologo;
import it.univr.diabete.model.FarmacoTerapia;
import it.univr.diabete.model.Glicemia;
import it.univr.diabete.model.Notification;
import it.univr.diabete.model.Paziente;
import it.univr.diabete.model.Terapia;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class NotificationService {

    public static final String ROLE_PATIENT = "PATIENT";
    public static final String ROLE_DOCTOR = "DOCTOR";

    public static final String TYPE_GLUCOSE_REMINDER = "GLUCOSE_REMINDER";
    public static final String TYPE_DRUG_REMINDER = "DRUG_REMINDER";
    public static final String TYPE_THERAPY_STATUS = "THERAPY_STATUS";
    public static final String TYPE_GLUCOSE_HIGH = "GLUCOSE_HIGH";
    public static final String TYPE_ADHERENCE_POOR = "ADHERENCE_POOR";
    public static final String TYPE_MESSAGES_UNREAD = "MESSAGES_UNREAD";

    public static final String SEVERITY_INFO = "INFO";
    public static final String SEVERITY_WARNING = "WARNING";
    public static final String SEVERITY_CRITICAL = "CRITICAL";

    public static final String ACTION_OPEN_PATIENT = "OPEN_PATIENT";
    public static final String ACTION_OPEN_THERAPY = "OPEN_THERAPY";
    public static final String ACTION_OPEN_MEASUREMENTS = "OPEN_MEASUREMENTS";
    public static final String ACTION_OPEN_MESSAGES = "OPEN_MESSAGES";
    public static final String ACTION_OPEN_REPORT = "OPEN_REPORT";

    private static final int MAX_NOTIFICATIONS = 30;
    private static final Duration PATIENT_HEAVY_TTL = Duration.ofMinutes(2);
    private static final Duration DOCTOR_HEAVY_TTL = Duration.ofHours(6);

    private static final DateTimeFormatter DATE_KEY = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATE_DISPLAY = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final Map<String, LocalDateTime> LAST_PATIENT_HEAVY_RUN = new ConcurrentHashMap<>();
    private static final Map<String, LocalDateTime> LAST_DOCTOR_HEAVY_RUN = new ConcurrentHashMap<>();

    private final NotificationDAO notificationDAO = new NotificationDAOImpl();
    private final PazienteDAO pazienteDAO = new PazienteDAOImpl();
    private final DiabetologoDAO diabetologoDAO = new DiabetologoDAOImpl();
    private final MessageDAO messageDAO = new MessageDAOImpl();
    private final GlicemiaDAO glicemiaDAO = new GlicemiaDAOImpl();
    private final TerapiaDAO terapiaDAO = new TerapiaDAOImpl();
    private final FarmacoTerapiaDAO farmacoTerapiaDAO = new FarmacoTerapiaDAOImpl();
    private final AssunzioneDAO assunzioneDAO = new AssunzioneDAOImpl();

    public List<Notification> fetchLatest(String role, String userId, int limit) throws Exception {
        String targetRole = normalizeRole(role);
        if (targetRole == null || userId == null) {
            return List.of();
        }
        int lim = limit > 0 ? limit : MAX_NOTIFICATIONS;
        return notificationDAO.findByTarget(targetRole, userId, lim);
    }

    public int countUnread(String role, String userId) throws Exception {
        String targetRole = normalizeRole(role);
        if (targetRole == null || userId == null) {
            return 0;
        }
        return notificationDAO.countUnread(targetRole, userId);
    }

    public void markAsRead(int id) throws Exception {
        notificationDAO.markRead(id);
    }

    public void markAllAsRead(String role, String userId) throws Exception {
        String targetRole = normalizeRole(role);
        if (targetRole == null || userId == null) {
            return;
        }
        notificationDAO.markAllRead(targetRole, userId);
    }

    public String normalizeRole(String role) {
        if (role == null) {
            return null;
        }
        if ("Paziente".equalsIgnoreCase(role) || ROLE_PATIENT.equalsIgnoreCase(role)) {
            return ROLE_PATIENT;
        }
        if ("Diabetologo".equalsIgnoreCase(role) || ROLE_DOCTOR.equalsIgnoreCase(role)) {
            return ROLE_DOCTOR;
        }
        return role.trim().toUpperCase();
    }

    public void generatePatientDashboardNotifications(String patientId) throws Exception {
        if (patientId == null || patientId.isBlank()) {
            return;
        }
        if (shouldRunHeavy(LAST_PATIENT_HEAVY_RUN, patientId, PATIENT_HEAVY_TTL)) {
            createMissingMeasurementReminders(patientId);
            createDrugReminder(patientId);
            createTherapyStatusNotifications(patientId);
        }
        createPatientMessageNotifications(patientId);
    }

    public void generateDoctorDashboardNotifications(String doctorId) throws Exception {
        if (doctorId == null || doctorId.isBlank()) {
            return;
        }
        if (shouldRunHeavy(LAST_DOCTOR_HEAVY_RUN, doctorId, DOCTOR_HEAVY_TTL)) {
            createPoorAdherenceNotifications(doctorId);
        }
        createDoctorMessageNotifications(doctorId);
    }

    public void onGlucoseRecorded(Glicemia g) throws Exception {
        if (g == null || g.getFkPaziente() == null) {
            return;
        }
        Paziente paziente = pazienteDAO.findById(g.getFkPaziente());
        if (paziente == null || paziente.getFkDiabetologo() == null) {
            return;
        }
        createHighGlucoseNotification(paziente, g);
    }

    private void createMissingMeasurementReminders(String patientId) throws Exception {
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        List<Glicemia> all = glicemiaDAO.findByPazienteIdAndDate(patientId, today);
        boolean mattinaDone = hasMeasurement(all, today, "Mattina");
        boolean pomeriggioDone = hasMeasurement(all, today, "Pomeriggio");
        boolean seraDone = hasMeasurement(all, today, "Sera");

        Map<String, LocalTime> cutoff = new HashMap<>();
        cutoff.put("Mattina", LocalTime.of(10, 30));
        cutoff.put("Pomeriggio", LocalTime.of(15, 30));
        cutoff.put("Sera", LocalTime.of(21, 30));

        if (now.isAfter(cutoff.get("Mattina")) && !mattinaDone) {
            createOrUpdateNotification(buildNotification(
                    ROLE_PATIENT,
                    patientId,
                    TYPE_GLUCOSE_REMINDER,
                    SEVERITY_INFO,
                    "Promemoria glicemia",
                    "Ricorda di misurare e registrare la glicemia prima di colazione.",
                    ACTION_OPEN_MEASUREMENTS,
                    today.format(DATE_KEY) + ":Mattina"
            ));
        } else if (mattinaDone) {
            notificationDAO.markReadByKey(ROLE_PATIENT, patientId, TYPE_GLUCOSE_REMINDER,
                    today.format(DATE_KEY) + ":Mattina");
        }

        if (now.isAfter(cutoff.get("Pomeriggio")) && !pomeriggioDone) {
            createOrUpdateNotification(buildNotification(
                    ROLE_PATIENT,
                    patientId,
                    TYPE_GLUCOSE_REMINDER,
                    SEVERITY_INFO,
                    "Promemoria glicemia",
                    "Ricorda di registrare la glicemia dopo pranzo (circa 2 ore).",
                    ACTION_OPEN_MEASUREMENTS,
                    today.format(DATE_KEY) + ":Pomeriggio"
            ));
        } else if (pomeriggioDone) {
            notificationDAO.markReadByKey(ROLE_PATIENT, patientId, TYPE_GLUCOSE_REMINDER,
                    today.format(DATE_KEY) + ":Pomeriggio");
        }

        if (now.isAfter(cutoff.get("Sera")) && !seraDone) {
            createOrUpdateNotification(buildNotification(
                    ROLE_PATIENT,
                    patientId,
                    TYPE_GLUCOSE_REMINDER,
                    SEVERITY_INFO,
                    "Promemoria glicemia",
                    "Ricorda di misurare e registrare la glicemia prima di cena.",
                    ACTION_OPEN_MEASUREMENTS,
                    today.format(DATE_KEY) + ":Sera"
            ));
        } else if (seraDone) {
            notificationDAO.markReadByKey(ROLE_PATIENT, patientId, TYPE_GLUCOSE_REMINDER,
                    today.format(DATE_KEY) + ":Sera");
        }
    }

    private boolean hasMeasurement(List<Glicemia> all, LocalDate day, String momento) {
        return all.stream()
                .anyMatch(g -> g.getDateStamp() != null
                        && g.getDateStamp().toLocalDate().isEqual(day)
                        && momento.equalsIgnoreCase(g.getParteGiorno()));
    }

    private void createDrugReminder(String patientId) throws Exception {
        LocalDate today = LocalDate.now();
        List<Terapia> terapie = terapiaDAO.findByPazienteId(patientId);
        int expectedDosesTotal = 0;
        int actualDosesTotal = 0;
        List<Assunzione> assunzioniToday = assunzioneDAO.findByPazienteAndDateRange(patientId, today, today);
        Map<String, Integer> assunzioniByKey = new HashMap<>();
        for (Assunzione a : assunzioniToday) {
            if (a.getDateStamp() == null) {
                continue;
            }
            String key = a.getFkTerapia() + ":" + a.getFkFarmaco();
            assunzioniByKey.merge(key, a.getQuantitaAssunta(), Integer::sum);
        }

        for (Terapia t : terapie) {
            if (!isTherapyActiveOn(t, today)) {
                continue;
            }
            List<FarmacoTerapia> farmaci = farmacoTerapiaDAO.findByTerapiaId(t.getId());
            for (FarmacoTerapia tf : farmaci) {
                int expectedForFarmaco = tf.getAssunzioniGiornaliere();
                expectedDosesTotal += expectedForFarmaco;

                String key = t.getId() + ":" + tf.getFkFarmaco();
                int takenQty = assunzioniByKey.getOrDefault(key, 0);
                int quantitaPerDose = tf.getQuantita();
                if (quantitaPerDose > 0) {
                    actualDosesTotal += (takenQty / quantitaPerDose);
                }
            }
        }

        String actionRefId = today.format(DATE_KEY);
        if (expectedDosesTotal <= 0) {
            notificationDAO.markReadByKey(ROLE_PATIENT, patientId, TYPE_DRUG_REMINDER, actionRefId);
            return;
        }

        if (actualDosesTotal < expectedDosesTotal) {
            int remaining = expectedDosesTotal - actualDosesTotal;
            String body = remaining == 1
                    ? "Ti manca ancora 1 dose oggi."
                    : "Ti mancano ancora " + remaining + " dosi oggi.";
            createOrUpdateNotification(buildNotification(
                    ROLE_PATIENT,
                    patientId,
                    TYPE_DRUG_REMINDER,
                    SEVERITY_WARNING,
                    "Promemoria terapia",
                    body,
                    ACTION_OPEN_THERAPY,
                    actionRefId
            ));
        } else {
            notificationDAO.markReadByKey(ROLE_PATIENT, patientId, TYPE_DRUG_REMINDER, actionRefId);
        }
    }

    private void createTherapyStatusNotifications(String patientId) throws Exception {
        LocalDate today = LocalDate.now();
        List<Terapia> terapie = terapiaDAO.findByPazienteId(patientId);
        for (Terapia t : terapie) {
            if (t.getDataFine() == null) {
                continue;
            }
            long daysLeft = ChronoUnit.DAYS.between(today, t.getDataFine());
            String actionRefId = String.valueOf(t.getId());
            if (daysLeft < 0) {
                String body = "La terapia " + safeName(t) + " e terminata il " + DATE_DISPLAY.format(t.getDataFine()) + ".";
                createOrUpdateNotification(buildNotification(
                        ROLE_PATIENT,
                        patientId,
                        TYPE_THERAPY_STATUS,
                        SEVERITY_CRITICAL,
                        "Terapia terminata",
                        body,
                        ACTION_OPEN_THERAPY,
                        actionRefId
                ));
            } else if (daysLeft == 0) {
                String body = "La terapia " + safeName(t) + " termina oggi.";
                createOrUpdateNotification(buildNotification(
                        ROLE_PATIENT,
                        patientId,
                        TYPE_THERAPY_STATUS,
                        SEVERITY_WARNING,
                        "Terapia in scadenza",
                        body,
                        ACTION_OPEN_THERAPY,
                        actionRefId
                ));
            } else if (daysLeft <= 3) {
                String body = daysLeft == 1
                        ? "La terapia " + safeName(t) + " termina tra 1 giorno."
                        : "La terapia " + safeName(t) + " termina tra " + daysLeft + " giorni.";
                createOrUpdateNotification(buildNotification(
                        ROLE_PATIENT,
                        patientId,
                        TYPE_THERAPY_STATUS,
                        SEVERITY_INFO,
                        "Terapia in scadenza",
                        body,
                        ACTION_OPEN_THERAPY,
                        actionRefId
                ));
            }
        }
    }

    private String safeName(Terapia t) {
        return (t.getNome() == null || t.getNome().isBlank()) ? "in corso" : t.getNome();
    }

    private boolean isTherapyActiveOn(Terapia t, LocalDate day) {
        if (t == null) {
            return false;
        }
        if (t.getDataInizio() != null && day.isBefore(t.getDataInizio())) {
            return false;
        }
        if (t.getDataFine() != null && day.isAfter(t.getDataFine())) {
            return false;
        }
        return true;
    }

    private void createPoorAdherenceNotifications(String doctorId) throws Exception {
        LocalDate today = LocalDate.now();
        List<Paziente> patients = pazienteDAO.findAll();
        for (Paziente p : patients) {
            if (p.getFkDiabetologo() == null || !p.getFkDiabetologo().equalsIgnoreCase(doctorId)) {
                continue;
            }
            boolean poorAdherence = isPoorAdherenceForLastDays(p.getCodiceFiscale(), 3, today);
            String actionRefId = p.getCodiceFiscale();
            if (poorAdherence) {
                String body = "Il paziente " + p.getNome() + " " + p.getCognome()
                        + " non segue la terapia da 3 giorni consecutivi.";
                createOrUpdateNotification(buildNotification(
                        ROLE_DOCTOR,
                        doctorId,
                        TYPE_ADHERENCE_POOR,
                        SEVERITY_WARNING,
                        "Scarsa aderenza alla terapia",
                        body,
                        ACTION_OPEN_THERAPY,
                        actionRefId
                ));
            } else {
                notificationDAO.markReadByKey(ROLE_DOCTOR, doctorId, TYPE_ADHERENCE_POOR, actionRefId);
            }
        }
    }

    private void createDoctorMessageNotifications(String doctorId) throws Exception {
        Map<String, Integer> unreadByPatient = messageDAO.getUnreadByPatient(doctorId);
        List<Notification> existing = notificationDAO.findByTargetAndType(ROLE_DOCTOR, doctorId, TYPE_MESSAGES_UNREAD);
        for (Notification n : existing) {
            String ref = n.getActionRefId();
            if (ref == null || !unreadByPatient.containsKey(ref)) {
                notificationDAO.markRead(n.getId());
            }
        }

        for (Map.Entry<String, Integer> entry : unreadByPatient.entrySet()) {
            int count = entry.getValue();
            if (count <= 0) {
                continue;
            }
            String patientId = entry.getKey();
            Paziente p = pazienteDAO.findById(patientId);
            String name = p != null ? p.getNome() + " " + p.getCognome() : patientId;
            String body = count == 1
                    ? name + ": 1 messaggio non letto."
                    : name + ": " + count + " messaggi non letti.";
            createOrUpdateNotification(buildNotification(
                    ROLE_DOCTOR,
                    doctorId,
                    TYPE_MESSAGES_UNREAD,
                    SEVERITY_INFO,
                    "Messaggi non letti",
                    body,
                    ACTION_OPEN_MESSAGES,
                    patientId
            ));
        }
    }

    private void createPatientMessageNotifications(String patientId) throws Exception {
        Map<String, Integer> unreadByDoctor = messageDAO.getUnreadByDiabetologist(patientId);
        List<Notification> existing = notificationDAO.findByTargetAndType(ROLE_PATIENT, patientId, TYPE_MESSAGES_UNREAD);
        for (Notification n : existing) {
            String ref = n.getActionRefId();
            if (ref == null || !unreadByDoctor.containsKey(ref)) {
                notificationDAO.markRead(n.getId());
            }
        }

        for (Map.Entry<String, Integer> entry : unreadByDoctor.entrySet()) {
            int count = entry.getValue();
            if (count <= 0) {
                continue;
            }
            String doctorId = entry.getKey();
            Diabetologo d = diabetologoDAO.findByEmail(doctorId);
            String name = d != null ? "Dott. " + d.getNome() + " " + d.getCognome() : doctorId;
            String body = count == 1
                    ? "Hai 1 messaggio da " + name + "."
                    : "Hai " + count + " messaggi da " + name + ".";
            createOrUpdateNotification(buildNotification(
                    ROLE_PATIENT,
                    patientId,
                    TYPE_MESSAGES_UNREAD,
                    SEVERITY_INFO,
                    "Messaggi non letti",
                    body,
                    ACTION_OPEN_MESSAGES,
                    doctorId
            ));
        }
    }

    private boolean isPoorAdherenceForLastDays(String patientId, int days, LocalDate today) throws Exception {
        List<Terapia> terapie = terapiaDAO.findByPazienteId(patientId);
        if (terapie.isEmpty()) {
            return false;
        }
        Map<Integer, List<FarmacoTerapia>> farmaciByTerapia = new HashMap<>();
        for (Terapia t : terapie) {
            farmaciByTerapia.put(t.getId(), farmacoTerapiaDAO.findByTerapiaId(t.getId()));
        }

        LocalDate start = today.minusDays(days - 1L);
        List<Assunzione> assunzioniRange = assunzioneDAO.findByPazienteAndDateRange(patientId, start, today);
        Map<String, Integer> assunzioniByDayAndKey = new HashMap<>();
        for (Assunzione a : assunzioniRange) {
            if (a.getDateStamp() == null) {
                continue;
            }
            String dayKey = a.getDateStamp().toLocalDate().toString();
            String key = dayKey + ":" + a.getFkTerapia() + ":" + a.getFkFarmaco();
            assunzioniByDayAndKey.merge(key, a.getQuantitaAssunta(), Integer::sum);
        }

        for (int i = 0; i < days; i++) {
            LocalDate day = today.minusDays(i);
            int expectedDoses = 0;
            int actualDoses = 0;
            for (Terapia t : terapie) {
                if (!isTherapyActiveOn(t, day)) {
                    continue;
                }
                List<FarmacoTerapia> farmaci = farmaciByTerapia.getOrDefault(t.getId(), List.of());
                for (FarmacoTerapia tf : farmaci) {
                    expectedDoses += tf.getAssunzioniGiornaliere();
                    String key = day.toString() + ":" + t.getId() + ":" + tf.getFkFarmaco();
                    int qty = assunzioniByDayAndKey.getOrDefault(key, 0);
                    int perDose = tf.getQuantita();
                    if (perDose > 0) {
                        actualDoses += (qty / perDose);
                    }
                }
            }
            if (expectedDoses == 0) {
                return false;
            }
            if (actualDoses >= expectedDoses) {
                return false;
            }
        }
        return true;
    }

    private void createHighGlucoseNotification(Paziente paziente, Glicemia g) throws Exception {
        int value = g.getValore();
        String moment = g.getParteGiorno();
        Severity severity = highGlucoseSeverity(value, moment);
        if (severity == null) {
            return;
        }
        String title = severity.level.equals(SEVERITY_CRITICAL)
                ? "Glicemia alta critica"
                : "Glicemia alta";
        String time = g.getDateStamp() != null
                ? g.getDateStamp().format(DateTimeFormatter.ofPattern("HH:mm"))
                : "";
        String body = "Glicemia alta: " + paziente.getNome() + " " + paziente.getCognome()
                + " - " + value + " mg/dL" + (time.isBlank() ? "" : " alle " + time) + ".";

        String actionRefId = paziente.getCodiceFiscale() + ":" +
                (g.getDateStamp() != null ? g.getDateStamp().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) : "now");

        createOrUpdateNotification(buildNotification(
                ROLE_DOCTOR,
                paziente.getFkDiabetologo(),
                TYPE_GLUCOSE_HIGH,
                severity.level,
                title,
                body,
                ACTION_OPEN_MEASUREMENTS,
                actionRefId
        ));
    }

    private Severity highGlucoseSeverity(int value, String moment) {
        if (moment != null && moment.equalsIgnoreCase("Mattina")) {
            if (value > 180) {
                return new Severity(SEVERITY_CRITICAL);
            }
            if (value > 130) {
                return new Severity(SEVERITY_WARNING);
            }
            return null;
        }
        if (value > 220) {
            return new Severity(SEVERITY_CRITICAL);
        }
        if (value > 180) {
            return new Severity(SEVERITY_WARNING);
        }
        return null;
    }

    private Notification buildNotification(
            String targetRole,
            String targetUserId,
            String type,
            String severity,
            String title,
            String body,
            String actionType,
            String actionRefId
    ) {
        Notification n = new Notification();
        n.setTargetRole(targetRole);
        n.setTargetUserId(targetUserId);
        n.setType(type);
        n.setSeverity(severity);
        n.setTitle(title);
        n.setBody(body);
        n.setCreatedAt(LocalDateTime.now());
        n.setActionType(actionType);
        n.setActionRefId(actionRefId);
        return n;
    }

    private void createOrUpdateNotification(Notification n) throws Exception {
        Notification existing = notificationDAO.findByKey(
                n.getTargetRole(), n.getTargetUserId(), n.getType(), n.getActionRefId());
        if (existing == null) {
            notificationDAO.insert(n);
            return;
        }
        boolean changed = !Objects.equals(existing.getTitle(), n.getTitle())
                || !Objects.equals(existing.getBody(), n.getBody())
                || !Objects.equals(existing.getSeverity(), n.getSeverity())
                || !Objects.equals(existing.getActionType(), n.getActionType());
        if (!changed) {
            return;
        }
        n.setId(existing.getId());
        n.setReadAt(existing.getReadAt());
        notificationDAO.update(n);
    }

    private static class Severity {
        private final String level;

        private Severity(String level) {
            this.level = level;
        }
    }

    private boolean shouldRunHeavy(Map<String, LocalDateTime> registry, String key, Duration ttl) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime last = registry.get(key);
        if (last != null && Duration.between(last, now).compareTo(ttl) < 0) {
            return false;
        }
        registry.put(key, now);
        return true;
    }
}
