package com.chatbot.unla.controllers;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.chatbot.unla.entities.Usuario;
import com.chatbot.unla.services.IUsuarioService;
import com.chatbot.unla.services.implementation.PreguntaUsuarioService;

@ControllerAdvice
public class GlobalAttributes {

    private final PreguntaUsuarioService preguntaUsuarioService;
	@Autowired
	@Qualifier("usuarioService")
	private IUsuarioService usuarioService;

    public GlobalAttributes(PreguntaUsuarioService preguntaUsuarioService) {
        this.preguntaUsuarioService = preguntaUsuarioService;
    }

    @ModelAttribute
    public void agregarCantidadPreguntasPendientes(Model model) {
        long cantidad = preguntaUsuarioService.getCantidadNoRespondidas();
        model.addAttribute("cantPreguntasPendientes", cantidad);
    }
    
    @ModelAttribute("usuarioLogueadoId")
    public Long getUsuarioLogueadoId(Principal principal) {
        if (principal != null) {
            Usuario usuario = usuarioService.getByUsername(principal.getName());
            if (usuario != null) {
                return usuario.getId();
            }
        }
        return null;
    }
}