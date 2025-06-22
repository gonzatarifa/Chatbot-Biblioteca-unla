package com.chatbot.unla.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotEmpty;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter @Setter @NoArgsConstructor
@Table(name = "baseDeConocimiento", uniqueConstraints = {@UniqueConstraint(columnNames = {"pregunta"})})
public class BaseDeConocimiento {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	
	@Column(name = "pregunta")
	@NotEmpty(message="el campo no debe estar vacio") 
	private String pregunta;
	
	@Column(name = "respuesta")
	@NotEmpty(message="el campo no debe estar vacio") 
	private String respuesta;

	public BaseDeConocimiento(long id, String pregunta, String respuesta) {
		super();
		this.id = id;
		this.pregunta = pregunta;
		this.respuesta = respuesta;
	}

	public BaseDeConocimiento(String pregunta, String respuesta) {
		super();
		this.pregunta = pregunta;
		this.respuesta = respuesta;
	}
	
}
