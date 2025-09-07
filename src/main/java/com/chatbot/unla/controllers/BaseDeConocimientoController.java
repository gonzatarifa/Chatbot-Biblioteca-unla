package com.chatbot.unla.controllers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
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
import org.springframework.web.multipart.MultipartFile;
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
	public String guardar(@RequestParam(value="preguntasCargadasJson", required=false) String preguntasJson,
	                      @Valid @ModelAttribute BaseDeConocimiento baseDeConocimiento,
	                      BindingResult result,
	                      RedirectAttributes attribute) {

	    int cargadas = 0;
	    int duplicadas = 0;

	    if(baseDeConocimiento.getPregunta() != null && !baseDeConocimiento.getPregunta().isEmpty()){
	        if(baseDeConocimientoService.buscarPorPreguntaExacta(baseDeConocimiento.getPregunta()) == null){
	            baseDeConocimiento.setFechaCreacion(LocalDateTime.now());
	            baseDeConocimiento.setHabilitado(true);
	            baseDeConocimiento.setEmbedding(generarEmbedding(baseDeConocimiento.getPregunta()));
	            baseDeConocimientoService.save(baseDeConocimiento);
	            cargadas++;
	        } else {
	            duplicadas++;
	        }
	    }
	    attribute.addFlashAttribute("success", String.format("%d entradas guardadas, %d duplicadas ignoradas", cargadas, duplicadas));
	    return "redirect:/baseDeConocimiento/lista";
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
      
    @GetMapping("/upload")
    public String uploadForm(Model model) {
        model.addAttribute("titulo", "Crear Entradas por Archivo");
        model.addAttribute("baseDeConocimiento", new BaseDeConocimiento());
        return ViewRouteHelper.BASE_DE_CONOCIMIENTO_UPLOAD; 
    }
    
    @PostMapping("/uploadPreview")
    public String uploadPreview(@RequestParam("file") MultipartFile file,
                                Model model,
                                RedirectAttributes attribute) {
        if(file.isEmpty()){
            attribute.addFlashAttribute("error","Archivo vacío.");
            return "redirect:/baseDeConocimiento/upload";
        }

        List<Map<String,String>> preview = new ArrayList<>();
        List<String> duplicadas = new ArrayList<>();

        try(InputStream is = file.getInputStream()) {
            String filename = file.getOriginalFilename();
            if(filename.endsWith(".xlsx")){
                Workbook workbook = new XSSFWorkbook(is);
                Sheet sheet = workbook.getSheetAt(0);
                for(Row row : sheet){
                    if(row.getRowNum() == 0) continue; // cabecera
                    Cell preguntaCell = row.getCell(0);
                    Cell respuestaCell = row.getCell(1);
                    if(preguntaCell != null && respuestaCell != null){
                        String pregunta = preguntaCell.getStringCellValue();
                        String respuesta = respuestaCell.getStringCellValue();
                        preview.add(Map.of("pregunta", pregunta, "respuesta", respuesta));
                        if(baseDeConocimientoService.buscarPorPreguntaExacta(pregunta) != null){
                            duplicadas.add(pregunta);
                        }
                    }
                }
                workbook.close();
            } else if(filename.endsWith(".csv")){
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                String line;
                boolean firstLine = true;
                while((line = reader.readLine()) != null){
                    if(firstLine){ firstLine=false; continue; }
                    String[] parts = line.split(",");
                    if(parts.length >=2){
                        String pregunta = parts[0].trim();
                        String respuesta = parts[1].trim();
                        preview.add(Map.of("pregunta", pregunta, "respuesta", respuesta));
                        if(baseDeConocimientoService.buscarPorPreguntaExacta(pregunta) != null){
                            duplicadas.add(pregunta);
                        }
                    }
                }
                reader.close();
            }

            model.addAttribute("preview", preview);
            model.addAttribute("duplicadas", duplicadas);
            model.addAttribute("baseDeConocimiento", new BaseDeConocimiento());
            model.addAttribute("titulo", "Preview de preguntas desde archivo");

            if(!duplicadas.isEmpty()){
                model.addAttribute("error", "Hay preguntas duplicadas marcadas en rojo.");
            }

        } catch(Exception e){
            e.printStackTrace();
            attribute.addFlashAttribute("error", "Error al procesar el archivo: " + e.getMessage());
            return "redirect:/baseDeConocimiento/upload";
        }
        return ViewRouteHelper.BASE_DE_CONOCIMIENTO_UPLOAD;
    }

    @PostMapping("/saveFromPreview")
    public String saveFromPreview(@RequestParam Map<String,String> params, RedirectAttributes attribute){
        int total = Integer.parseInt(params.get("total"));
        int saved = 0;
        int duplicates = 0;

        for(int i=0;i<total;i++){
            String pregunta = params.get("pregunta_"+i);
            String respuesta = params.get("respuesta_"+i);
            if(pregunta==null || pregunta.isEmpty()) continue;

            if(baseDeConocimientoService.buscarPorPreguntaExacta(pregunta)==null){
                BaseDeConocimiento bc = new BaseDeConocimiento();
                bc.setPregunta(pregunta);
                bc.setRespuesta(respuesta);
                bc.setFechaCreacion(LocalDateTime.now());
                bc.setHabilitado(true);
                bc.setEmbedding(generarEmbedding(pregunta));
                baseDeConocimientoService.save(bc);
                saved++;
            } else {
                duplicates++;
            }
        }

        if(duplicates > 0){
            attribute.addFlashAttribute("error","No se pudieron guardar "+duplicates+" preguntas duplicadas.");
        } else {
            attribute.addFlashAttribute("success","Preguntas guardadas correctamente.");
        }

        return "redirect:/baseDeConocimiento/lista";
    }


}
