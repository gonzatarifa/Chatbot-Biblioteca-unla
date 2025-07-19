package com.chatbot.unla.controllers;

import com.chatbot.unla.entities.BaseDeConocimiento;
import com.chatbot.unla.entities.PreguntaUsuario;
import com.chatbot.unla.helpers.ViewRouteHelper;
import com.chatbot.unla.services.IPreguntaUsuarioService;
import com.chatbot.unla.services.implementation.BaseDeConocimientoService;
import com.chatbot.unla.services.implementation.EmailService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/preguntas")
public class PreguntaUsuarioController {

    @Autowired
    @Qualifier("preguntaUsuarioService")
    private IPreguntaUsuarioService preguntaUsuarioService;
    
    @Autowired
    @Qualifier("baseDeConocimientoService")
    private BaseDeConocimientoService baseDeConocimientoService;
    
    @Autowired
    private EmailService emailService;


    @GetMapping("/lista")
    public String listarPreguntas(Model model) {
        List<PreguntaUsuario> preguntas = preguntaUsuarioService.getAll().stream()
                .filter(PreguntaUsuario::isHabilitado)
                .collect(Collectors.toList());

        model.addAttribute("titulo", "Preguntas recibidas");
        model.addAttribute("lista", preguntas);
        return ViewRouteHelper.PREGUNTA_LISTA;
    }

    @GetMapping("lista/delete/{id}")
    public String eliminar(@PathVariable("id") long id, RedirectAttributes attribute) {
        PreguntaUsuario pregunta = preguntaUsuarioService.buscar(id);
        if (pregunta != null) {
            pregunta.setHabilitado(false);
            preguntaUsuarioService.save(pregunta);
            attribute.addFlashAttribute("warning", "Pregunta deshabilitada correctamente");
        } else {
            attribute.addFlashAttribute("error", "Pregunta no encontrada");
        }
        return ViewRouteHelper.PREGUNTA_REDIRECT_LISTA;
    }
    
    @GetMapping("/responder/{id}")
    public String mostrarFormularioRespuesta(@PathVariable("id") long id, Model model, RedirectAttributes attr) {
        PreguntaUsuario pregunta = preguntaUsuarioService.buscar(id);
        if (pregunta == null || !pregunta.isHabilitado()) {
            attr.addFlashAttribute("error", "Pregunta no encontrada");
            return "redirect:/preguntas/lista";
        }
        model.addAttribute("pregunta", pregunta);
        return "pregunta/responder"; 
    }

    // Procesar respuesta
    @PostMapping("/responder")
    public String procesarRespuesta(
            @RequestParam("id") long id,
            @RequestParam("respuesta") String respuesta,
            @RequestParam(name = "saludo", required = false) String saludo,
            @RequestParam(name = "firma", required = false) String firma,
            RedirectAttributes attr) {

        PreguntaUsuario pregunta = preguntaUsuarioService.buscar(id);
        if (pregunta == null || !pregunta.isHabilitado()) {
            attr.addFlashAttribute("error", "Pregunta no encontrada o deshabilitada");
            return "redirect:/preguntas/lista";
        }

        pregunta.setRespuestaEnviada(true);
        pregunta.setFechaEnvioRespuesta(LocalDateTime.now());
        preguntaUsuarioService.save(pregunta);

        String embedding = generarEmbedding(pregunta.getPregunta());
        BaseDeConocimiento base = new BaseDeConocimiento(pregunta.getPregunta(), respuesta, embedding);
        baseDeConocimientoService.save(base);

        saludo = saludo != null ? saludo : "";
        firma = firma != null ? firma : "";
        String cuerpoFinal = (saludo + "\n\n" + respuesta + "\n\n" + firma).trim();

        String destino = pregunta.getEmail();
        String asunto = "Respuesta a tu consulta: " + pregunta.getPregunta();
        emailService.enviarCorreo(destino, asunto, cuerpoFinal);

        attr.addFlashAttribute("success", "Respuesta enviada, registrada y enviada por correo");
        return "redirect:/preguntas/lista";
    }
    
    private String generarEmbedding(String pregunta) {
    	try {
	    	ObjectMapper mapper = new ObjectMapper();
	    	HttpClient client = HttpClient.newHttpClient();
	    	
	    	String preguntaBody = mapper.writeValueAsString(Map.of(
	                "model", "nomic-embed-text",
	                "prompt", pregunta
	    		));
	
	    	HttpRequest preguntaRequest = HttpRequest.newBuilder()
	    			.uri(URI.create("http://localhost:11434/api/embeddings"))
	    			.header("Content-Type", "application/json")
	    			.POST(HttpRequest.BodyPublishers.ofString(preguntaBody))
	    			.build();
	
	    	HttpResponse<String> preguntaResponse = client.send(preguntaRequest, HttpResponse.BodyHandlers.ofString());
	    	JsonNode preguntaJson = mapper.readTree(preguntaResponse.body());
	    	List<Double> vectorPregunta = mapper.convertValue(preguntaJson.get("embedding"),
	    			mapper.getTypeFactory().constructCollectionType(List.class, Double.class)
	    		);
	        return mapper.writeValueAsString(vectorPregunta); 
    	} catch (IOException | InterruptedException e) {
	        e.printStackTrace();
	        return "Error al generar embedding: " + e.getMessage();
    	}
    }
}
