package org.service;

import org.dao.SymptomDao;
import org.model.PatientSymptom;
import org.model.Symptom;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SymptomService {

    private final SymptomDao symptomDao;

    public SymptomService() {
        this.symptomDao = new SymptomDao();
    }

    /**
     * Tüm belirti türlerini getirir
     * @return Tüm belirti türleri
     */
    public List<Symptom> getAllSymptoms() {
        try {
            return symptomDao.findAll();
        } catch (SQLException e) {
            System.err.println("Belirtiler alınırken hata: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * Belirti adına göre belirti nesnesini getirir
     * @param symptomName Belirti adı
     * @return Belirti nesnesi veya null
     */
    public Symptom getSymptomByName(String symptomName) {
        try {
            return symptomDao.findByName(symptomName);
        } catch (SQLException e) {
            System.err.println("Belirti adına göre arama yapılırken hata: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Hastanın belirtilerini getirir
     * @param patientId Hasta ID
     * @return Hasta belirtileri listesi
     */
    public List<PatientSymptom> getPatientSymptoms(Integer patientId) {
        try {
            return symptomDao.getPatientSymptomsByPatientId(patientId);
        } catch (SQLException e) {
            System.err.println("Hasta belirtileri alınırken hata: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * Hasta belirtilerini detaylı olarak getirir (semptom adı, açıklama vb. bilgiler dahil)
     * @param patientId Hasta ID
     * @return Detaylı belirti bilgileri listesi
     */
    public List<Map<String, Object>> getPatientSymptomDetails(Integer patientId) {
        try {
            return symptomDao.getPatientSymptomDetails(patientId);
        } catch (SQLException e) {
            System.err.println("Hasta belirti detayları alınırken hata: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * Belirli bir belirtiye sahip tüm hastaların ID'lerini getirir
     * @param symptomName Belirti adı
     * @return Hasta ID'leri listesi
     */
    public List<Integer> findPatientIdsBySymptomName(String symptomName) {
        try {
            return symptomDao.findPatientIdsBySymptomName(symptomName);
        } catch (SQLException e) {
            System.err.println("Belirtiye göre hasta ID'leri alınırken hata: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * Belirti adına göre belirtiyi bulur veya yoksa yeni oluşturur
     * @param symptomName Belirti adı
     * @param description Açıklama
     * @return Belirti nesnesi
     */
    public Symptom createOrGetSymptom(String symptomName, String description) {
        try {
            Symptom symptom = symptomDao.findByName(symptomName);
            if (symptom == null) {
                symptom = new Symptom();
                symptom.setSymptom_adi(symptomName);
                symptom.setAciklama(description);
                symptomDao.save(symptom);
            }
            return symptom;
        } catch (SQLException e) {
            System.err.println("Belirti oluşturulurken hata: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Belirtiyi hastaya ekler
     * @param patientId Hasta ID
     * @param symptomId Belirti ID
     * @param reportDate Belirtilme tarihi
     * @return İşlem başarılı ise true
     */
    public boolean addSymptomToPatient(Integer patientId, Integer symptomId, LocalDate reportDate) {
        try {
            PatientSymptom patientSymptom = new PatientSymptom();
            patientSymptom.setPatient_id(patientId);
            patientSymptom.setSymptom_id(symptomId);
            patientSymptom.setBelirtilme_tarihi(reportDate != null ? reportDate : LocalDate.now());

            return symptomDao.savePatientSymptom(patientSymptom);
        } catch (SQLException e) {
            System.err.println("Hasta belirtisi eklenirken hata: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Hasta belirtisi ekler
     * @param patientSymptom Hasta belirti nesnesi
     * @return İşlem başarılı ise true
     */
    public boolean savePatientSymptom(PatientSymptom patientSymptom) throws SQLException {
        try {
            return symptomDao.savePatientSymptom(patientSymptom);
        } catch (SQLException e) {
            System.err.println("Hasta belirtisi eklenirken hata: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Belirti adına göre hastaya belirti ekler
     * @param patientId Hasta ID
     * @param symptomName Belirti adı
     * @param description Açıklama
     * @param reportDate Belirtilme tarihi
     * @return İşlem başarılı ise true
     */
    public boolean addSymptomToPatientByName(Integer patientId, String symptomName,
                                             String description, LocalDate reportDate) {
        try {
            // Önce belirti adına göre belirti nesnesini bul veya oluştur
            Symptom symptom = createOrGetSymptom(symptomName, description);
            if (symptom == null) {
                return false;
            }

            // Hastaya bu belirtiyi ekle
            return addSymptomToPatient(patientId, symptom.getSymptom_id(), reportDate);
        } catch (Exception e) {
            System.err.println("Hasta belirtisi eklenirken hata: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Hasta belirtisini günceller
     * @param patientSymptomId Hasta belirti ID
     * @param symptomId Yeni belirti ID
     * @param reportDate Yeni belirtilme tarihi
     * @return İşlem başarılı ise true
     */
    public boolean updatePatientSymptom(Integer patientSymptomId, Integer symptomId, LocalDate reportDate) {
        try {
            // Önce mevcut belirtiyi bul
            PatientSymptom patientSymptom = null;

            // Hasta belirtilerini getir ve ID'ye göre ara
            for (PatientSymptom ps : symptomDao.getPatientSymptomsByPatientId(null)) {
                if (ps.getPatient_symptom_id().equals(patientSymptomId)) {
                    patientSymptom = ps;
                    break;
                }
            }

            if (patientSymptom == null) {
                return false;
            }

            // Belirti ID ve tarihi güncelle
            patientSymptom.setSymptom_id(symptomId);
            if (reportDate != null) {
                patientSymptom.setBelirtilme_tarihi(reportDate);
            }

            // Kaydı güncelle
            return symptomDao.savePatientSymptom(patientSymptom);
        } catch (SQLException e) {
            System.err.println("Hasta belirtisi güncellenirken hata: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Hasta belirtisini siler
     * @param patientSymptomId Hasta belirti ID
     * @return İşlem başarılı ise true
     */
    public boolean deletePatientSymptom(Integer patientSymptomId) {
        try {
            return symptomDao.deletePatientSymptom(patientSymptomId);
        } catch (SQLException e) {
            System.err.println("Hasta belirtisi silinirken hata: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}