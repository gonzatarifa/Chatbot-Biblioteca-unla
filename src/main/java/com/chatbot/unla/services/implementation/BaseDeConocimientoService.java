package com.chatbot.unla.services.implementation;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.chatbot.unla.entities.BaseDeConocimiento;
import com.chatbot.unla.entities.PreguntaUsuario;
import com.chatbot.unla.repositories.IBaseDeConocimientoRepository;
import com.chatbot.unla.repositories.IPreguntaUsuarioRepository;
import com.chatbot.unla.services.IBaseDeConocimientoService;
import com.chatbot.unla.services.IPreguntaUsuarioService;

@Service("baseDeConocimientoService")
public class BaseDeConocimientoService implements IBaseDeConocimientoService {

	@Autowired
	@Qualifier("baseDeConocimientoRepository")
	private IBaseDeConocimientoRepository baseDeConocimientoRepository;

	@Override
	public void save(BaseDeConocimiento baseDeConocimiento) {
		baseDeConocimientoRepository.save(baseDeConocimiento);
	}

	@Override
	public List<BaseDeConocimiento> getAll() {
		return baseDeConocimientoRepository.findAll();
	}

	@Override
	public BaseDeConocimiento buscar(long id) {
		return baseDeConocimientoRepository.findById(id).orElse(null);
	}

	@Override
	public void eliminar(long id) {
		baseDeConocimientoRepository.deleteById(id);
	}
}
