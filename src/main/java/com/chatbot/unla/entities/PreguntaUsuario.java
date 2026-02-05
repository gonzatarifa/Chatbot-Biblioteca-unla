package com.chatbot.unla.entities;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter @Setter @NoArgsConstructor
@Table(name = "pregunta_usuario")
public class PreguntaUsuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "pregunta", length = 500) 
    @NotEmpty(message = "El campo pregunta no debe estar vacío")
    private String pregunta;

    @Column(name = "nombre")
    private String nombre;

    @Column(name = "apellido")
    private String apellido;

    @Column(name = "email")
    @Email(message = "Debe ser un email válido")
    @NotEmpty(message = "El campo email no debe estar vacío")
    private String email;

    @Column(name = "fecha_envio_pregunta")
    private LocalDateTime fechaEnvioPregunta;

    @Column(name = "fecha_envio_respuesta")
    private LocalDateTime fechaEnvioRespuesta;

    @Column(name = "habilitado")
    private boolean habilitado;

    @Column(name = "respuesta_enviada")
    private boolean respuestaEnviada;
    
    @ManyToOne
    @JoinColumn(name = "usuario_respondio_id")
    private Usuario usuarioRespondio;
    
    @Column(name = "fijada", nullable = false)
    private boolean fijada = false;
    
    @ManyToOne
    @JoinColumn(name = "usuario_respondiendo_id")
    private Usuario usuarioRespondiendo;

    public PreguntaUsuario(String pregunta, String nombre, String apellido, String email,
                           LocalDateTime fechaEnvioPregunta, LocalDateTime fechaEnvioRespuesta,
                           boolean habilitado, boolean respuestaEnviada) {
        this.pregunta = pregunta;
        this.nombre = nombre;
        this.apellido = apellido;
        this.email = email;
        this.fechaEnvioPregunta = fechaEnvioPregunta;
        this.fechaEnvioRespuesta = fechaEnvioRespuesta;
        this.habilitado = habilitado;
        this.respuestaEnviada = respuestaEnviada;
    }
}
