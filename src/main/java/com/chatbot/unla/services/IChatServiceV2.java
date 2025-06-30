package com.chatbot.unla.services;

import org.springframework.stereotype.Service;

@Service
public interface IChatServiceV2 {

	public String procesarPregunta(String preguntaUsuario);
	
	public String buscarRespuesta(String preguntaCoincidente);
	
}
