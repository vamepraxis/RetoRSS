package com.rssapp.vame.retorss.bean;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * Bitmap serializable que permite
 * hacer el transporte de imagenes
 * entre actividades.
 */
public class BitmapSerializable implements Serializable {

    /** Serializable UID */
    private static final long serialVersionUID = -6298516694275121291L;

    /** Bitmap que se mantendra aqui para evitar excepciones */
    transient Bitmap bitmap;

    /** Valores default para bitmaps vacios */
    public static int WIDTH_PX = 151;
    public static int HEIGHT_PX = 151;

    public BitmapSerializable(){
        // Vacio
        defaultBitmap();
    }

    public BitmapSerializable(Bitmap b){
        // Manejo de nulos
        if(b == null){
            defaultBitmap();
        } else {
            bitmap = b;
        }
    }

    /**
     * Escribe el objeto de forma serializada para
     * que pueda ser transportado entre actividades
     * @param oos Flujo de salida que se envia a la
     *            siguiente activdad
     * @throws IOException En caso de que no pueda ser
     *            leido el flujo
     */
    private void writeObject(ObjectOutputStream oos) throws IOException{
        // Serializara los objetos que no estan marcados como transitorios
        oos.defaultWriteObject();
        // Serializando el bitmap
        if(bitmap!=null){
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            boolean success = bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteStream);
            if(success){
                oos.writeObject(byteStream.toByteArray());
            }
        }
    }

    /**
     * Lee el objeto para serializar la informacion
     * @param ois Flujo que se introduce al serializar
     * @throws IOException En caso de que no se pueda
     *              establecer la comunicacion
     * @throws ClassNotFoundException En caso de que la
     *              clase no exista
     */
    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException{
        // Leyendo la informacion
        ois.defaultReadObject();
        // Obteniendo los bytes del bitmap
        byte[] image = (byte[]) ois.readObject();
        if(image != null && image.length > 0){
            bitmap = BitmapFactory.decodeByteArray(image, 0, image.length);
        }
    }

    /**
     * Obtiene el bitmap
     * @return Bitmap a usar
     */
    public Bitmap getBitmap() {
        return bitmap;
    }

    /**
     * Establece el bitmap a utilizar
     * @param bitmap Bitmap de uso rudo
     */
    public void setBitmap(Bitmap bitmap) {
        // Manejo de nulos
        if(bitmap == null){
            defaultBitmap();
        } else {
            this.bitmap = bitmap;
        }
    }

    /**
     * Obtiene el bitmap
     * @return Bitmap a usar
     */
    public void defaultBitmap() {
        Bitmap.Config conf = Bitmap.Config.ARGB_8888; // see other conf types
        this.bitmap = Bitmap.createBitmap(WIDTH_PX, HEIGHT_PX, conf);
    }

}
