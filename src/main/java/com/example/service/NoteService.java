package com.example.service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.dao.NoteDAO;
import com.example.dao.ParametreDAO;
import com.example.model.Candidat;
import com.example.model.Matiere;
import com.example.model.Note;
import com.example.model.Parametre;

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

        final double sommeDiff;
        double temp = 0.0;
        for (int i = 0; i < valeurs.size(); i++) {
            for (int j = i + 1; j < valeurs.size(); j++) {
                temp += Math.abs(valeurs.get(i) - valeurs.get(j));
            }
        }
        sommeDiff = temp;

        List<Parametre> params = parametreDAO.findAllByMatiere(matiere);

        
        List<Parametre> paramsValides = params.stream()
                .filter(param -> {
                    double seuil = param.getSeuil().doubleValue();
                    String operateur = param.getOperateur().getNom();
                    return switch (operateur) {
                        case "<"  -> sommeDiff < seuil;
                        case ">"  -> sommeDiff > seuil;
                        case "<=" -> sommeDiff <= seuil;
                        case ">=" -> sommeDiff >= seuil;
                        default   -> false;
                    };
                })
                .collect(Collectors.toList());

        if (paramsValides.isEmpty()) return calculMoyenne(valeurs);


        Parametre meilleurParam = paramsValides.stream()
                .min((p1, p2) -> {
                    double ecart1 = Math.abs(p1.getSeuil().doubleValue() - sommeDiff);
                    double ecart2 = Math.abs(p2.getSeuil().doubleValue() - sommeDiff);

                    int cmp = Double.compare(ecart1, ecart2);
                    if (cmp != 0) return cmp; 

                    return Double.compare(
                        p1.getSeuil().doubleValue(),
                        p2.getSeuil().doubleValue()
                    );
                })
                .orElse(null);

        if (meilleurParam == null) return calculMoyenne(valeurs);

        String nomResolution = meilleurParam.getResolution().getNom().toLowerCase();

        if (nomResolution.contains("plus petit")) {
            return Collections.min(valeurs);
        } else if (nomResolution.contains("plus grand")) {
            return Collections.max(valeurs);
        } else if (nomResolution.contains("moyenne")) {
            return calculMoyenne(valeurs);
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