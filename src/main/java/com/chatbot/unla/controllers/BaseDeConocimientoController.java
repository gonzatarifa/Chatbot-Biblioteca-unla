package com.chatbot.unla.controllers;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.nio.file.Paths;
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
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
import com.chatbot.unla.entities.model.ConflictoBC;
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
	    
	    if (!model.containsAttribute("baseDeConocimiento")) {
	        model.addAttribute("baseDeConocimiento", new BaseDeConocimiento());
	    }

	    model.addAttribute("titulo", "Formulario: Nueva Entrada");
	    return ViewRouteHelper.BASE_DE_CONOCIMIENTO_INDEX;
	}
	
	@PostMapping("/")
	public String guardar(
	        @RequestParam(value = "confirmarActualizacion", required = false) Boolean confirmar,
	        @Valid @ModelAttribute BaseDeConocimiento baseDeConocimiento,
	        BindingResult result,
	        RedirectAttributes attribute) {

	    // Validaciones
	    if (baseDeConocimiento.getPregunta() != null &&
	        baseDeConocimiento.getPregunta().length() > 500) {

	        attribute.addFlashAttribute("error",
	                "La pregunta supera el máximo de 500 caracteres.");
	        attribute.addFlashAttribute("baseDeConocimiento", baseDeConocimiento);
	        return ViewRouteHelper.BASE_DE_CONOCIMIENTO_REDIRECT;
	    }

	    if (baseDeConocimiento.getRespuesta() != null &&
	        baseDeConocimiento.getRespuesta().length() > 1500) {

	        attribute.addFlashAttribute("error",
	                "La respuesta supera el máximo de 1500 caracteres.");
	        attribute.addFlashAttribute("baseDeConocimiento", baseDeConocimiento);
	        return ViewRouteHelper.BASE_DE_CONOCIMIENTO_REDIRECT;
	    }

	    if (baseDeConocimiento.getPregunta() == null ||
	        baseDeConocimiento.getPregunta().trim().isEmpty()) {

	        attribute.addFlashAttribute("error", "La pregunta no puede estar vacía.");
	        return ViewRouteHelper.BASE_DE_CONOCIMIENTO_REDIRECT;
	    }

	    BaseDeConocimiento existente =
	            baseDeConocimientoService.buscarPorPreguntaExacta(
	                    baseDeConocimiento.getPregunta().trim()
	            );

	    // EXISTE Y ESTÁ DESHABILITADA → PEDIR CONFIRMACIÓN
	    if (existente != null && !existente.isHabilitado() && confirmar == null) {

	        attribute.addFlashAttribute(
	                "warning",
	                "Ya existe una pregunta deshabilitada en la base de conocimiento. ¿Desea actualizarla y habilitarla nuevamente?"
	        );
	        attribute.addFlashAttribute("requiereConfirmacion", true);
	        attribute.addFlashAttribute("baseDeConocimiento", baseDeConocimiento);
	        attribute.addFlashAttribute("respuestaAnterior", existente.getRespuesta());
	        attribute.addFlashAttribute("preguntaExistente", existente.getPregunta());


	        return ViewRouteHelper.BASE_DE_CONOCIMIENTO_REDIRECT;
	    }

	    // CONFIRMADO → ACTUALIZAR
	    if (existente != null && !existente.isHabilitado() && Boolean.TRUE.equals(confirmar)) {

	        existente.setRespuesta(baseDeConocimiento.getRespuesta());
	        existente.setHabilitado(true);
	        existente.setFechaActualizacion(LocalDateTime.now());
	        existente.setEmbedding(generarEmbedding(existente.getPregunta()));

	        baseDeConocimientoService.save(existente);

	        attribute.addFlashAttribute(
	                "success",
	                "La pregunta fue actualizada y habilitada correctamente."
	        );

	        return ViewRouteHelper.BASE_DE_CONOCIMIENTO_REDIRECT_LISTA;
	    }

	    // NO EXISTE → CREAR NUEVA
	    if (existente == null) {

	        baseDeConocimiento.setFechaCreacion(LocalDateTime.now());
	        baseDeConocimiento.setFechaActualizacion(LocalDateTime.now());
	        baseDeConocimiento.setHabilitado(true);
	        baseDeConocimiento.setEmbedding(
	                generarEmbedding(baseDeConocimiento.getPregunta())
	        );

	        baseDeConocimientoService.save(baseDeConocimiento);

	        attribute.addFlashAttribute(
	                "success",
	                "Entrada creada correctamente."
	        );

	        return ViewRouteHelper.BASE_DE_CONOCIMIENTO_REDIRECT_LISTA;
	    }

	    // EXISTE Y ESTÁ HABILITADA
	    attribute.addFlashAttribute(
	            "error",
	            "La pregunta ya existe y se encuentra habilitada."
	    );

	    return ViewRouteHelper.BASE_DE_CONOCIMIENTO_REDIRECT;
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

        model.addAttribute("preview", null);
        model.addAttribute("duplicadas", null);
        model.addAttribute("erroresLongitud", null);
        model.addAttribute("conflictos", null);

        return ViewRouteHelper.BASE_DE_CONOCIMIENTO_UPLOAD;
    }

    
    @PostMapping("/uploadPreview")
    public String uploadPreview(
            @RequestParam("file") MultipartFile file,
            Model model,
            RedirectAttributes attribute) {

        if (file.isEmpty()) {
            attribute.addFlashAttribute("error", "Archivo vacío.");
            return ViewRouteHelper.BASE_DE_CONOCIMIENTO_REDIRECT_UPLOAD;
        }

        List<Map<String, String>> preview = new ArrayList<>();
        List<String> duplicadas = new ArrayList<>();
        List<Integer> erroresLongitud = new ArrayList<>();
        List<ConflictoBC> conflictos = new ArrayList<>();

        try (InputStream is = file.getInputStream()) {

            String filename = file.getOriginalFilename();

            Workbook workbook = filename != null && filename.endsWith(".xlsx")
                    ? new XSSFWorkbook(is)
                    : null;

            if (workbook != null) {

                Sheet sheet = workbook.getSheetAt(0);

                for (Row row : sheet) {
                    if (row.getRowNum() == 0) continue;

                    Cell cp = row.getCell(0);
                    Cell cr = row.getCell(1);
                    if (cp == null || cr == null) continue;

                    String pregunta = cp.getStringCellValue().trim();
                    String respuesta = cr.getStringCellValue().trim();

                    preview.add(Map.of(
                            "pregunta", pregunta,
                            "respuesta", respuesta
                    ));

                    int index = preview.size() - 1;

                    if (pregunta.length() > 500 || respuesta.length() > 1500) {
                        erroresLongitud.add(index);
                    }

                    BaseDeConocimiento existente =
                            baseDeConocimientoService.buscarPorPreguntaExacta(pregunta);

                    if (existente != null) {
                        if (!existente.isHabilitado()) {
                            ConflictoBC c = new ConflictoBC();
                            c.setPregunta(pregunta);
                            c.setRespuestaExistente(existente.getRespuesta());
                            c.setRespuestaNueva(respuesta);
                            conflictos.add(c);
                        } else {
                            duplicadas.add(pregunta);
                        }
                    }
                }

                workbook.close();
            }

        } catch (Exception e) {
            attribute.addFlashAttribute("error", "Error procesando archivo.");
            return ViewRouteHelper.BASE_DE_CONOCIMIENTO_REDIRECT_UPLOAD;
        }

        // 🔹 Datos para la vista
        model.addAttribute("preview", preview);
        model.addAttribute("duplicadas", duplicadas);
        model.addAttribute("erroresLongitud", erroresLongitud);
        model.addAttribute("conflictos", conflictos);
        model.addAttribute("titulo", "Previsualización de preguntas");

        return ViewRouteHelper.BASE_DE_CONOCIMIENTO_UPLOAD;
    }


    @PostMapping("/saveFromPreview")
    public String saveFromPreview(
            @RequestParam Map<String, String> params,
            @RequestParam(required = false, name = "confirmar_conflicto") List<String> confirmarConflictos,
            @RequestParam(required = false, name = "conflicto_respuesta") List<String> respuestasConflicto,
            RedirectAttributes redirect) {

        int total = Integer.parseInt(params.getOrDefault("total", "0"));
        int cargadas = 0;

        /* ----------- GUARDAR NUEVAS ----------- */
        for (int i = 0; i < total; i++) {

            String pregunta = params.get("pregunta_" + i);
            String respuesta = params.get("respuesta_" + i);

            if (pregunta == null || respuesta == null) continue;

            BaseDeConocimiento existente =
                    baseDeConocimientoService.buscarPorPreguntaExacta(pregunta);

            if (existente != null) continue;

            BaseDeConocimiento nueva = new BaseDeConocimiento();
            nueva.setPregunta(pregunta);
            nueva.setRespuesta(respuesta);
            nueva.setFechaCreacion(LocalDateTime.now());
            nueva.setHabilitado(true);
            nueva.setEmbedding(generarEmbedding(pregunta));

            baseDeConocimientoService.save(nueva);
            cargadas++;
        }

        /* ----------- RESOLVER CONFLICTOS ----------- */
        if (confirmarConflictos != null && respuestasConflicto != null) {

            for (int i = 0; i < confirmarConflictos.size(); i++) {

                String pregunta = confirmarConflictos.get(i);
                String respuestaNueva = respuestasConflicto.get(i);

                BaseDeConocimiento existente =
                        baseDeConocimientoService.buscarPorPreguntaExacta(pregunta);

                if (existente != null && !existente.isHabilitado()) {
                    existente.setRespuesta(respuestaNueva);
                    existente.setHabilitado(true);
                    existente.setFechaActualizacion(LocalDateTime.now());
                    existente.setEmbedding(generarEmbedding(pregunta));

                    baseDeConocimientoService.save(existente);
                    cargadas++;
                }
            }
        }

        redirect.addFlashAttribute(
                "success",
                cargadas + " entradas guardadas correctamente."
        );

        return ViewRouteHelper.BASE_DE_CONOCIMIENTO_REDIRECT_LISTA;
    }


    
    @GetMapping("/descargar-ejemplo")
    public ResponseEntity<Resource> descargarEjemplo() throws IOException {
        Path path = Paths.get("src/main/resources/static/archivos/ejemplo_preguntas.xlsx");
        Resource resource = new UrlResource(path.toUri());

        if (!resource.exists()) {
            throw new IOException("No se encontró el archivo de ejemplo en la ruta especificada");
        }
        
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=ejemplo_preguntas.xlsx")
                .body(resource);
    }


}