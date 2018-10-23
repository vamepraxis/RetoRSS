package com.rssapp.vame.retorss.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.graphics.Bitmap;
import android.util.Log;

import com.rssapp.vame.retorss.bean.DataRow;
import com.rssapp.vame.retorss.bean.Preferencias;

import java.io.ByteArrayOutputStream;
import java.util.logging.Logger;

/**
 * Clase unica que contiene la informacion de los registros RSS
 */
public class DatabaseOpenHelper extends SQLiteOpenHelper {

    /** Clase de logging */
    private static final Logger log = Logger.getLogger(DatabaseOpenHelper.class.getName());

    private final Context mHelperContext;

    /** Base de datos */
    private static final String TAG = "RSSDatabase";
    /** Conector */
    private static SQLiteDatabase mDatabase;

    // Columnas de la tabla
    public static final String COL_TITULO = "TITULO";
    public static final String COL_LINK = "LINK";
    public static final String COL_DESCRIPCION = "DESCRIPTION";
    public static final String COL_PUB_DATE= "PUBDATE";
    public static final String COL_IMG = "IMAGEN";
    // Tabla preferencias
    public static final String COL_NOMBRE = "NOMBRE";
    public static final String COL_URL = "URL";
    public static final String COL_ID = "ID";

    // Metadatos de la tabla
    public static final String DATABASE_NAME = "RSS";
    public static final String FTS_RSS_VIRTUAL_TABLE = "RSSRow";
    public static final String FTS_PREF_VIRTUAL_TABLE = "PREFERENCIAS";
    private static final String PRIMARY_KEY = " PRIMARY KEY ";
    private static final int DATABASE_VERSION = 1;

    private static final String FTS_TABLE_CREATE =
            "CREATE VIRTUAL TABLE " + FTS_RSS_VIRTUAL_TABLE +
                    " USING fts3 (" +
                    COL_TITULO + ", " +
                    COL_LINK + ", " +
                    COL_DESCRIPCION + ", " +
                    COL_PUB_DATE + ", " +
                    COL_IMG + ")";

    private static final String PREF_TABLE_CREATE =
            "CREATE VIRTUAL TABLE " + FTS_PREF_VIRTUAL_TABLE +
                    " USING fts3 (" +
                    COL_ID  + PRIMARY_KEY +" , " +
                    COL_NOMBRE + ", " +
                    COL_URL + ")";

    public DatabaseOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        mHelperContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        mDatabase = db;
        mDatabase.execSQL(FTS_TABLE_CREATE);
        mDatabase.execSQL(PREF_TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(TAG, "Actualizando la base de datos, version " + oldVersion + " a la version "
                + newVersion + ", se destruiran los datos");
        db.execSQL("DROP TABLE IF EXISTS " + FTS_RSS_VIRTUAL_TABLE);
        onCreate(db);
    }

    /**
     * Ejecuta una consulta a la base de datos
     * @param selection Consulta a realizar
     * @param selectionArgs Informacion a obtener
     * @param columns Columnas a buscar
     * @return Cursor con la informacion obtenida
     */
    public Cursor query(String selection, String[] selectionArgs, String[] columns, String table) {
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        builder.setTables(table);

        Cursor cursor = builder.query(this.getReadableDatabase(),
                columns, selection, selectionArgs, null, null, null);

        if (cursor == null) {
            return null;
        } else if (!cursor.moveToFirst()) {
            cursor.close();
            return null;
        }
        return cursor;
    }

    /**
     * Agrega una fila de los registros obtenidos a la
     * tabla virtual
     * @param dataRow Se encarga del trabajo pesado
     * @return entero mostrando el resultado
     */
    public long addDataRow(DataRow dataRow) {
        if (mDatabase == null){
            mDatabase = getWritableDatabase();
        }
        ContentValues initialValues = new ContentValues();
        initialValues.put(COL_TITULO, dataRow.getTitulo());
        initialValues.put(COL_LINK, dataRow.getLink());
        initialValues.put(COL_DESCRIPCION, dataRow.getDescripcion());
        initialValues.put(COL_PUB_DATE, dataRow.getFechaPub());
        initialValues.put(COL_IMG, dataRow.getPath());

        return mDatabase.insert(FTS_RSS_VIRTUAL_TABLE, null, initialValues);
    }

    /**
     * Agrega una preferencia a la tabla
     * @param preferencias Preferencias a insertar
     * @return rows afected
     */
    public long addPreferencia(Preferencias preferencias){
        if (mDatabase == null){
            mDatabase = getWritableDatabase();
        }
        ContentValues initialValues = new ContentValues();
        initialValues.put(COL_ID, preferencias.getUid());
        initialValues.put(COL_NOMBRE, preferencias.getNombre());
        initialValues.put(COL_URL, preferencias.getUrl());

        return mDatabase.insert(FTS_PREF_VIRTUAL_TABLE, null, initialValues);
    }

    /**
     * Elimina una preferencia de la tabla
     * @return rows eliminadas en la consulta
     */
    public long eliminarPreferencia(Preferencias preferencia){
        if(mDatabase == null) {
            mDatabase = getWritableDatabase();
        }
        String where = COL_NOMBRE + " = ? AND " + COL_URL + " = ?  AND " + COL_ID + " = ? ";
        String[] args = {preferencia.getNombre(), preferencia.getUrl(), preferencia.getUid()};
        return mDatabase.delete(FTS_PREF_VIRTUAL_TABLE, where, args);
    }

    /**
     * Reconstruye una tabla en caso de que sea
     * necesario.
     */
    public void reconstruirTabla(){
        if (mDatabase == null){
            mDatabase = getWritableDatabase();
        }
        mDatabase.execSQL("DROP TABLE IF EXISTS " + FTS_RSS_VIRTUAL_TABLE);
        mDatabase.execSQL(FTS_TABLE_CREATE);
    }

    /**
     * Convierte una imagen a un bitmap
     * @param bitmap Bitmap a transformar
     * @return Cadena de texto de dicho bitmap
     */
    private byte[] bitMapToBytes(Bitmap bitmap) {
        if(bitmap != null) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
            return baos.toByteArray();
        } else {
            return null;
        }

    }
}
