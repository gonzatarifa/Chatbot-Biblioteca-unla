package com.chatbot.unla.services.implementation;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.springframework.stereotype.Service;

import com.chatbot.unla.services.IChatQuestionNormalizerService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service("chatQuestionNormalizerService")
public class ChatQuestionNormalizerService implements IChatQuestionNormalizerService {
	
	private static final String OLLAMA_URL = "http://localhost:11434/api/chat";
    private static final String MODEL = "qwen3:0.6b";
	
	public String ObtenerPreguntaNormalizada(String preguntaUsuario) {

		String systemPrompt = """
				Normalizá esta pregunta hecha en español para una biblioteca.

				Agregá tildes, signos de pregunta y corregí errores comunes de escritura.No corrijas nombres de libros y no cambies nombres propios ni traduzcas nada.

				Respondé con la pregunta normalizada, sin comillas ni explicaciones.
				
				Pregunta: """ + preguntaUsuario;
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
            System.out.println("-----------> Pregunta normalizada: " + cleanedResponse);
            
            return cleanedResponse;

        } catch (Exception e) {
            return "ERROR: " + e.getMessage();
        }
    }
}
