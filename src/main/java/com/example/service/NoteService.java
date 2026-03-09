package com.example.service;

import com.example.dao.NoteDAO;
import com.example.dao.ParametreDAO;
import com.example.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NoteService {

    @Autowired
    private NoteDAO noteDAO;

    @Autowired
    private ParametreDAO parametreDAO;

    public Double calculerNoteFinale(Candidat candidat, Matiere matiere) {
        List<Note> notes = noteDAO.findByCandidatAndMatiere(candidat, matiere);
        if (notes.isEmpty()) return 0.0;

        List<Double> valeurs = notes.stream()
                .map(Note::getNote)
                .collect(Collectors.toList());

        
        double sommeDiff = 0.0;
        for (int i = 0; i < valeurs.size(); i++) {
            for (int j = i + 1; j < valeurs.size(); j++) {
                sommeDiff += Math.abs(valeurs.get(i) - valeurs.get(j));
            }
        }

        
        List<Parametre> params = parametreDAO.findAllByMatiere(matiere);

        for (Parametre param : params) {
            double seuil = param.getSeuil().doubleValue();
            String operateur = param.getOperateur().getNom();

            boolean conditionMet = switch (operateur) {
                case "<"  -> sommeDiff < seuil;
                case ">"  -> sommeDiff > seuil;
                case "<=" -> sommeDiff <= seuil;
                case ">=" -> sommeDiff >= seuil;
                default   -> false;
            };

            if (conditionMet) {
                String nomResolution = param.getResolution().getNom().toLowerCase();

                if (nomResolution.contains("plus petit")) {
                    return Collections.min(valeurs);
                } else if (nomResolution.contains("plus grand")) {
                    return Collections.max(valeurs);
                } else if (nomResolution.contains("moyenne")) {
                    return calculMoyenne(valeurs);
                }
            
            }
        }

        
        return calculMoyenne(valeurs);
    }

    private Double calculMoyenne(List<Double> valeurs) {
        return valeurs.stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);
    }
}