package com.chatbot.unla.services.implementation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.chatbot.unla.entities.PreguntaUsuario;
import com.chatbot.unla.repositories.IPreguntaUsuarioRepository;
import com.chatbot.unla.services.IPreguntaUsuarioService;


@Service("preguntaUsuarioService")
public class PreguntaUsuarioService implements IPreguntaUsuarioService {
	
	@Autowired
	@Qualifier("preguntaUsuarioRepository")
	private IPreguntaUsuarioRepository preguntaUsuarioRepository;
	
	@Override
	public void save(PreguntaUsuario preguntaUsuario) {
		preguntaUsuarioRepository.save(preguntaUsuario);	
	}
}
