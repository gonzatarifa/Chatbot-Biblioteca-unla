package com.chatbot.unla.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.chatbot.unla.entities.PreguntaUsuario;
import com.chatbot.unla.entities.Usuario;

@Service
public interface IPreguntaUsuarioService {
	
	//public List<PreguntaUsuario> getAll();

	//public Usuario buscar(long id);

	//public void eliminar(long id);
	
	public void save(PreguntaUsuario preguntaUsuario);
	
}
