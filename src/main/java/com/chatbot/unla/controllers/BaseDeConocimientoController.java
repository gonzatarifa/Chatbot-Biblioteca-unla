package com.chatbot.unla.controllers;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.chatbot.unla.entities.BaseDeConocimiento;
import com.chatbot.unla.helpers.ViewRouteHelper;
import com.chatbot.unla.services.IBaseDeConocimientoService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Controller
@RequestMapping("/baseDeConocimiento")
public class BaseDeConocimientoController {
	
	@Autowired
    private IBaseDeConocimientoService baseDeConocimientoService;
	
	@GetMapping("/")
	public String crear(Model model) {
	    BaseDeConocimiento baseDeConocimiento = new BaseDeConocimiento();
		model.addAttribute("titulo", "Formulario: Nueva Entrada");
		model.addAttribute("baseDeConocimiento", baseDeConocimiento);
		return ViewRouteHelper.BASE_DE_CONOCIMIENTO_INDEX;
	}
	
	@PostMapping("/")
	public String guardar(@Valid @ModelAttribute BaseDeConocimiento baseDeConocimiento,
	                      BindingResult result,
	                      Model model,
	                      RedirectAttributes attribute) {

		if (result.hasErrors()) {
			model.addAttribute("titulo", "Formulario: Nueva Entrada");
			model.addAttribute("baseDeConocimiento", baseDeConocimiento);
			return ViewRouteHelper.BASE_DE_CONOCIMIENTO_INDEX;
		}

		LocalDateTime now = LocalDateTime.now();
		if (baseDeConocimiento.getId() == 0) {
			baseDeConocimiento.setFechaCreacion(now);
			baseDeConocimiento.setHabilitado(true);
		} else {
	        // Si es edicion
	        BaseDeConocimiento original = baseDeConocimientoService.buscar(baseDeConocimiento.getId());
	        if (original != null) {
	            baseDeConocimiento.setHabilitado(original.isHabilitado());
	        }
	    }
		baseDeConocimiento.setFechaActualizacion(now);
		baseDeConocimiento.setEmbedding(generarEmbedding(baseDeConocimiento.getPregunta()));

		baseDeConocimientoService.save(baseDeConocimiento);
		attribute.addFlashAttribute("success", "Entrada guardada con éxito");
		return ViewRouteHelper.BASE_DE_CONOCIMIENTO_REDIRECT_LISTA;
	}

	@GetMapping("/lista")
	public String listar(@RequestParam(name = "verDeshabilitadas", required = false, defaultValue = "false") boolean verDeshabilitadas, Model model) {
		List<BaseDeConocimiento> lista;
		String titulo;

		if (verDeshabilitadas) {
			lista = baseDeConocimientoService.getAllDeshabilitados();
			titulo = "Listado de Preguntas Deshabilitadas";
		} else {
			lista = baseDeConocimientoService.getAllHabilitados();
			titulo = "Listado de Preguntas Habilitadas";
		}

		model.addAttribute("lista", lista);
		model.addAttribute("titulo", titulo);
		model.addAttribute("verDeshabilitadas", verDeshabilitadas);
		model.addAttribute("mostrarDeshabilitados", verDeshabilitadas);
		return ViewRouteHelper.BASE_DE_CONOCIMIENTO_LISTA;
	}
    
    @GetMapping("/lista/edit/{id}")
    public String editar(@PathVariable("id") long id, Model model) {
    	BaseDeConocimiento base = baseDeConocimientoService.buscar(id);
    	model.addAttribute("titulo", "Editar Entrada");
    	model.addAttribute("baseDeConocimiento", base);
    	return ViewRouteHelper.BASE_DE_CONOCIMIENTO_INDEX;
    }
    
    @GetMapping("/lista/delete/{id}")
    public String eliminar(@PathVariable("id") long id, RedirectAttributes attribute) {
    	BaseDeConocimiento base = baseDeConocimientoService.buscar(id);
    	base.setHabilitado(false);
    	baseDeConocimientoService.save(base);
    	attribute.addFlashAttribute("warning","Pregunta eliminada con exito");
    	return ViewRouteHelper.BASE_DE_CONOCIMIENTO_REDIRECT_LISTA;
    }
    
    @GetMapping("/lista/restore/{id}")
    public String restaurar(@PathVariable("id") long id, RedirectAttributes attribute) {
        BaseDeConocimiento base = baseDeConocimientoService.buscar(id);
        base.setHabilitado(true);
        baseDeConocimientoService.save(base);
        attribute.addFlashAttribute("success", "Pregunta restaurada con éxito");
        return ViewRouteHelper.BASE_DE_CONOCIMIENTO_REDIRECT_LISTA_DESHABILITADAS;
    }
    
    @GetMapping("/buscar")
    public String buscar(@RequestParam("query") String query,
            @RequestParam(value = "filtro", required = false, defaultValue = "todos") String filtro,
            Model model) {
    	List<BaseDeConocimiento> resultados;
    	
    	switch (filtro) {
        case "pregunta":
            resultados = baseDeConocimientoService.buscarPorPregunta(query, true);
            break;
        case "respuesta":
            resultados = baseDeConocimientoService.buscarPorRespuesta(query, true);
            break;
        case "ticket":
            resultados = baseDeConocimientoService.BuscarPorIdUsuario(query, true);
            break;
        default:
            resultados = baseDeConocimientoService.buscarPorTexto(query, true);
            break;
    	}
    	model.addAttribute("titulo", "Resultados para: " + query);
    	model.addAttribute("lista", resultados);
    	model.addAttribute("query", query);
    	model.addAttribute("filtro", filtro);
    	return ViewRouteHelper.BASE_DE_CONOCIMIENTO_LISTA;
    }
    
    @GetMapping("/buscarDeshabilitados")
    public String buscarDeshabilitados(@RequestParam("query") String query,
            @RequestParam(value = "filtro", required = false, defaultValue = "todos") String filtro,
            Model model) {
        List<BaseDeConocimiento> resultados;
        switch (filtro) {
        case "pregunta":
            resultados = baseDeConocimientoService.buscarPorPregunta(query, false);
            break;
        case "respuesta":
            resultados = baseDeConocimientoService.buscarPorRespuesta(query, false);
            break;
        case "ticket":
            resultados = baseDeConocimientoService.BuscarPorIdUsuario(query, false);
            break;
        default:
            resultados = baseDeConocimientoService.buscarPorTexto(query, false);
            break;
    	}
        model.addAttribute("titulo", "Resultados de busqueda deshabilitadas para: " + query);
        model.addAttribute("lista", resultados);
        model.addAttribute("query", query);
        model.addAttribute("filtro", filtro);
        model.addAttribute("verDeshabilitadas", true);
        model.addAttribute("mostrarDeshabilitados", true);
        return ViewRouteHelper.BASE_DE_CONOCIMIENTO_LISTA;
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
