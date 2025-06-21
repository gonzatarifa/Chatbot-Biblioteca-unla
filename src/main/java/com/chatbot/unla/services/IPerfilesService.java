package com.chatbot.unla.services;

import java.util.List;

import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Service;

import com.chatbot.unla.entities.Perfiles;

@Service
public interface IPerfilesService {
	
	public List<Perfiles> getAll();
	
	public void save(Perfiles perfil);
	
	public Perfiles buscar(long id);
	
	public void eliminar (long id);
	
	public Perfiles getByRol(@Param("rol") String rol);

}
