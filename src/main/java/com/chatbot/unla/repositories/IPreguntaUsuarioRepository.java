package com.chatbot.unla.repositories;

import java.io.Serializable;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.chatbot.unla.entities.PreguntaUsuario;

@Repository("preguntaUsuarioRepository")
public interface IPreguntaUsuarioRepository extends JpaRepository<PreguntaUsuario, Serializable>{
	
	List<PreguntaUsuario> findByUsuarioRespondiendoId(Long usuarioId);

}
