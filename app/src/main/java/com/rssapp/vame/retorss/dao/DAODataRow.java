package com.rssapp.vame.retorss.dao;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;

import com.rssapp.vame.retorss.bean.DataRow;
import com.rssapp.vame.retorss.utils.Compresor;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.rssapp.vame.retorss.dao.DatabaseOpenHelper.COL_TITULO;
import static com.rssapp.vame.retorss.dao.DatabaseOpenHelper.FTS_RSS_VIRTUAL_TABLE;

/**
 * Objeto de acceso a datos para la gestion
 * de una base de datos.
 * En este caso es para la tabla de filas RSS
 */
public class DAODataRow {

    /** Clase de logging */
    private static final Logger log = Logger.getLogger(DAODataRow.class.getName());

    /** Clase de apoyo para manejar los datos */
    private final DatabaseOpenHelper mDatabaseOpenHelper;

    public DAODataRow(Context context) {
        mDatabaseOpenHelper = new DatabaseOpenHelper(context);
    }

    /**
     * Prepara una consulta a la tabla construida
     * @param query Consulta con la informacion
     * @param columns Columas a consultar
     * @return Cursor con los datos de salida
     */
    public List<DataRow> buscarCoincidencias(String query, String[] columns) {
        String selection = COL_TITULO + " MATCH ?";
        String[] selectionArgs = new String[] {"*" +query+"*"};

        Cursor cursor = mDatabaseOpenHelper.query(selection, selectionArgs, columns, FTS_RSS_VIRTUAL_TABLE);
        return cursor2DataRows(cursor);
    }

    /**
     * Ejecuta una consulta para obtener todos los registros
     * de la tabla observada
     * @return
     */
    public List<DataRow> obtenerTodo(){
        Cursor cursor = mDatabaseOpenHelper.query(null, null, null, FTS_RSS_VIRTUAL_TABLE);
        return cursor2DataRows(cursor);
    }

    /**
     * Genera un cursor para la tabla de registros
     * @param cursor Cursor con el contenido de la consulta
     * @return Listado de DataRows, en caso de no existir alguna
     * retorna una lista vacia
     */
    public List<DataRow> cursor2DataRows(Cursor cursor){
        List<DataRow> filas = new ArrayList<>();
        if(cursor == null){
            return filas;
        }
        // Procesando el cursor
        for(cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            log.log(Level.INFO, "-> " +filas.size());
            DataRow dataRow = new DataRow();
            dataRow.setTitulo(cursor.getString(0)); // TITULO
            dataRow.setLink(cursor.getString(1)); // LINK
            dataRow.setDescripcion(cursor.getString(2)); // DESCRIPCION
            dataRow.setFechaPub(cursor.getString(3)); // PUB_DATE
            dataRow.setPath(cursor.getString(4)); // IMG
            // Cargando cadena de texto a bitmap
            Bitmap bitmap = Compresor.cargarImg(dataRow.getPath());
            dataRow.setImg(bitmap);

            // Se agrega a la lista
            filas.add(dataRow);
        }
        return filas;
    }

    /**
     * Reconstruye una base de datos
     */
    public void truncarTabla(){
        mDatabaseOpenHelper.reconstruirTabla();
    }

    /**
     * Agrega un registro a la tabla
     */
    public void agregarATabla(DataRow dataRow){
        mDatabaseOpenHelper.addDataRow(dataRow);
    }

}
