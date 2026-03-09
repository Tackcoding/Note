package com.example.dao;

import com.example.model.Matiere;
import com.example.model.Parametre;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ParametreDAO extends JpaRepository<Parametre, Long> {
    Optional<Parametre> findByMatiere(Matiere matiere);
    List<Parametre> findAllByMatiere(Matiere matiere);
}