package com.chatbot.unla.entities.model;

import java.io.Serializable;

public class ConflictoBC implements Serializable {
    private static final long serialVersionUID = 1L;

    private String pregunta;
    private String respuestaExistente;
    private String respuestaNueva;


    public ConflictoBC() { }


	public ConflictoBC(String pregunta, String respuestaExistente, String respuestaNueva) {
		super();
		this.pregunta = pregunta;
		this.respuestaExistente = respuestaExistente;
		this.respuestaNueva = respuestaNueva;
	}


	public String getPregunta() {
		return pregunta;
	}


	public void setPregunta(String pregunta) {
		this.pregunta = pregunta;
	}


	public String getRespuestaExistente() {
		return respuestaExistente;
	}


	public void setRespuestaExistente(String respuestaExistente) {
		this.respuestaExistente = respuestaExistente;
	}


	public String getRespuestaNueva() {
		return respuestaNueva;
	}


	public void setRespuestaNueva(String respuestaNueva) {
		this.respuestaNueva = respuestaNueva;
	}


	public static long getSerialversionuid() {
		return serialVersionUID;
	}

    
}