package org.service;

import org.dao.ExerciseDao;
import org.dao.IExerciseDao;
import org.model.Exercise;
import org.model.ExerciseTracking;
import org.model.Patient;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Egzersiz takibi için servis sınıfı.
 */
public class ExerciseService {

    private final IExerciseDao exerciseDao;
    private final PatientService patientService;

    public ExerciseService() {
        this.exerciseDao = new ExerciseDao();
        this.patientService = new PatientService();
    }

}