package com.chatbot.unla.controllers;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.chatbot.unla.services.implementation.PreguntaUsuarioService;

@ControllerAdvice
public class GlobalAttributes {

    private final PreguntaUsuarioService preguntaUsuarioService;

    public GlobalAttributes(PreguntaUsuarioService preguntaUsuarioService) {
        this.preguntaUsuarioService = preguntaUsuarioService;
    }

    @ModelAttribute
    public void agregarCantidadPreguntasPendientes(Model model) {
        long cantidad = preguntaUsuarioService.getCantidadNoRespondidas();
        model.addAttribute("cantPreguntasPendientes", cantidad);
    }
}