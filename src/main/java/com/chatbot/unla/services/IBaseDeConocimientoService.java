package com.chatbot.unla.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.chatbot.unla.entities.BaseDeConocimiento;
import com.chatbot.unla.entities.PreguntaUsuario;
import com.chatbot.unla.entities.Usuario;

@Service
public interface IBaseDeConocimientoService {
	
	public List<BaseDeConocimiento> getAll();

	public BaseDeConocimiento buscar(long id);

	public void eliminar(long id);
	
	public void save(BaseDeConocimiento baseDeConocimiento);
	
}
