package com.chatbot.unla.services;

import java.util.List;

import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Service;

import com.chatbot.unla.entities.Usuario;

@Service
public interface IUsuarioService {
	
	public List<Usuario> getAll();

	public Usuario buscar(long id);

	public void eliminar(long id);
	
	public void save(Usuario usuario);
	
	public Usuario getByEmail(@Param("correoElectronico") String correoElectronico);
	
	public Usuario getByDni(@Param("documento") long documento);

	public Usuario getByUsername(@Param("nombreDeUsuario") String nombreDeUsuario);
	
	public void actualizarSaludoYFirma(Long id, String saludo, String firma);
	
	public boolean existeEmailHabilitado(String email, Long id);
	
	public boolean existeUsuarioHabilitado(String username, Long id);
	
	public boolean existeDniHabilitado(long dni, Long id);
}
