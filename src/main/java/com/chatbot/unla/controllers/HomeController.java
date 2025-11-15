package com.chatbot.unla.controllers;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.chatbot.unla.entities.PreguntaUsuario;
import com.chatbot.unla.entities.model.MensajeChat;
import com.chatbot.unla.helpers.FeedbackSession;
import com.chatbot.unla.helpers.ViewRouteHelper;
import com.chatbot.unla.services.IPreguntaUsuarioService;
import com.chatbot.unla.services.implementation.ChatServiceV2;

@Controller
@RequestMapping("/")
public class HomeController {

	@Autowired
	private ChatServiceV2 chatService;

	@Autowired
	private IPreguntaUsuarioService preguntaUsuarioService;

    @GetMapping("/")
    public ModelAndView index(HttpSession session) {
        ModelAndView modelAndView = new ModelAndView(ViewRouteHelper.INDEX);

        String username = "anonimo";
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            username = auth.getName();
        }

        String sessionKey = "historial_" + username;
        List<MensajeChat> historial = (List<MensajeChat>) session.getAttribute(sessionKey);
        if (historial == null) {
            historial = new ArrayList<>();
            String mensajeInicial = "¡Bienvenido/a! Estás en el Servicio de Referencia Virtual “Rodolfo Puiggrós”. " +
                    "Podés preguntarme lo que quieras y te ayudaré con información de la biblioteca.";
            historial.add(new MensajeChat("bot", mensajeInicial));
            session.setAttribute(sessionKey, historial);
        }
        modelAndView.addObject("historial", historial);

        String feedbackKey = "feedbackSession_" + username;
        FeedbackSession feedbackSession = (FeedbackSession) session.getAttribute(feedbackKey);
        if (feedbackSession != null) {
            modelAndView.addObject("feedbackSession", feedbackSession);
        }

        return modelAndView;
    }
	
	// index para poder usar el logo y volver a inicio
	@GetMapping("/index")
	public ModelAndView indexx() {
		ModelAndView modelAndView = new ModelAndView(ViewRouteHelper.INDEX);
		return modelAndView;
	}


    @PostMapping("/")
    public String procesarPregunta(@RequestParam("pregunta") String preguntaUsuario,
                                   RedirectAttributes redirectAttributes, HttpSession session) {
    	
    	 if (preguntaUsuario.length() > 500) {
    	        redirectAttributes.addFlashAttribute("respuesta",
    	            "La pregunta supera el límite de 500 caracteres. Intentá reducirla.");
    	        redirectAttributes.addFlashAttribute("respuestaGenerica", false);
    	        return "redirect:/";
    	    }
    	
        String username = "anonimo";
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            username = auth.getName();
        }

        String sessionKey = "historial_" + username;
        List<MensajeChat> historial = (List<MensajeChat>) session.getAttribute(sessionKey);
        if (historial == null) historial = new ArrayList<>();
        historial.add(new MensajeChat("usuario", preguntaUsuario));

        String respuestaModelo = null;
        for (int i = 0; i < 2; i++) {
            respuestaModelo = chatService.procesarPregunta(preguntaUsuario);
            if (!"NINGUNA".equalsIgnoreCase(respuestaModelo)) break;
        }

        String respuestaFinal;
        boolean respuestaGenerica;

        if (!"NINGUNA".equalsIgnoreCase(respuestaModelo)) {
            String respuesta = chatService.buscarRespuesta(respuestaModelo);
            if (respuesta != null) {
                respuestaFinal = respuesta;
                respuestaGenerica = false;
            } else {
                respuestaFinal = "Pregunta no reconocida: " + respuestaModelo +
                        ". Podés completar el formulario abajo y un bibliotecario te enviará la respuesta a tu correo a la brevedad.";
                respuestaGenerica = true;
            }
        } else {
            respuestaFinal = "No pude responder automáticamente tu pregunta. " +
                    "Podés completar el formulario abajo y un bibliotecario te enviará la respuesta a tu correo a la brevedad.";
            respuestaGenerica = true;
        }

        historial.add(new MensajeChat("bot", respuestaFinal));
        session.setAttribute(sessionKey, historial);

        redirectAttributes.addFlashAttribute("pregunta", preguntaUsuario);
        redirectAttributes.addFlashAttribute("respuesta", respuestaFinal);
        redirectAttributes.addFlashAttribute("respuestaGenerica", respuestaGenerica);

        return "redirect:/";
    }

    @PostMapping("/feedback")
    public String recibirFeedback(@RequestParam("pregunta") String pregunta,
                                  @RequestParam(value = "nombre", required = false) String nombre,
                                  @RequestParam(value = "apellido", required = false) String apellido,
                                  @RequestParam(value = "email", required = false) String email,
                                  @RequestParam(value = "util", required = false) String util,
                                  @RequestParam(value = "respuesta", required = false) String respuesta,
                                  RedirectAttributes redirectAttributes,
                                  HttpSession session) {

        String username = "anonimo";
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            username = auth.getName();
        }

        if ("true".equals(util)) {
            redirectAttributes.addFlashAttribute("agradecimientoFeedback", "¡Gracias por tu feedback!");
            redirectAttributes.addFlashAttribute("respuesta", respuesta);
            redirectAttributes.addFlashAttribute("pregunta", pregunta);
            return "redirect:/";
        }

        FeedbackSession feedbackSession = new FeedbackSession();
        feedbackSession.setNombre(nombre);
        feedbackSession.setApellido(apellido);
        feedbackSession.setEmail(email);
        feedbackSession.setPregunta(pregunta);
        String feedbackKey = "feedbackSession_" + username;
        session.setAttribute(feedbackKey, feedbackSession);

        PreguntaUsuario feedback = new PreguntaUsuario();
        feedback.setPregunta(pregunta);
        feedback.setNombre(nombre);
        feedback.setApellido(apellido);
        feedback.setEmail(email);
        feedback.setFechaEnvioPregunta(LocalDateTime.now());
        feedback.setHabilitado(true);
        feedback.setRespuestaEnviada(false);
        preguntaUsuarioService.save(feedback);

        redirectAttributes.addFlashAttribute("agradecimientoFeedback",
                "Tu pregunta fue: \"" + pregunta + "\". Recibirás una respuesta al correo proporcionado a la brevedad.");
        redirectAttributes.addFlashAttribute("respuesta", "Pendiente");
        redirectAttributes.addFlashAttribute("pregunta", pregunta);
        redirectAttributes.addFlashAttribute("showFeedbackForm", true);
        return "redirect:/";
    }
	
	@GetMapping("/chat/pdf")
	public void descargarChat(HttpSession session, HttpServletResponse response) throws Exception {
	    String username = "anonimo";
	    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
	    if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
	        username = auth.getName();
	    }
	    String sessionKey = "historial_" + username;

	    List<MensajeChat> historial = (List<MensajeChat>) session.getAttribute(sessionKey);
	    if (historial == null || historial.isEmpty()) {
	        historial = new ArrayList<>();
	        historial.add(new MensajeChat("bot", "No hay conversación disponible."));
	    }

	    response.setContentType("application/pdf");
	    response.setHeader("Content-Disposition", "attachment; filename=conversacion.pdf");

	    com.lowagie.text.Document document = new com.lowagie.text.Document();
	    com.lowagie.text.pdf.PdfWriter.getInstance(document, response.getOutputStream());

	    document.open();

	    // Colores y fuentes
	    java.awt.Color granate = new java.awt.Color(128, 0, 32); 
	    com.lowagie.text.Font fontTitulo = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 16, com.lowagie.text.Font.BOLD, granate);
	    com.lowagie.text.Font fontUsuario = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 12, com.lowagie.text.Font.BOLD, java.awt.Color.BLUE);
	    com.lowagie.text.Font fontBot = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 12, com.lowagie.text.Font.BOLD, granate);
	    com.lowagie.text.Font fontNormal = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 12);

	    com.lowagie.text.pdf.PdfPTable headerTable = new com.lowagie.text.pdf.PdfPTable(2);
	    headerTable.setWidthPercentage(100);
	    headerTable.setWidths(new int[]{1, 5}); 

	    try {
	        com.lowagie.text.Image logo = com.lowagie.text.Image.getInstance("src/main/resources/static/images/UNla_logo.png");
	        logo.scaleAbsolute(50, 50); 
	        com.lowagie.text.pdf.PdfPCell logoCell = new com.lowagie.text.pdf.PdfPCell(logo, false);
	        logoCell.setBorder(com.lowagie.text.Rectangle.NO_BORDER);
	        logoCell.setVerticalAlignment(com.lowagie.text.Element.ALIGN_MIDDLE);
	        headerTable.addCell(logoCell);

	        com.lowagie.text.pdf.PdfPCell titleCell = new com.lowagie.text.pdf.PdfPCell(
	            new com.lowagie.text.Paragraph("Conversación con el Servicio de Referencia Virtual\nRodolfo Puiggrós - UNLa\n\n", fontTitulo)
	        );
	        titleCell.setBorder(com.lowagie.text.Rectangle.NO_BORDER);
	        titleCell.setVerticalAlignment(com.lowagie.text.Element.ALIGN_MIDDLE);
	        titleCell.setHorizontalAlignment(com.lowagie.text.Element.ALIGN_CENTER);
	        headerTable.addCell(titleCell);

	        document.add(headerTable);

	    } catch (Exception e) {
	        System.out.println("No se pudo cargar el logo de la UNLa: " + e.getMessage());
	    }

	    com.lowagie.text.pdf.draw.LineSeparator separator = new com.lowagie.text.pdf.draw.LineSeparator();
	    separator.setLineColor(granate);
	    document.add(new com.lowagie.text.Chunk(separator));
	    document.add(new com.lowagie.text.Paragraph("\n"));

	    // Historial de mensajes
	    for (MensajeChat msg : historial) {
	        if ("usuario".equals(msg.getRemitente())) {
	            document.add(new com.lowagie.text.Paragraph("Tu: ", fontUsuario));
	        } else {
	            document.add(new com.lowagie.text.Paragraph("Chatbot: ", fontBot));
	        }
	        document.add(new com.lowagie.text.Paragraph(msg.getContenido(), fontNormal));
	        document.add(new com.lowagie.text.Chunk(new com.lowagie.text.pdf.draw.DottedLineSeparator()));
	    }

	    document.close();
	}

}
