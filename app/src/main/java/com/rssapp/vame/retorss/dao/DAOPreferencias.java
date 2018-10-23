package com.rssapp.vame.retorss.dao;


import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.rssapp.vame.retorss.bean.Preferencias;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static com.rssapp.vame.retorss.dao.DatabaseOpenHelper.FTS_PREF_VIRTUAL_TABLE;

/**
 * Carga las preferencias de una
 * tabla virtual
 */
public class DAOPreferencias {

    /** Clase de logging */
    private static final Logger log = Logger.getLogger(DAOPreferencias.class.getName());

    /** Clase de apoyo para manejar los datos */
    private final DatabaseOpenHelper mDatabaseOpenHelper;

    /**
     * Inicializa la conexion a la base de datos
     * @param context contexto de la aplicacion
     */
    public DAOPreferencias(Context context) {
        mDatabaseOpenHelper = new DatabaseOpenHelper(context);
    }

    /**
     * Agrega una preferencia a la tabla
     * @param preferencias Preferencia a agregar
     * @return long indicando row inserted
     */
    public long agregarPreferencia(Preferencias preferencias) {
        return mDatabaseOpenHelper.addPreferencia(preferencias);
    }

    /**
     * Elimina una preferencia de la tabla
     * @param preferencia Preferencia a eliminar
     * @return rows afected
     */
    public long eliminarPreferencia(Preferencias preferencia){
        return mDatabaseOpenHelper.eliminarPreferencia(preferencia);
    }

    /**
     * Carga las preferencias de una tabla
     * @return Listado de preferencias
     */
    public List<Preferencias> cargarPreferencias() {
        Cursor cursor = mDatabaseOpenHelper.query(null, null, null, FTS_PREF_VIRTUAL_TABLE);
        return cursor2Preferencias(cursor);
    }

    /**
     * Genera un cursor para la tabla de registros
     * @param cursor Cursor con el contenido de la consulta
     * @return Listado de Preferencias, en caso de no existir alguna
     * retorna una lista vacia
     */
    public List<Preferencias> cursor2Preferencias(Cursor cursor) {
        List<Preferencias> filas = new ArrayList<>();
        if(cursor == null){
            return filas;
        }
        // Procesando el cursor
        List<Preferencias> lista = new ArrayList<Preferencias>();
        for(cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            Log.d("myTag", "This is my message");
            Preferencias pref = new Preferencias();
            pref.setUid(cursor.getString(0));
            pref.setNombre(cursor.getString(1));
            pref.setUrl(cursor.getString(2));
            lista.add(pref);
        }
        return lista;
    }

}
