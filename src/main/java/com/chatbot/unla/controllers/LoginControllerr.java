package com.chatbot.unla.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.chatbot.unla.helpers.ViewRouteHelper;

@Controller
public class LoginControllerr {
	
	 @GetMapping("/login")
	    public String login() {
	        return "user/login"; // nombre del template Thymeleaf login.html
	    }
}
