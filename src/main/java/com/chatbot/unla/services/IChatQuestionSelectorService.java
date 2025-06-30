package com.chatbot.unla.services;

import java.util.List;

import org.springframework.stereotype.Service;

@Service
public interface IChatQuestionSelectorService {
	
	public String obtenerPreguntaSimilar(List<String> preguntas, String preguntaUsuario);

}
