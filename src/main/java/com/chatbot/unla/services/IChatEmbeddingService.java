package com.chatbot.unla.services;

import java.util.List;

import org.springframework.stereotype.Service;

@Service
public interface IChatEmbeddingService {
	
	public List<String> obtenerPreguntasSimilares(String preguntaUsuario);

}
