package com.chatbot.unla.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.chatbot.unla.helpers.ViewRouteHelper;

@Controller
@RequestMapping("/")
public class HomeController {
	
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

}
