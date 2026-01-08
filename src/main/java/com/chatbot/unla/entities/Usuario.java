package com.chatbot.unla.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.Digits;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import lombok.Data;
import lombok.NoArgsConstructor;


@Entity
@Data @NoArgsConstructor
@Table(name = "usuario", uniqueConstraints = {@UniqueConstraint(columnNames = {"documento", "correoElectronico", "nombreDeUsuario"})})

public class Usuario {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@Column(name = "nombre")
	@NotEmpty(message="el campo no debe estar vacio") 
	@Size(max = 100)
	private String nombre;

	@Column(name = "apellido")
	@NotEmpty(message="el campo no debe estar vacio") 
	@Size(max = 100)
	private String apellido;
	
	@Column(name = "tipoDocumento")
	private String tipoDocumento;

	@Column(name = "documento")
	@NotNull
	@Digits(integer = 18, fraction = 0)
	private long documento;

	@Column(name = "correoElectronico")
	@Email
	@NotEmpty(message="el campo no debe estar vacio") 
	@Size(max = 100)
	private String correoElectronico;

	@Column(name = "nombreDeUsuario")
	@NotEmpty(message="el campo no debe estar vacio") 
	@Size(max = 100)
	private String nombreDeUsuario;

	@Column(name = "contrasena")
	@Size(max = 200)
	private String contrasena;

	@ManyToOne
	@JoinColumn(name = "perfiles_id")
	private Perfiles perfiles;

	@Column(name = "deshabilitado")
	private boolean deshabilitado;
	
	@Column(name = "saludo")
	@Size(max = 200)
	private String saludo;

	@Column(name = "firma")
	@Size(max = 200)
	private String firma;
	
	@Transient
	private String contrasenaActual;
	@Transient
	private String nuevaContrasena;
	@Transient
	private String confirmarContrasena;

	public Usuario(long id, String nombre, String apellido, String tipoDocumento, int documento,
			String correoElectronico, String nombreDeUsuario, String contrasena) {
		super();
		this.id = id;
		this.nombre = nombre;
		this.apellido = apellido;
		this.tipoDocumento = tipoDocumento;
		this.documento = documento;
		this.correoElectronico = correoElectronico;
		this.nombreDeUsuario = nombreDeUsuario;
		this.contrasena = contrasena;

	}

	public Usuario(String nombre, String apellido, String tipoDocumento, int documento, String correoElectronico,
			String nombreDeUsuario, String contrasena) {
		super();
		this.nombre = nombre;
		this.apellido = apellido;
		this.tipoDocumento = tipoDocumento;
		this.documento = documento;
		this.correoElectronico = correoElectronico;
		this.nombreDeUsuario = nombreDeUsuario;
		this.contrasena = contrasena;

	}
	
}