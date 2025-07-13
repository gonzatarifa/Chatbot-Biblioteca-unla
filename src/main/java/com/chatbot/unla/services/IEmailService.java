package com.chatbot.unla.services;

import org.springframework.stereotype.Service;

@Service
public interface IEmailService {

	 public void enviarCorreo(String to, String asunto, String cuerpo);
	
}
