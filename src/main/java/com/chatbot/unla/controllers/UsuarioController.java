package com.chatbot.unla.controllers;

import java.util.ArrayList;

import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.chatbot.unla.entities.Perfiles;
import com.chatbot.unla.entities.Usuario;
import com.chatbot.unla.helpers.ViewRouteHelper;
import com.chatbot.unla.services.IPerfilesService;
import com.chatbot.unla.services.IUsuarioService;



@Controller
@RequestMapping("/usuarios")

public class UsuarioController {

	@Autowired
	@Qualifier("usuarioService")
	private IUsuarioService usuarioService;

	@Autowired
	@Qualifier("perfilesService")
	private IPerfilesService perfilesService;
	

	@GetMapping("/")
	public String crear(Model model) {
		Usuario usuario = new Usuario();
		List<Perfiles> listaPerfiles = perfilesService.getAll();
		List<Perfiles> perfiles = new ArrayList<Perfiles>();
		for (Perfiles p : listaPerfiles) {
			if (p.isDeshabilitado() == true) {
				perfiles.add(p);
			}
		}
		model.addAttribute("titulo", "Formulario: Nuevo Usuario");
		model.addAttribute("usuario", usuario);
		model.addAttribute("perfiles", perfiles);
		return ViewRouteHelper.USUARIO_INDEX;
	}
	
	@PostMapping("/")
	public String guardar(@Valid @ModelAttribute Usuario usuario,BindingResult result, Model model, RedirectAttributes attribute) {
		List<Perfiles> listaPerfiles = perfilesService.getAll();
		List<Perfiles> perfiles = new ArrayList<Perfiles>();
		for (Perfiles p : listaPerfiles) {
			if (p.isDeshabilitado() == true) {
				perfiles.add(p);
			}
		}

		if(usuarioService.getByDni(usuario.getDocumento())!=null && usuarioService.getByDni(usuario.getDocumento()).getId()!=usuario.getId()) 
		{
			FieldError error = new FieldError("usuario", "documento", "Ya existe un usuario con ese documento");
			result.addError(error);
		}
		
		if(usuarioService.getByUsername(usuario.getNombreDeUsuario())!=null && usuarioService.getByUsername(usuario.getNombreDeUsuario()).getId()!=usuario.getId()) {
			FieldError error = new FieldError("usuario", "nombreDeUsuario", "Ya existe un usuario con ese nombre de usuario");
			result.addError(error);
		}
		if(usuarioService.getByEmail(usuario.getCorreoElectronico())!=null && usuarioService.getByEmail(usuario.getCorreoElectronico()).getId()!=usuario.getId()) {
			FieldError error = new FieldError("usuario", "correoElectronico", "Ya existe un usuario con ese correo electronico");
			result.addError(error);
		}
			
		if(result.hasErrors()) {
			model.addAttribute("titulo", "Formulario: Nuevo Usuario");
			model.addAttribute("usuario", usuario);
			model.addAttribute("perfiles", listaPerfiles);
			System.out.println("Se encontraron Errores en el formulario!");
			return ViewRouteHelper.USUARIO_INDEX;
			}
		usuario.setDeshabilitado(true);
		BCryptPasswordEncoder pe = new BCryptPasswordEncoder();
		String passwordCrypt = pe.encode(usuario.getContrasena());
		usuario.setContrasena(passwordCrypt);
		usuarioService.save(usuario);
		System.out.println("Usuario guardado con exito!");
		attribute.addFlashAttribute("success","Usuario agregado con exito");
		return ViewRouteHelper.USUARIO_REDIRECT_LISTA;

	}

	@GetMapping("/lista")
	public String listarClientes(Model model) {
		List<Usuario> listado = usuarioService.getAll();
		List<Usuario> usuarios = new ArrayList<Usuario>();
		for (Usuario u : listado) {
			if (u.isDeshabilitado() == true) {
				usuarios.add(u);
			}
		}
		model.addAttribute("titulo", "Lista de Usuarios");
		model.addAttribute("lista", usuarios);
		return ViewRouteHelper.USUARIO_LISTA;
	}

	@GetMapping("lista/edit/{id}")
	public String editar(@PathVariable("id") long id, Model model) {
		Usuario usuario1 = usuarioService.buscar(id);
		List<Perfiles> listaPerfiles = perfilesService.getAll();
		model.addAttribute("titulo", "Editar usuario");
		model.addAttribute("usuario", usuario1);
		model.addAttribute("perfiles", listaPerfiles);
		return ViewRouteHelper.USUARIO_FORM;
	}

	@GetMapping("lista/delete/{id}")
	public String eliminar(@PathVariable("id") long id,RedirectAttributes attribute) {
		Usuario u = usuarioService.buscar(id);
		u.setDeshabilitado(false);
		usuarioService.save(u);
		System.out.println("Registro eliminado con exito");
		attribute.addFlashAttribute("warning","Usuario eliminado con exito");
		return ViewRouteHelper.ROUTE_REDIRECT;
	}
	
}