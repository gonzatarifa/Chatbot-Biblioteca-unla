package com.chatbot.unla.repositories;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.chatbot.unla.entities.BaseDeConocimiento;

@Repository("baseDeConocimientoRepository")
public interface IBaseDeConocimientoRepository extends JpaRepository<BaseDeConocimiento, Serializable>{

	Optional<BaseDeConocimiento> findByPreguntaIgnoreCase(String pregunta);
	
	@Query("SELECT b.pregunta FROM BaseDeConocimiento b")
	List<String> findAllPreguntas();
	
	@Query("SELECT b.pregunta FROM BaseDeConocimiento b WHERE b.habilitado = true")
	List<String> findAllPreguntasHabilitadas(); 
	
	@Query("SELECT b FROM BaseDeConocimiento b WHERE b.habilitado = true")
	List<BaseDeConocimiento> findAllHabilitados();
	
	@Query("SELECT b FROM BaseDeConocimiento b WHERE b.habilitado = false")
	List<BaseDeConocimiento> findAllDeshabilitados();
	
	@Query("SELECT b FROM BaseDeConocimiento b " +
		       "WHERE b.habilitado = true AND " +
		       "(LOWER(b.pregunta) LIKE LOWER(CONCAT('%', :texto, '%')) " +
		       "OR LOWER(b.respuesta) LIKE LOWER(CONCAT('%', :texto, '%')))")
	List<BaseDeConocimiento> buscarPorTexto(@Param("texto") String texto);

}
