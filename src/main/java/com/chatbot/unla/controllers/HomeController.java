package com.chatbot.unla.controllers;


import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.chatbot.unla.entities.PreguntaUsuario;
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

	
	//index de la primera vista
		@GetMapping("/")
		public ModelAndView index() {
			ModelAndView modelAndView = new ModelAndView(ViewRouteHelper.INDEX);
			return modelAndView;
		}
	
	//index para poder usar el logo y volver a inicio
		@GetMapping("/index")
		public ModelAndView indexx() {
			ModelAndView modelAndView = new ModelAndView(ViewRouteHelper.INDEX);
			return modelAndView;
		}
		
		@PostMapping("/")
		public String procesarPregunta(@RequestParam("pregunta") String preguntaUsuario,
		                               RedirectAttributes redirectAttributes) {
		    String respuestaModelo = chatService.procesarPregunta(preguntaUsuario);

		    String respuestaFinal = "🤐 Pregunta no reconocida. No se responderá nada.";
		    if (!respuestaModelo.equalsIgnoreCase("NINGUNA")) {
		        String respuesta = chatService.buscarRespuesta(respuestaModelo);
		        if (respuesta != null) {
		            respuestaFinal = respuesta;
		        } else {
		            respuestaFinal = "❌ Pregunta no reconocida: " + respuestaModelo;
		        }
		    }

		    redirectAttributes.addFlashAttribute("pregunta", preguntaUsuario);
		    redirectAttributes.addFlashAttribute("respuesta", respuestaFinal);
		    return "redirect:/";
		}
		
		@PostMapping("/feedback")
		public String recibirFeedback(
		        @RequestParam("pregunta") String pregunta,
		        @RequestParam(value = "nombre", required = false) String nombre,
		        @RequestParam(value = "apellido", required = false) String apellido,
		        @RequestParam(value = "email", required = false) String email,
		        @RequestParam(value = "util", required = false) String util,
		        @RequestParam(value = "respuesta", required = false) String respuesta,
		        RedirectAttributes redirectAttributes) {

		    if (util != null && util.equals("true")) {
		        redirectAttributes.addFlashAttribute("agradecimientoFeedback", "✅ ¡Gracias por tu feedback!");
		        redirectAttributes.addFlashAttribute("respuesta", respuesta);
		        redirectAttributes.addFlashAttribute("pregunta", pregunta);
		        return "redirect:/";
		    }

		    // Si no marcó como útil, asumimos que es feedback
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
		        "Tu pregunta fue: \"" + pregunta + "\". 📧 Recibirás una respuesta al correo proporcionado a la brevedad.");
		    redirectAttributes.addFlashAttribute("pregunta", pregunta);
		    redirectAttributes.addFlashAttribute("showFeedbackForm", false);
		    return "redirect:/";
		}

}
