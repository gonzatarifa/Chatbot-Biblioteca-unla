package com.chatbot.unla.controllers;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.chatbot.unla.helpers.ViewRouteHelper;
import com.chatbot.unla.services.IChatService;
import com.chatbot.unla.services.implementation.ChatServiceV2;

@Controller
@RequestMapping("/")
public class HomeController {
	
	@Autowired
	private ChatServiceV2 chatService;

	
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

}
