package com.chatbot.unla.entities.model;

import java.io.Serializable;

public class MensajeChat implements Serializable {
    private static final long serialVersionUID = 1L;

    private String remitente;
    private String contenido;

    public MensajeChat() { }

    public MensajeChat(String remitente, String contenido) {
        this.remitente = remitente;
        this.contenido = contenido;
    }

    // getters y setters
    public String getRemitente() { return remitente; }
    public void setRemitente(String remitente) { this.remitente = remitente; }

    public String getContenido() { return contenido; }
    public void setContenido(String contenido) { this.contenido = contenido; }
}