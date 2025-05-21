package org.service;

import org.dao.PatientSymptomDao;
import org.dao.SymptomDao;
import org.model.PatientSymptom;
import org.model.Symptom;
import org.util.DateTimeUtil;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Belirti yönetimi için servis sınıfı
 */
public class SymptomService {

    private final SymptomDao symptomDao;
    private final PatientSymptomDao patientSymptomDao;

    public SymptomService() {
        this.symptomDao = new SymptomDao();
        this.patientSymptomDao = new PatientSymptomDao();
    }

    /**
     * Yeni belirti ekler veya mevcut belirti adını kullanır
     * @param symptomName Belirti adı
     * @param description Açıklama
     * @return Belirti nesnesi
     */
    public Symptom createOrGetSymptom(String symptomName, String description) {
        try {
            // Belirti adına göre ara
            Symptom existingSymptom = symptomDao.findByName(symptomName);

            // Varsa mevcut belirtiyi döndür
            if (existingSymptom != null) {
                return existingSymptom;
            }

            // Yoksa yeni belirti oluştur
            Symptom newSymptom = new Symptom(symptomName, description);
            boolean saved = symptomDao.save(newSymptom);

            if (saved) {
                return newSymptom;
            } else {
                throw new RuntimeException("Belirti kaydedilemedi");
            }
        } catch (SQLException e) {
            System.err.println("Belirti işlemleri sırasında hata: " + e.getMessage());
            throw new RuntimeException("Veritabanı hatası: " + e.getMessage());
        }
    }

    /**
     * Hastaya belirti ekler
     * @param patientId Hasta ID
     * @param symptomId Belirti ID
     * @param reportDate Bildirim tarihi
     * @return İşlem başarılı ise true, değilse false
     */
    public boolean addSymptomToPatient(Integer patientId, Integer symptomId, LocalDate reportDate) {
        try {
            // Tarih null ise güncel tarihi kullan
            LocalDate date = (reportDate != null) ? reportDate : DateTimeUtil.getCurrentDate();

            PatientSymptom patientSymptom = new PatientSymptom(patientId, symptomId, date);
            return patientSymptomDao.save(patientSymptom);
        } catch (SQLException e) {
            System.err.println("Hastaya belirti eklenirken hata: " + e.getMessage());
            return false;
        }
    }

    /**
     * Hastaya belirti ekler (adı ile)
     * @param patientId Hasta ID
     * @param symptomName Belirti adı
     * @param description Açıklama
     * @param reportDate Bildirim tarihi
     * @return İşlem başarılı ise true, değilse false
     */
    public boolean addSymptomToPatientByName(Integer patientId, String symptomName,
                                             String description, LocalDate reportDate) {
        try {
            // Belirti oluştur veya mevcut olanı al
            Symptom symptom = createOrGetSymptom(symptomName, description);

            // Tarih kontrolü
            LocalDate date = (reportDate != null) ? reportDate : DateTimeUtil.getCurrentDate();

            // Hastaya belirtiyi ekle
            return addSymptomToPatient(patientId, symptom.getSymptom_id(), date);
        } catch (Exception e) {
            System.err.println("Hastaya belirti eklenirken hata: " + e.getMessage());
            return false;
        }
    }

    /**
     * Hastanın belirtilerini getirir
     * @param patientId Hasta ID
     * @return Hasta belirtilerinin listesi
     */
    public List<Map<String, Object>> getPatientSymptomDetails(Integer patientId) {
        try {
            List<PatientSymptom> patientSymptoms = patientSymptomDao.findByPatientId(patientId);
            List<Map<String, Object>> result = new ArrayList<>();

            for (PatientSymptom ps : patientSymptoms) {
                Map<String, Object> symptomDetail = new HashMap<>();

                // Semptom bilgilerini al
                Symptom symptom = symptomDao.findById(ps.getSymptom_id());
                if (symptom != null) {
                    symptomDetail.put("symptomId", symptom.getSymptom_id());
                    symptomDetail.put("symptomName", symptom.getSymptom_adi());
                    symptomDetail.put("description", symptom.getAciklama());
                    symptomDetail.put("reportDate", ps.getBelirtilme_tarihi());
                    symptomDetail.put("patientSymptomId", ps.getPatient_symptom_id());

                    result.add(symptomDetail);
                }
            }

            return result;
        } catch (SQLException e) {
            System.err.println("Hasta belirtileri getirilirken hata: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Hasta belirtisini siler
     * @param patientSymptomId Hasta belirti ID
     * @return İşlem başarılı ise true, değilse false
     */
    public boolean deletePatientSymptom(Integer patientSymptomId) {
        try {
            return patientSymptomDao.delete(patientSymptomId);
        } catch (SQLException e) {
            System.err.println("Hasta belirtisi silinirken hata: " + e.getMessage());
            return false;
        }
    }

    /**
     * Hasta belirtisini günceller
     * @param patientSymptomId Hasta belirti ID
     * @param symptomId Yeni belirti ID
     * @param reportDate Yeni bildirim tarihi
     * @return İşlem başarılı ise true, değilse false
     */
    public boolean updatePatientSymptom(Integer patientSymptomId, Integer symptomId, LocalDate reportDate) {
        try {
            PatientSymptom patientSymptom = patientSymptomDao.findById(patientSymptomId);
            if (patientSymptom != null) {
                patientSymptom.setSymptom_id(symptomId);

                // Tarih kontrolü
                if (reportDate != null) {
                    patientSymptom.setBelirtilme_tarihi(reportDate);
                } else {
                    patientSymptom.setBelirtilme_tarihi(DateTimeUtil.getCurrentDate());
                }

                return patientSymptomDao.save(patientSymptom);
            }
            return false;
        } catch (SQLException e) {
            System.err.println("Hasta belirtisi güncellenirken hata: " + e.getMessage());
            return false;
        }
    }

    /**
     * Tüm belirtileri getirir
     * @return Belirtilerin listesi
     */
    public List<Symptom> getAllSymptoms() {
        try {
            return symptomDao.findAll();
        } catch (SQLException e) {
            System.err.println("Belirtiler getirilirken hata: " + e.getMessage());
            return new ArrayList<>();
        }
    }
}