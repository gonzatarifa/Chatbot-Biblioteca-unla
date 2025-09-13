# 🤖 Chatbot biblioteca UNLa 

Este proyecto es un **Servicio de referencia virtual** desarrollado con **Spring Boot** y herramientas de IA para gestionar una **base de conocimiento**.  
Incluye carga manual de preguntas/respuestas, carga por archivo (CSV/Excel), y soporte de embeddings para búsquedas semánticas.

---

## 🚀 Tecnologías utilizadas
- **Spring Boot** (Spring Tools Suite / Spring Tools 4)
- **MySQL Workbench 8.5**
- **Ollama** con modelos:
  - `nomic-embed-text` → para generar embeddings vectoriales.
  - `qwen3:0.6b` → para normalizar las preguntas ingresadas.
- **Thymeleaf** para vistas.
- **Bootstrap 5** para el frontend.
- **Spring Mail** para notificaciones por correo (requiere contraseña de aplicación).

---

## ⚙️ Requisitos previos
1. **Java 17** o superior.  
2. **Spring Tools Suite (STS) / Spring Tools 4** o IntelliJ IDEA con soporte para Spring Boot.  
3. **MySQL Workbench 8.5** (crear base de datos).  
4. **Ollama** instalado → [Descargar aquí](https://ollama.ai).  
   - Asegurarse de tener descargados los modelos:
     ```bash
     ollama pull nomic-embed-text
     ollama pull qwen3:0.6b
     ```
5. **Configuración de correo**: se debe generar una **contraseña de aplicación** desde tu proveedor (ej. Gmail, Outlook) para que la aplicación pueda enviar correos.

---

## 🗄️ Configuración de Base de Datos
1. Iniciar **MySQL Workbench**.
2. Crear la base de datos:
   ```sql
   CREATE DATABASE  IF NOT EXISTS `Chatbot-Biblioteca-unla`;
