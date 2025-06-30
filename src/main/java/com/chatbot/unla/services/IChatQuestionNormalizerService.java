package com.chatbot.unla.services;

import org.springframework.stereotype.Service;

@Service
public interface IChatQuestionNormalizerService {
	
	public String ObtenerPreguntaNormalizada(String preguntaUsuario);

}
