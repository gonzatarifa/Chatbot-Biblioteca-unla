package com.chatbot.unla.services;

import org.springframework.stereotype.Service;

@Service
public interface IChatService {

	public String obtenerRespuestaSimilar(String preguntaUsuario);
	
	public String buscarRespuesta(String preguntaCoincidente);
	
}
