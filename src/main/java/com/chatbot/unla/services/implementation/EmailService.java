package com.chatbot.unla.services.implementation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.chatbot.unla.services.IEmailService;

@Service("emailService")
public class EmailService implements IEmailService {
	  @Autowired
	    private JavaMailSender mailSender;

	    @Value("${spring.mail.username}")
	    private String from;

	    public void enviarCorreo(String to, String asunto, String cuerpo) {
	        SimpleMailMessage mensaje = new SimpleMailMessage();
	        mensaje.setFrom(from);
	        mensaje.setTo(to);
	        mensaje.setSubject(asunto);
	        mensaje.setText(cuerpo);
	        mailSender.send(mensaje);
	    }

}
