package com.chatbot.unla.controllers;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.chatbot.unla.services.IChatServiceV2;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

	@Autowired
    private IChatServiceV2 chatService;

	@PostMapping
    public String responder(@RequestBody String preguntaUsuario) {
        String preguntaCoincidente = chatService.procesarPregunta(preguntaUsuario);

        if (preguntaCoincidente.equalsIgnoreCase("NINGUNA")) {
            return "🤐 Pregunta no reconocida. No se responderá nada.";
        }

        String respuesta = chatService.buscarRespuesta(preguntaCoincidente);
        if (respuesta != null) {
            return "🤖 " + respuesta;
        } else {
            return "❌ Pregunta no reconocida: " + preguntaCoincidente;
        }
    }
}
