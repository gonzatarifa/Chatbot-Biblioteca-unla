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

    public String obtenerRespuestaSimilar(List<String> preguntas, String preguntaUsuario) {

    	String listaPreguntas = preguntas.stream()
    	        .map(p -> "- " + p)
    	        .collect(Collectors.joining("\n"));

        String systemPrompt = """
                Eres un sistema de una biblioteca que recibe una lista de preguntas predefinidas y una pregunta del usuario.

                Tu tarea es entender la intencion de la pregunta y devolver **únicamente** la pregunta predefinida que mejor coincida con la pregunta del usuario, sin agregar nada más, ni explicaciones, ni comillas.
                                
        		En caso de que ninguna coincida o muchas preguntas puedan ser la correcta, responde solo la palabra: NINGUNA
                
                En el caso de encontrar la pregunta, enviar la pregunta sin modificarla de ninguna manera, como por ejemplo, respetando los signos de pregunta, sin incluir ningun tipo de razonamiento.

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
            //System.out.println("Respuesta con <think>: " + rawResponse);
            String cleanedResponse = rawResponse.replaceAll("(?s)<think>.*?</think>", "").trim();
            System.out.println("-----------> Pregunta elegida por IA: " + cleanedResponse);
            
            return cleanedResponse;

        } catch (Exception e) {
            return "ERROR: " + e.getMessage();
        }
    }
}
