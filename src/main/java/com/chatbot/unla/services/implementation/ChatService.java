package com.chatbot.unla.services.implementation;

import com.chatbot.unla.entities.BaseDeConocimiento;
import com.chatbot.unla.repositories.IBaseDeConocimientoRepository;
import com.chatbot.unla.services.IChatService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;
import java.net.URI;
import java.net.http.*;
import java.util.List;
import java.util.stream.Collectors;

@Service("chatService")
public class ChatService implements IChatService {
	
	@Autowired
	private IBaseDeConocimientoRepository baseDeConocimientoRepository;
	
	private static final String OLLAMA_URL = "http://localhost:11434/api/chat";
    private static final String MODEL = "deepseek-r1";

    public String obtenerRespuestaSimilar(String preguntaUsuario) {
    	
    	List<String> preguntas = baseDeConocimientoRepository.findAllPreguntas();

    	String listaPreguntas = preguntas.stream()
    	        .map(p -> "- " + p)
    	        .collect(Collectors.joining("\n"));

        String systemPrompt = """
                Eres un sistema que recibe una lista de preguntas predefinidas y una pregunta del usuario.

                Tu tarea es devolver **únicamente** la pregunta predefinida que mejor coincida con la pregunta del usuario, sin agregar nada más, ni explicaciones, ni comillas.

                Si ninguna coincide, responde solo la palabra: NINGUNA
                
                Responde directamente sin incluir <think> o razonamientos internos, en caso de encontrar la pregunta responder esa pregunta tal cual esta en la siguiente lista:

                Lista de preguntas predefinidas:
                """ + listaPreguntas + "\nPregunta del usuario:\n" + preguntaUsuario;

        try {
            ObjectMapper mapper = new ObjectMapper();

            String body = mapper.writeValueAsString(mapper.createObjectNode()
                    .put("model", MODEL)
                    .put("stream", false)
                    .set("messages", mapper.createArrayNode()
                        .add(mapper.createObjectNode().put("role", "system").put("content", systemPrompt))
                        .add(mapper.createObjectNode().put("role", "user").put("content", preguntaUsuario)))
            );

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(OLLAMA_URL))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            JsonNode json = mapper.readTree(response.body());
            
            // Esto se hace para remover la parte de <think> que tiene deepseek y algunos otras IA's
            String rawResponse = json.get("message").get("content").asText().trim();
            String cleanedResponse = rawResponse.replaceAll("(?s)<think>.*?</think>", "").trim();
            
            return cleanedResponse;

        } catch (Exception e) {
            return "ERROR: " + e.getMessage();
        }
    }
    
    public String buscarRespuesta(String preguntaCoincidente) {
        return baseDeConocimientoRepository.findByPreguntaIgnoreCase(preguntaCoincidente)
                       .map(BaseDeConocimiento::getRespuesta)
                       .orElse(null);
    }
}
