package com.chatbot.unla.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.chatbot.unla.helpers.ViewRouteHelper;
import com.chatbot.unla.services.IChatService;

@Controller
@RequestMapping("/")
public class HomeController {
	
	@Autowired
	private IChatService chatService;

	
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
		public ModelAndView procesarPregunta(@RequestParam("pregunta") String preguntaUsuario) {
		    ModelAndView modelAndView = new ModelAndView(ViewRouteHelper.INDEX);
		    String respuestaModelo = chatService.obtenerRespuestaSimilar(preguntaUsuario);

		    String respuestaFinal = "🤐 Pregunta no reconocida. No se responderá nada.";
		    if (!respuestaModelo.equalsIgnoreCase("NINGUNA")) {
		        String respuesta = chatService.buscarRespuesta(respuestaModelo);
		        if (respuesta != null) {
		            respuestaFinal = "🤖 " + respuesta;
		        } else {
		            respuestaFinal = "❌ Pregunta no reconocida: " + respuestaModelo;
		        }
		    }

		    modelAndView.addObject("pregunta", preguntaUsuario);
		    modelAndView.addObject("respuesta", respuestaFinal);
		    return modelAndView;
		}

}
