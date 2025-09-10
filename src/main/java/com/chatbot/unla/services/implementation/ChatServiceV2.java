package com.chatbot.unla.services.implementation;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.chatbot.unla.entities.BaseDeConocimiento;
import com.chatbot.unla.repositories.IBaseDeConocimientoRepository;
import com.chatbot.unla.services.IChatEmbeddingService;
import com.chatbot.unla.services.IChatQuestionNormalizerService;
import com.chatbot.unla.services.IChatQuestionSelectorService;
import com.chatbot.unla.services.IChatServiceV2;

@Service("chatServiceV2")
public class ChatServiceV2 implements IChatServiceV2 {
	
	@Autowired
	private IBaseDeConocimientoRepository baseDeConocimientoRepository;
	
	@Autowired
	private IChatQuestionNormalizerService chatQuestionNormalizerService;
	
	@Autowired
	private IChatEmbeddingService chatEmbeddingService;
	
	@Autowired
	private IChatQuestionSelectorService chatQuestionSelectorService;
	
	public String procesarPregunta(String preguntaUsuario) {
		
		System.out.println("\n\n************** Inicio de procesamiento de pregunta ************");
		System.out.println("-----------> Pregunta Original: " + preguntaUsuario);
		
		String preguntaNormalizada = chatQuestionNormalizerService.ObtenerPreguntaNormalizada(preguntaUsuario);
		
		List<String> preguntasSimilares = new ArrayList<>();
		
		preguntasSimilares = chatEmbeddingService.obtenerPreguntasSimilares(preguntaNormalizada);
		
		if (preguntasSimilares.isEmpty()) {
			return "NINGUNA";
		} else {
			return (chatQuestionSelectorService.obtenerPreguntaSimilar(preguntasSimilares, preguntaUsuario));
		}
	}
	
	public String buscarRespuesta(String preguntaCoincidente) {
        return baseDeConocimientoRepository.findByPreguntaIgnoreCase(preguntaCoincidente)
                       .map(BaseDeConocimiento::getRespuesta)
                       .orElse(null);
    }

}
