package com.rssapp.vame.retorss.bean;

import android.graphics.Bitmap;

import java.io.Serializable;
import java.util.UUID;

/**
 * Fila del RSS feed para que obtendra la informacion.
 * En ella se almacenaran los datos obtenidos de cualquier
 * RSS Feed cuyo formato cumpla con la version 2.0 de RSS.
 */
public class DataRow implements Serializable {

    private String id;

    /* Titulo del feed */
    private String titulo;

    /* URL del enlace de la pagina */
    private String link;

    /* Descripcion de la pagina */
    private String descripcion;

    /* Fecha de publicacion de la noticia */
    private String fechaPub;

    /* Imagen */
    private BitmapSerializable img;
    private String path;

    public DataRow(){
        this.img = new BitmapSerializable();
        // Vacio, sin ningun uso
        this.id = UUID.randomUUID().toString();
    }

    /**
     * Obtiene el titulo
     * @return Cadena con el titulo
     */
    public String getTitulo() {
        return titulo;
    }

    /**
     * Establece un valor para el titulo
     * @param titulo Valor a establecer
     */
    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    /**
     * Obtiene la informacion de la URL
     * @return Cadena con la URL
     */
    public String getLink() {
        return link;
    }

    /**
     * Retorna la informacion de la URL
     * @param link URL en cadena de texto
     */
    public void setLink(String link) {
        this.link = link;
    }

    /**
     * Obtiene la descripcion del feed
     * @return Cadena de texto con la descripcion
     */
    public String getDescripcion() {
        return descripcion;
    }

    /**
     * Establece la descripcion del feed
     * @param descripcion Texto a establecer
     */
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    /**
     * Obtiene la fecha de la publicacion
     * @return Fecha de la publicacion
     */
    public String getFechaPub() {
        return fechaPub;
    }

    /**
     * Establece la fecha de la publicacion
     * @param fechaPub Fecha a establecer
     */
    public void setFechaPub(String fechaPub) {
        this.fechaPub = fechaPub;
    }

    /**
     * Obtiene la imagen asignada al objeto
     * @return Mapa de bits con la imagen obtenida
     */
    public Bitmap getImg() {
        return img.getBitmap();
    }

    /**
     * Almacena la imagen que se cargara posteriormente
     * @param img Imagen a cargar
     */
    public void setImg(Bitmap img) {
        this.img.setBitmap(img);
    }

    /**
     * Establece la ruta para la imagen
     * @return Cadena de texto con la imagen
     */
    public String getPath() {
        return path;
    }

    /**
     * Establece la ruta para la imagen
     * @param path Ruta con la imagen
     */
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * Obtiene el ID de la aplicacion
     * @return String con el id unico
     */
    public String getId(){
        return id;
    }
}
