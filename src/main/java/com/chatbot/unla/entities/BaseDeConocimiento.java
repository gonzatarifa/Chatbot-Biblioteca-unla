package com.chatbot.unla.entities;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotEmpty;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

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
	
    @Lob
    @Column(name = "embedding", columnDefinition = "TEXT")
    private String embedding;
    
    @CreationTimestamp
	@Column(name = "fecha_creacion", updatable = false)
	private LocalDateTime fechaCreacion;

	@UpdateTimestamp
	@Column(name = "fecha_actualizacion")
	private LocalDateTime fechaActualizacion;

	@Column(name = "habilitado", nullable = false)
	private boolean habilitado = true;

	public BaseDeConocimiento(long id, String pregunta, String respuesta, String embedding) {
		super();
		this.id = id;
		this.pregunta = pregunta;
		this.respuesta = respuesta;
		this.embedding = embedding;
	}

	public BaseDeConocimiento(String pregunta, String respuesta, String embedding) {
		super();
		this.pregunta = pregunta;
		this.respuesta = respuesta;
		this.embedding = embedding;
	}

	public BaseDeConocimiento(long id, @NotEmpty(message = "el campo no debe estar vacio") String pregunta,
			@NotEmpty(message = "el campo no debe estar vacio") String respuesta, String embedding,
			LocalDateTime fechaCreacion, LocalDateTime fechaActualizacion, boolean habilitado) {
		super();
		this.id = id;
		this.pregunta = pregunta;
		this.respuesta = respuesta;
		this.embedding = embedding;
		this.fechaCreacion = fechaCreacion;
		this.fechaActualizacion = fechaActualizacion;
		this.habilitado = habilitado;
	}
	
}
