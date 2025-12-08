package com.chatbot.unla.services.implementation;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Service;

import com.chatbot.unla.entities.Usuario;
import com.chatbot.unla.repositories.IUsuarioRepository;
import com.chatbot.unla.services.IUsuarioService;


@Service("usuarioService")
public class UsuarioService implements IUsuarioService {
	
	@Autowired
	@Qualifier("usuarioRepository")
	private IUsuarioRepository usuarioRepository;
	
	@Override
	public List<Usuario> getAll() {
		return usuarioRepository.findAll();
	}

	@Override
	public Usuario buscar(long id) {
		return usuarioRepository.findById(id).orElse(null);
	}
	
	@Override
	public void save(Usuario usuario) {
		usuarioRepository.save(usuario);
	}

	@Override
	public void eliminar(long id) {
		usuarioRepository.deleteById(id);
	}

	public Usuario getByDni(@Param("documento") int documento) {
		return usuarioRepository.getByDni(documento);
	}
	
	public Usuario getByEmail(@Param("correoElectronico") String correoElectronico) {
		return usuarioRepository.getByEmail(correoElectronico);
	}
	
	public Usuario getByUsername(@Param("nombreDeUsuario") String nombreDeUsuario) {
		return usuarioRepository.getByUsername(nombreDeUsuario);
	}

	@Override
	public void actualizarSaludoYFirma(Long id, String saludo, String firma) {
	    Usuario user = usuarioRepository.findById(id).orElseThrow();
	    user.setSaludo(saludo);
	    user.setFirma(firma);
	    usuarioRepository.save(user);
	}

}
