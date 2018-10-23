package com.rssapp.vame.retorss.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.rssapp.vame.retorss.R;

/**
 * Clase de apoyo para gestionar los
 * datos obtenidos por la herencia
 */
public class PreferencesManager {

    private Context context;

    private SharedPreferences mPrefs;
    private SharedPreferences.Editor editor;
    public PreferencesManager(Context cxt){
        context = cxt;
        mPrefs = context.getSharedPreferences(context.getString(R.string.archivo_preferencias), Context.MODE_PRIVATE);
        editor = mPrefs.edit();
    }

    /**
     * Establece el valor para una preferencia de
     * tipo int
     * @param clave Nombre de la preferencia getString(R.string.clave)
     * @param valor Valor a ingresar
     */
    public void setPreferenciaInt(String clave, int valor){
        editor.putInt(clave, valor);
        editor.apply();
    }

    /**
     * Establece el valor para una preferencia de
     * tipo String
     * @param clave Nombre de la preferencia getString(R.string.clave)
     * @param valor Valor a ingresar
     */
    public void setPreferenciaString(String clave, String valor){
        editor.putString(clave, valor);
        editor.apply();
    }

    /**
     * Obtiene una preferencia de tipo String
     * @param clave Clave a obtener getString(R.string.clave)
     * @return Cadena de texto con el valor, por default espacio en blanco
     */
    public String getPreferenciaString(String clave){
        return mPrefs.getString(clave, "");
    }

    /**
     * Obtiene una preferencia de tipo String
     * @param clave Clave a obtener getString(R.string.clave)
     * @return Entero establecido, por default 0
     */
    public int getPreferenciaInt(String clave){
        return mPrefs.getInt(clave, 0);
    }

}
