package com.chatbot.unla.services;

import java.util.List;

import org.springframework.stereotype.Service;

@Service
public interface IChatQuestionSelectorService {
	
	public String obtenerRespuestaSimilar(List<String> preguntas, String preguntaUsuario);

}
