package com.chatbot.unla.controllers;

import com.chatbot.unla.entities.BaseDeConocimiento;
import com.chatbot.unla.entities.PreguntaUsuario;
import com.chatbot.unla.entities.Usuario;
import com.chatbot.unla.helpers.ViewRouteHelper;
import com.chatbot.unla.services.IPreguntaUsuarioService;
import com.chatbot.unla.services.implementation.BaseDeConocimientoService;
import com.chatbot.unla.services.implementation.EmailService;
import com.chatbot.unla.services.implementation.UsuarioService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
    
    @Autowired
    private UsuarioService usuarioService;

    @GetMapping("/lista")
    public String listarPreguntas(Model model) {
    	
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        Usuario usuario = usuarioService.getByUsername(username);
    	
        List<PreguntaUsuario> preguntas = preguntaUsuarioService.getAll().stream()
                .filter(p -> p.isHabilitado() && !p.isRespuestaEnviada())
                .sorted((p1, p2) -> {
                    boolean p1EsMia = p1.isFijada() && p1.getUsuarioRespondiendo() != null 
                                      && p1.getUsuarioRespondiendo().getId() == usuario.getId();
                    boolean p2EsMia = p2.isFijada() && p2.getUsuarioRespondiendo() != null 
                                      && p2.getUsuarioRespondiendo().getId() == usuario.getId();
                    return Boolean.compare(p2EsMia, p1EsMia);
                })
                .collect(Collectors.toList());

        model.addAttribute("titulo", "Preguntas recibidas");
        model.addAttribute("lista", preguntas);
        model.addAttribute("mostrandoRespondidas", false);
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
            return ViewRouteHelper.PREGUNTA_REDIRECT_LISTA;
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        Usuario usuario = usuarioService.getByUsername(username);

        model.addAttribute("saludoDefault", usuario.getSaludo());
        model.addAttribute("firmaDefault", usuario.getFirma());

        model.addAttribute("pregunta", pregunta);

        return ViewRouteHelper.PREGUNTA_RESPONDER;
    }
    
    // Procesar respuesta
    @PostMapping("/responder")
    public String procesarRespuesta(
            @RequestParam("id") long id,
            @RequestParam("respuesta") String respuesta,
            @RequestParam(name = "saludo", required = false) String saludo,
            @RequestParam(name = "firma", required = false) String firma,
            @RequestParam(name = "preguntaEditada", required = false) String preguntaEditada,
            @RequestParam(name = "respuestaEditada", required = false) String respuestaEditada,
            @RequestParam(name = "guardarEnBaseConocimiento", defaultValue = "false") boolean guardarEnBaseConocimiento,
            RedirectAttributes attr) {

        PreguntaUsuario pregunta = preguntaUsuarioService.buscar(id);
        if (pregunta == null || !pregunta.isHabilitado()) {
            attr.addFlashAttribute("error", "Pregunta no encontrada o deshabilitada");
            return ViewRouteHelper.PREGUNTA_REDIRECT_LISTA;
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        Usuario usuarioRespondio = usuarioService.getByUsername(username);
        if (usuarioRespondio == null) {
            attr.addFlashAttribute("error", "Usuario autenticado no encontrado en el sistema");
            return ViewRouteHelper.PREGUNTA_REDIRECT_LISTA;
        }

        pregunta.setRespuestaEnviada(true);
        pregunta.setFechaEnvioRespuesta(LocalDateTime.now());
        pregunta.setUsuarioRespondio(usuarioRespondio); 
        preguntaUsuarioService.save(pregunta);
        
        //enviar email
        saludo = saludo != null ? saludo : "";
        firma = firma != null ? firma : "";
        String cuerpoFinal = (saludo + "\n\n" + respuesta + "\n\n" + firma).trim();
        String destino = pregunta.getEmail();
        String asunto = "Respuesta a tu consulta: " + pregunta.getPregunta();
        emailService.enviarCorreo(destino, asunto, cuerpoFinal);
        
        if (guardarEnBaseConocimiento) {

            String preguntaParaGuardar =
                    (preguntaEditada != null && !preguntaEditada.trim().isEmpty())
                            ? preguntaEditada.trim()
                            : pregunta.getPregunta();

            String respuestaParaGuardar =
                    (respuestaEditada != null && !respuestaEditada.trim().isEmpty())
                            ? respuestaEditada.trim()
                            : respuesta;

            BaseDeConocimiento existente =
                    baseDeConocimientoService.buscarPorPreguntaExacta(preguntaParaGuardar);

            if (existente == null) {

                BaseDeConocimiento base = new BaseDeConocimiento();
                base.setPregunta(preguntaParaGuardar);
                base.setRespuesta(respuestaParaGuardar);
                base.setEmbedding(generarEmbedding(preguntaParaGuardar));
                base.setPreguntaUsuario(pregunta);

                baseDeConocimientoService.save(base);

                attr.addFlashAttribute("success",
                        "Respuesta enviada y guardada en base de conocimientos");
            } else {
                attr.addFlashAttribute("warning",
                        "La pregunta ya existe en la base de conocimientos");
            }

        } else {
            attr.addFlashAttribute("success", "Respuesta enviada correctamente");
        }

        return ViewRouteHelper.PREGUNTA_REDIRECT_LISTA;
    }
    
    @GetMapping("/respondidas")
    public String listarPreguntasRespondidas(Model model) {
        List<PreguntaUsuario> preguntasRespondidas = preguntaUsuarioService.getAll().stream()
                .filter(p -> p.isHabilitado() && p.isRespuestaEnviada())
                .collect(Collectors.toList());

        model.addAttribute("titulo", "Preguntas Respondidas");
        model.addAttribute("lista", preguntasRespondidas);
        model.addAttribute("mostrandoRespondidas", true);
        return ViewRouteHelper.PREGUNTA_LISTA;
    }
    
    @GetMapping("/fijar/{id}")
    public String fijarPregunta(@PathVariable("id") long id, RedirectAttributes attr) {
        PreguntaUsuario pregunta = preguntaUsuarioService.buscar(id);
        if (pregunta == null || !pregunta.isHabilitado()) {
            attr.addFlashAttribute("error", "Pregunta no encontrada");
            return ViewRouteHelper.PREGUNTA_REDIRECT_LISTA;
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        Usuario usuario = usuarioService.getByUsername(username);

        if (pregunta.isFijada()) {
            if (pregunta.getUsuarioRespondiendo() != null 
                && pregunta.getUsuarioRespondiendo().getId() == usuario.getId()) {
                
                pregunta.setFijada(false);
                pregunta.setUsuarioRespondiendo(null);
                attr.addFlashAttribute("success", "Pregunta liberada");
            } else {
                attr.addFlashAttribute("error", "No podés liberar una pregunta fijada por otro usuario");
            }
        } else {
            pregunta.setFijada(true);
            pregunta.setUsuarioRespondiendo(usuario);
            attr.addFlashAttribute("success", "Pregunta fijada por " + usuario.getNombre());
        }

        preguntaUsuarioService.save(pregunta);
        return ViewRouteHelper.PREGUNTA_REDIRECT_LISTA;
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
