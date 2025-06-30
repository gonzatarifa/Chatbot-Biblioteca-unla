package com.chatbot.unla.services.implementation;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.chatbot.unla.services.IChatQuestionSelectorService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service("chatQuestionSelectorService")
public class ChatQuestionSelectorService implements IChatQuestionSelectorService {
	
	private static final String OLLAMA_URL = "http://localhost:11434/api/chat";
    private static final String MODEL = "qwen3:0.6b";

    public String obtenerPreguntaSimilar(List<String> preguntas, String preguntaUsuario) {

    	if (preguntaUsuario == null || preguntaUsuario.trim().length() < 3) {
    	    return "NINGUNA";
    	}

    	String listaPreguntas = preguntas.stream()
    	        .map(p -> "- " + p)
    	        .collect(Collectors.joining("\n"));

    	String systemPrompt = """
    			Eres un sistema de biblioteca. Se te dará una lista de preguntas predefinidas y una pregunta del usuario.

    			Tu única tarea es elegir **exactamente una** de las preguntas predefinidas que represente claramente la intención de la pregunta del usuario.

    			⚠️ IMPORTANTE:
    			- Solo puedes devolver una pregunta que esté en la lista proporcionada.
    			- No puedes escribir texto adicional, explicaciones ni emojis.
    			- No repitas la pregunta del usuario.
    			- No inventes frases nuevas.

    			✅ Si alguna pregunta de la lista representa lo que el usuario quiere, devuélvela **exactamente como está en la lista**, sin modificarla.

    			❌ Si ninguna representa claramente lo que el usuario quiere, responde solo con: NINGUNA

    			Lista de preguntas predefinidas:
    			""" + listaPreguntas + "\n\nPregunta del usuario:\n" + preguntaUsuario;

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

            String rawResponse = json.get("message").get("content").asText().trim();

            String cleanedResponse = rawResponse
            	    .replaceAll("(?s)<think>.*?</think>", "")
            	    .replaceAll("^[^\\p{L}\\p{N}¿]+", "")
            	    .trim();

            System.out.println("-----------> Pregunta elegida por IA: " + cleanedResponse);

            if (!preguntas.contains(cleanedResponse) && !cleanedResponse.equals("NINGUNA")) {
                return "NINGUNA";
            }

            return cleanedResponse;

        } catch (Exception e) {
            return "ERROR: " + e.getMessage();
        }
    }
}
