package com.rssapp.vame.retorss.bean;

import java.io.Serializable;

/**
 * Listado de preferencias para el usuario.
 * Mostrara la informacion que ha dado de
 * alta en la aplicacion.
 */
public class Preferencias implements Serializable {

    /**
     * Nombre
     */
    private String nombre;

    /**
     * URL del repositorio
     */
    private String url;

    /**
     * Identificador
     */
    private String uid;

    /**
     * Posicion en el listado
     */
    private int posicion;

    public Preferencias(){
        //Vacio
    }


    /**
     * Retorna el nickname que se definio
     * para la fuente de datos
     * @return Nombre del repositorio
     */
    public String getNombre() {
        return nombre;
    }

    /**
     * Define un nickname para la
     * fuente que se da de alta
     * @param nombre Cadena de texto con el nombre
     */
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    /**
     * Obtiene la url que se mostrara
     * @return Cadena de texto con la url
     */
    public String getUrl() {
        return url;
    }

    /**
     * Se establece la url que se mostrara
     * @param url cadena de texto con la url
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * Obtiene la posicion en la lista sobre
     * la que esta guardado
     * @return Listado por posicion
     */
    public int getPosicion() {
        return posicion;
    }

    /**
     * Establece la posicion en la lista sobre
     * la que esta almacenado
     * @param posicion Posicion
     */
    public void setPosicion(int posicion) {
        this.posicion = posicion;
    }

    /**
     * Obtiene el id unico para este registro
     * @return Cadena de texto con el id
     */
    public String getUid() {
        return uid;
    }

    /**
     * Establece el valor del id para este registro
     * @param uid ID a establecer
     */
    public void setUid(String uid) {
        this.uid = uid;
    }

}
