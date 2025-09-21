package com.chatbot.unla.helpers;

import java.io.Serializable;

public class FeedbackSession implements Serializable {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String nombre;
    private String apellido;
    private String email;
    private String pregunta;

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPregunta() { return pregunta; }
    public void setPregunta(String pregunta) { this.pregunta = pregunta; }
}
