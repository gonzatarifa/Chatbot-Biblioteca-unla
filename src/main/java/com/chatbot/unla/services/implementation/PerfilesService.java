package com.chatbot.unla.services.implementation;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Service;

import com.chatbot.unla.entities.Perfiles;
import com.chatbot.unla.repositories.IPerfilesRepository;
import com.chatbot.unla.services.IPerfilesService;


@Service("perfilesService")
public class PerfilesService implements IPerfilesService {
	
	@Autowired
	@Qualifier("perfilesRepository")
	private IPerfilesRepository perfilesRepository;

	
	@Override
	public List<Perfiles> getAll() {
		return perfilesRepository.findAll();
	}
	
	@Override
	public void save(Perfiles perfil) {
		perfilesRepository.save(perfil);
	
	}
	
	@Override
	public Perfiles buscar(long id) {
		return perfilesRepository.findById(id).orElse(null);
	}
	
	@Override
	public void eliminar (long id) {
		perfilesRepository.deleteById(id);
	}
	
	public Perfiles getByRol(@Param("rol") String rol) {
		return perfilesRepository.getByRol(rol);
	}
}
