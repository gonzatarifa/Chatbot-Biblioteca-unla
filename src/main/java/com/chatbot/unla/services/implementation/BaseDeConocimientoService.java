package com.chatbot.unla.services.implementation;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.chatbot.unla.entities.BaseDeConocimiento;
import com.chatbot.unla.repositories.IBaseDeConocimientoRepository;
import com.chatbot.unla.services.IBaseDeConocimientoService;

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
	
	@Override
	public List<BaseDeConocimiento> getAllHabilitados() {
		return baseDeConocimientoRepository.findAllHabilitados();
	}
	
	@Override
	public List<BaseDeConocimiento> getAllDeshabilitados() {
		return baseDeConocimientoRepository.findAllDeshabilitados();
	}
	
	@Override
	public List<BaseDeConocimiento> buscarPorTexto(String texto, boolean habilitado) {
		return baseDeConocimientoRepository.buscarPorTextoYEstado(texto, habilitado);
	}

	@Override
	public List<BaseDeConocimiento> buscarPorPregunta(String texto, boolean habilitado) {
		return baseDeConocimientoRepository.buscarPorPreguntayYEstado(texto, habilitado);
	}

	@Override
	public List<BaseDeConocimiento> buscarPorRespuesta(String texto, boolean habilitado) {
		return baseDeConocimientoRepository.buscarPorRespuestaYEstado(texto, habilitado);
	}

	@Override
	public List<BaseDeConocimiento> BuscarPorIdUsuario(String texto, boolean habilitado) {
		return baseDeConocimientoRepository.buscarPorIdUsuarioYEstado(texto, habilitado);
	}

	@Override
	public BaseDeConocimiento buscarPorPreguntaExacta(String pregunta) {
		return baseDeConocimientoRepository.buscarPorPreguntaExacta(pregunta);
	}
	
	;
}
