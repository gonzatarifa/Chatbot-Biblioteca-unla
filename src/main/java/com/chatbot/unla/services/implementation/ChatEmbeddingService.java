package com.chatbot.unla.services.implementation;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.chatbot.unla.entities.BaseDeConocimiento;
import com.chatbot.unla.repositories.IBaseDeConocimientoRepository;
import com.chatbot.unla.services.IChatEmbeddingService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service("chatEmbeddingService")
public class ChatEmbeddingService implements IChatEmbeddingService {
	
	@Autowired
	private IBaseDeConocimientoRepository baseDeConocimientoRepository;
	
	private static final String OLLAMA_URL = "http://localhost:11434/api/embeddings";
    private static final String MODEL = "nomic-embed-text";

	public List<String> obtenerPreguntasSimilares(String preguntaUsuario) {
	    try {
	        ObjectMapper mapper = new ObjectMapper();
	        HttpClient client = HttpClient.newHttpClient();

	        // Peticion HTTP para obtener embedding de pregunta del usuario
	        String userEmbeddingBody = mapper.writeValueAsString(Map.of(
	            "model", MODEL,
	            "prompt", preguntaUsuario
	        ));

	        HttpRequest userEmbeddingRequest = HttpRequest.newBuilder()
	                .uri(URI.create(OLLAMA_URL))
	                .header("Content-Type", "application/json")
	                .POST(HttpRequest.BodyPublishers.ofString(userEmbeddingBody))
	                .build();

	        HttpResponse<String> userResponse = client.send(userEmbeddingRequest, HttpResponse.BodyHandlers.ofString());
	        JsonNode userEmbeddingJson = mapper.readTree(userResponse.body());
	        List<Double> userVector = mapper.convertValue(
	            userEmbeddingJson.get("embedding"),
	            mapper.getTypeFactory().constructCollectionType(List.class, Double.class)
	        );
	        
	        List<BaseDeConocimiento> items = baseDeConocimientoRepository.findAllHabilitados();

	        double umbral = 0.80;

	        List<PreguntaSimilitud> candidatas = new ArrayList<>();

	        // Bucle para recorrer y agarrar las preguntas que mas se parecen segun embedding (maximo 5)
	        for (BaseDeConocimiento item : items) {

	        	List<Double> vectorPregunta = mapper.readValue(item.getEmbedding(),
	        	        mapper.getTypeFactory().constructCollectionType(List.class, Double.class));

	        	double similitud = calcularSimilitudCoseno(userVector, vectorPregunta);

	            if (similitud >= umbral) {
	                candidatas.add(new PreguntaSimilitud(item.getPregunta(), similitud));
	            }
	        }
	        System.out.println("-----------Lista de preguntas segun embeddings-------------");
	        System.out.println(candidatas.stream()
	                .sorted((a, b) -> Double.compare(b.similitud, a.similitud))
	                .limit(5)
	                .map(p -> p.pregunta)
	                .collect(Collectors.toList()));
	        System.out.println("---------Fin de Lista de preguntas segun embeddings--------");
	        
	        return candidatas.stream()
	                .sorted((a, b) -> Double.compare(b.similitud, a.similitud))
	                .limit(5)
	                .map(p -> p.pregunta)
	                .collect(Collectors.toList());

	    } catch (Exception e) {
	        e.printStackTrace();
	        return List.of("ERROR: " + e.getMessage());
	    }
	}
	
	private double calcularSimilitudCoseno(List<Double> v1, List<Double> v2) {
	    double dot = 0.0, normA = 0.0, normB = 0.0;
	    for (int i = 0; i < v1.size(); i++) {
	        dot += v1.get(i) * v2.get(i);
	        normA += Math.pow(v1.get(i), 2);
	        normB += Math.pow(v2.get(i), 2);
	    }
	    return dot / (Math.sqrt(normA) * Math.sqrt(normB));
	}
    
    private static class PreguntaSimilitud {
        String pregunta;
        double similitud;

        PreguntaSimilitud(String pregunta, double similitud) {
            this.pregunta = pregunta;
            this.similitud = similitud;
        }
    }
    
}
