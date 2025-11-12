package com.chatbot.unla.services.implementation;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.chatbot.unla.entities.PreguntaUsuario;
import com.chatbot.unla.repositories.IPreguntaUsuarioRepository;
import com.chatbot.unla.services.IPreguntaUsuarioService;

@Service("preguntaUsuarioService")
public class PreguntaUsuarioService implements IPreguntaUsuarioService {

	@Autowired
	@Qualifier("preguntaUsuarioRepository")
	private IPreguntaUsuarioRepository preguntaUsuarioRepository;

	@Override
	public void save(PreguntaUsuario preguntaUsuario) {
		preguntaUsuarioRepository.save(preguntaUsuario);
	}
	
	@Override
	public void saveAll(List<PreguntaUsuario> preguntaUsuarioList) {
		preguntaUsuarioRepository.saveAll(preguntaUsuarioList);
	}

	@Override
	public List<PreguntaUsuario> getAll() {
		return preguntaUsuarioRepository.findAll();
	}

	@Override
	public PreguntaUsuario buscar(long id) {
		return preguntaUsuarioRepository.findById(id).orElse(null);
	}

	@Override
	public void eliminar(long id) {
		preguntaUsuarioRepository.deleteById(id);
	}

	@Override
	public long getCantidadNoRespondidas() {
		 return preguntaUsuarioRepository.findAll()
			        .stream()
			        .filter(p -> p.isHabilitado() && !p.isRespuestaEnviada())
			        .count();
	}
	
	@Override
	public List<PreguntaUsuario> findByUsuarioRespondiendoId(Long usuarioId){
		return preguntaUsuarioRepository.findByUsuarioRespondiendoId(usuarioId);
	}
}
