package com.chatbot.unla.controllers;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.AccessDeniedException;
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
import com.chatbot.unla.entities.PreguntaUsuario;
import com.chatbot.unla.entities.Usuario;
import com.chatbot.unla.helpers.ViewRouteHelper;
import com.chatbot.unla.services.IEmailService;
import com.chatbot.unla.services.IPerfilesService;
import com.chatbot.unla.services.IPreguntaUsuarioService;
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
	
	@Autowired
	@Qualifier("preguntaUsuarioService")
	private IPreguntaUsuarioService preguntaUsuarioService;
	
	@Autowired
	private IEmailService emailService;
	
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;
	

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

		if (usuarioService.existeDniHabilitado(usuario.getDocumento(), usuario.getId())) {

			result.addError(
					new FieldError("usuario", "documento", "Ya existe un usuario habilitado con ese documento"));
		}

		if (usuarioService.existeUsuarioHabilitado(usuario.getNombreDeUsuario(), usuario.getId())) {

			result.addError(new FieldError("usuario", "nombreDeUsuario",
					"Ya existe un usuario habilitado con ese nombre de usuario"));
		}

		if (usuarioService.existeEmailHabilitado(usuario.getCorreoElectronico(), usuario.getId())) {

			result.addError(new FieldError("usuario", "correoElectronico",
					"Ya existe un usuario habilitado con ese correo electrónico"));
		}

		if (usuario.getContrasena() == null || usuario.getContrasena().trim().isEmpty()) {
			result.addError(new FieldError("usuario", "contrasena", "La contraseña no puede estar vacía"));
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
	    usuario.setSaludo(usuario.getSaludo());
	    usuario.setFirma(usuario.getFirma());
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
	
	@PostMapping("lista/edit/{id}")
	public String editar(@PathVariable("id") long id,
	                     @Valid @ModelAttribute("usuario") Usuario usuario,
	                     BindingResult result,
	                     Model model,
	                     RedirectAttributes attributes) {

	    List<Perfiles> listaPerfiles = perfilesService.getAll();
	    
		if (usuarioService.existeDniHabilitado(usuario.getDocumento(), usuario.getId())) {

			result.addError(
					new FieldError("usuario", "documento", "Ya existe un usuario habilitado con ese documento"));
		}

		if (usuarioService.existeUsuarioHabilitado(usuario.getNombreDeUsuario(), usuario.getId())) {

			result.addError(new FieldError("usuario", "nombreDeUsuario",
					"Ya existe un usuario habilitado con ese nombre de usuario"));
		}

		if (usuarioService.existeEmailHabilitado(usuario.getCorreoElectronico(), usuario.getId())) {

			result.addError(new FieldError("usuario", "correoElectronico",
					"Ya existe un usuario habilitado con ese correo electrónico"));
		}
	    if (result.hasErrors()) {
	        model.addAttribute("titulo", "Editar usuario");
	        model.addAttribute("usuario", usuario);
	        model.addAttribute("perfiles", listaPerfiles);
	        return ViewRouteHelper.USUARIO_FORM;
	    }
	    Usuario usuarioBD = usuarioService.buscar(id);
	    usuarioBD.setNombre(usuario.getNombre());
	    usuarioBD.setApellido(usuario.getApellido());
	    usuarioBD.setTipoDocumento(usuario.getTipoDocumento());
	    usuarioBD.setDocumento(usuario.getDocumento());
	    usuarioBD.setCorreoElectronico(usuario.getCorreoElectronico());
	    usuarioBD.setNombreDeUsuario(usuario.getNombreDeUsuario());
	    usuarioBD.setPerfiles(usuario.getPerfiles());
	    usuarioBD.setSaludo(usuario.getSaludo());
	    usuarioBD.setFirma(usuario.getFirma());
	    
	    usuarioService.save(usuarioBD);
	    attributes.addFlashAttribute("success", "Usuario actualizado con éxito");
	    return ViewRouteHelper.USUARIO_REDIRECT_LISTA;
	}



	@GetMapping("lista/cambiar-contrasena/{id}")
	public String cambiarContrasenaForm(@PathVariable("id") long id, Model model, Principal principal) {
	    Usuario usuario = usuarioService.buscar(id);
	    Usuario usuarioLogueado = usuarioService.getByUsername(principal.getName());
	    boolean esAdministrador = usuarioLogueado.getPerfiles().getRol().equalsIgnoreCase("Administrador");
	    if (!esAdministrador && usuarioLogueado.getId() != id) {
	        throw new AccessDeniedException("No tienes permiso para cambiar la contraseña de otro usuario");
	    }
	    model.addAttribute("titulo", "Cambiar Contraseña");
	    model.addAttribute("usuario", usuario);
	    return ViewRouteHelper.USUARIO_PASSWORD; 
	}

	@PostMapping("lista/cambiar-contrasena/{id}")
	public String cambiarContrasena(@PathVariable("id") long id,
	                                @ModelAttribute Usuario usuario,
	                                BindingResult result,
	                                RedirectAttributes attributes, Principal principal) {

	    Usuario usuarioBD = usuarioService.buscar(id);
	    Usuario usuarioLogueado = usuarioService.getByUsername(principal.getName());
	    
	    boolean esAdministrador = usuarioLogueado.getPerfiles()
	            .getRol().equalsIgnoreCase("Administrador");
	    
	    if (usuario.getNuevaContrasena() != null && !usuario.getNuevaContrasena().isEmpty()) {
	        if (!passwordEncoder.matches(usuario.getContrasenaActual(), usuarioBD.getContrasena())) {
	            result.addError(new FieldError("usuario", "contrasenaActual", "Contraseña actual incorrecta"));
	        } else if (!usuario.getNuevaContrasena().equals(usuario.getConfirmarContrasena())) {
	            result.addError(new FieldError("usuario", "confirmarContrasena", "Las contraseñas no coinciden"));
	        } else {
	            usuarioBD.setContrasena(passwordEncoder.encode(usuario.getNuevaContrasena()));
	        }
	    }

	    if (result.hasErrors()) {
	    	return ViewRouteHelper.USUARIO_PASSWORD; 
	    }

	    usuarioService.save(usuarioBD);
	    attributes.addFlashAttribute("success", "Contraseña cambiada con éxito");
	    if (esAdministrador) {
	        return ViewRouteHelper.USUARIO_REDIRECT_LISTA;
	    } else {
	        return ViewRouteHelper.REDIRECT; 
	    }
	}

	@GetMapping("lista/delete/{id}")
	public String eliminar(@PathVariable("id") long id,RedirectAttributes attribute) {
		Usuario u = usuarioService.buscar(id);
		u.setDeshabilitado(false);
		usuarioService.save(u);
		
		List<PreguntaUsuario> preguntas = preguntaUsuarioService.findByUsuarioRespondiendoId(id);
		for (PreguntaUsuario p : preguntas) {
	        p.setUsuarioRespondiendo(null);
	        p.setFijada(false);
	    }
		preguntaUsuarioService.saveAll(preguntas);
		
		System.out.println("Registro eliminado con exito");
		attribute.addFlashAttribute("warning","Usuario eliminado con exito");
		return ViewRouteHelper.ROUTE_REDIRECT;
	}
	
	@GetMapping("lista/cambiar-saludo-firma/{id}")
	public String cambiarSaludoFirmaForm(@PathVariable("id") long id, Model model, Principal principal) {

	    Usuario usuario = usuarioService.buscar(id);
	    Usuario usuarioLogueado = usuarioService.getByUsername(principal.getName());
	    boolean esAdministrador = usuarioLogueado.getPerfiles().getRol().equalsIgnoreCase("Administrador");

	    // Un operador SOLO puede modificar su propio saludo/firma
	    if (!esAdministrador && usuarioLogueado.getId() != id) {
	        throw new AccessDeniedException("No tienes permiso para editar el saludo/firma de otro usuario");
	    }

	    model.addAttribute("titulo", "Editar Saludo y Firma");
	    model.addAttribute("usuario", usuario);

	    return ViewRouteHelper.USUARIO_FIRMA_SALUDO;
	}

	
	@PostMapping("lista/cambiar-saludo-firma/{id}")
	public String guardarSaludoFirma(@PathVariable("id") Long id,
	                                 @ModelAttribute("usuario") Usuario usuarioForm,
	                                 BindingResult result,
	                                 RedirectAttributes redirect) {

	    Usuario usuarioBD = usuarioService.buscar(id);

      if (result.hasErrors()) {
    	  return ViewRouteHelper.USUARIO_FIRMA_SALUDO;
	    }

	    usuarioBD.setSaludo(usuarioForm.getSaludo());
	    usuarioBD.setFirma(usuarioForm.getFirma());
	    usuarioService.save(usuarioBD);

	    redirect.addFlashAttribute("success", "Saludo y firma actualizados correctamente.");
	    return ViewRouteHelper.REDIRECT;
	}
	
	@PostMapping("lista/restablecer-contrasena/{id}")
	public String restablecerContrasena(@PathVariable("id") long id,
	                                    RedirectAttributes attributes,
	                                    Principal principal) {

	    Usuario usuario = usuarioService.buscar(id);
	    Usuario usuarioLogueado = usuarioService.getByUsername(principal.getName());

	    boolean esAdministrador = usuarioLogueado.getPerfiles()
	            .getRol().equalsIgnoreCase("Administrador");

	    if (!esAdministrador && usuarioLogueado.getId() != id) {
	        throw new AccessDeniedException("No tienes permiso para esta acción");
	    }

	    String contrasenaTemporal = generarContrasenaTemporal();

	    usuario.setContrasena(passwordEncoder.encode(contrasenaTemporal));
	    usuarioService.save(usuario);

	    String asunto = "Restablecimiento de contraseña";
	    String cuerpo =
	            "Hola " + usuario.getNombre() + ",\n\n" +
	            "Tu contraseña ha sido restablecida correctamente.\n\n" +
	            "Contraseña temporal: " + contrasenaTemporal + "\n\n" +
	            "Te recomendamos ingresar al sistema y cambiarla a la brevedad.\n\n" +
	            "Saludos.\nPreguntame UNLa";

	    emailService.enviarCorreo(
	            usuario.getCorreoElectronico(),
	            asunto,
	            cuerpo
	    );

	    attributes.addFlashAttribute(
	            "success",
	            "La contraseña fue restablecida y enviada por correo electrónico"
	    );

	    return ViewRouteHelper.USUARIO_REDIRECT_LISTA;
	}
	
	private String generarContrasenaTemporal() {
	    return UUID.randomUUID()
	            .toString()
	            .replace("-", "")
	            .substring(0, 10);
	}
}