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
                Eres un sistema que va a recibir una pregunta que un usuario realizara a una biblioteca.
                
                Tu funcion es normalizar esta pregunta para que posteriormente sea procesada por medio de embeddings.
                
                Tene en cuenta que te van a hablar en español siempre, por que cosas como "hs" se puede traducir a horas.
                
                Tambien tene en cuenta que te pueden preguntar especificamente sobre nombres de libros y pueden no ponerle mayuscula, por lo que tener cuidado a la hora de borrar.
                
                **Unicamente** responder con la pregunta normalizada en español, sin agregar nada más, ni explicaciones, ni comillas.
                
                \nPregunta del usuario:""" + preguntaUsuario;

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
