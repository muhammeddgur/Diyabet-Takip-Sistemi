package org.service;

import org.dao.DietDao;
import org.dao.IDietDao;
import org.model.Diet;
import org.model.DietTracking;
import org.model.Patient;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Diyet takibi için servis sınıfı.
 */
public class DietService {

    private final IDietDao dietDao;
    private final PatientService patientService;

    public DietService() {
        this.dietDao = new DietDao();
        this.patientService = new PatientService();
    }

}