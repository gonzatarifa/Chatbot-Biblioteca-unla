package com.chatbot.unla.repositories;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.chatbot.unla.entities.BaseDeConocimiento;

@Repository("baseDeConocimientoRepository")
public interface IBaseDeConocimientoRepository extends JpaRepository<BaseDeConocimiento, Serializable>{

	Optional<BaseDeConocimiento> findByPreguntaIgnoreCase(String pregunta);
	
	@Query("SELECT b.pregunta FROM BaseDeConocimiento b")
	List<String> findAllPreguntas();
	
}
