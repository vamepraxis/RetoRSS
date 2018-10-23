package com.rssapp.vame.retorss.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Comprime imagenes muy grandes
 * en android
 */
public class Compresor {

    /** Logger de la clase */
    private static final Logger LOGGER = Logger.getLogger(Compresor.class.getName());

    private Compresor(){
        // Vacio
    }

    public static Bitmap comprimir(Bitmap fuente){
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        fuente.compress(Bitmap.CompressFormat.PNG, 100, out);
        return BitmapFactory.decodeStream(new ByteArrayInputStream(out.toByteArray()));
    }

    /**
     * Almacena una imagen con compresion
     * @param bitmap Imagen a guardar
     * @param ruta Ruta donde se guardara la imagen
     * @return True en caso de almacenar la imagen, sino False
     */
    public static boolean saveImg(Bitmap bitmap, String ruta){
        try (FileOutputStream out = new FileOutputStream(ruta)) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            return true;
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "" + e);
        }
        return false;
    }

    /**
     * Carga un bitmap de una imagen
     * @param ruta de donde se cargara el mapa
     * @return El mapa de bits o nulo
     */
    public static Bitmap cargarImg(String ruta){
        File f = new File(ruta);
        try (FileInputStream stream = new FileInputStream(f)) {
            return BitmapFactory.decodeStream(stream);
        } catch(IOException e){
            LOGGER.log(Level.SEVERE, "" + e);
        }
        return null;
    }

    /**
     * Borra el contenido de un directorio
     * @param ruta Ruta que se usara para borrar sus
     *             contenidos
     */
    public static void borrarDirectorio(String ruta){
        File dir = new File(ruta);
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                new File(dir, children[i]).delete();
            }
        }
    }
}
