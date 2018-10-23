package com.rssapp.vame.retorss;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.rssapp.vame.retorss.bean.DataRow;
import com.rssapp.vame.retorss.bean.Preferencias;
import com.rssapp.vame.retorss.dao.DAODataRow;
import com.rssapp.vame.retorss.dao.DAOPreferencias;
import com.rssapp.vame.retorss.utils.Compresor;
import com.rssapp.vame.retorss.utils.PreferenciasAdapter;
import com.rssapp.vame.retorss.utils.WebUtil;

import java.io.File;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * En esta actividad se elige la URL que se utiliza en el sistema
 */
public class PreferencesActivity extends AppCompatActivity {

    /** Contexto de la aplicacion */
    final Context context = this;

    /**
     * Cajas de texto editables
     */
    private EditText nombre;
    private EditText url;

    /** Acceso a base de datos SQLite*/
    private DAOPreferencias pref = new DAOPreferencias(this);

    /** List view principal */
    private ListView lv;

    /** Url elegida */
    private String urlActual;

    /** bandera que indicara si es necesario recargar la lista */
    private int cambio = 0;

    /** Adaptador de la lista */
    private PreferenciasAdapter preferenciasAdapter;

    /** Dialogo para agregar preferencias en la pantalla */
    private Dialog dialog;

    /** Listado de preferencias */
    List<Preferencias> preferencias;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.preferencias);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Get reference of widgets from XML layout
        lv = (ListView) findViewById(R.id.prefList);

        preferencias = cargarPreferencias();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        preferenciasAdapter = new PreferenciasAdapter(preferencias,this);
        preferenciasAdapter.setBuilder(builder);
        lv.setAdapter( preferenciasAdapter );
    }

    /**
     * boton agregara en menu
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.mnu_pref, menu);
        return true;
    }

    /**
     * Opc Menu Horizontal Preferencias
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.addpref:
                // Se invoca dialogo para agregar
                dialog = new Dialog(context);
                dialog.setContentView(R.layout.form_add_pref);
                nombre = (EditText) dialog.findViewById(R.id.et_nombre);
                url = (EditText) dialog.findViewById(R.id.et_url);
                dialog.show();
                break;
            case android.R.id.home:
                Intent intent = new Intent(this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
                break;
        }
        return true;
    }

    /**
     * Invoca el modal que dara de alta las preferencias
     * @param v Vista para dar de alta
     */
    public void alta(View v) {
        Preferencias preferencia = new Preferencias();
        preferencia.setNombre( nombre.getText().toString() );
        preferencia.setUrl( url.getText().toString() );
        // Identificador unico
        preferencia.setUid(UUID.randomUUID().toString());
        validarFuente(preferencia);
    }

    /**
     * Posterior a la ejecucion de una tarea asincrona
     * se ejecuta este metodo para agregar una preferencia
     * a la base de datos y almacenar en memoria
     * @param preferencia Preferencia a almacenar
     */
    public void agregarRegistro(Preferencias preferencia){
        long cant = pref.agregarPreferencia(preferencia);
        if (cant > 0) {
            Toast.makeText(this, getString(R.string.pref_agregar_ok),
                    Toast.LENGTH_SHORT).show();
            // Agregando a la lista
            preferencias.add(preferencia);
            // Se actualizan las preferencias
            urlActual = preferencia.getUrl();
            cambio=0;
            guardarEnMemoria();
            // Se actualiza la lista
            preferenciasAdapter.notifyDataSetChanged();
            dialog.dismiss();
        }else {
            Toast.makeText(this, getString(R.string.pref_agregar_error),
                    Toast.LENGTH_SHORT).show();
        }
        nombre.setText("");
        url.setText("");
    }

    /**
     * Carga el listado de fuentes RSS dadas de
     * alta en la base de datos
     * @return Listado de fuentes RSS
     */
    private List<Preferencias> cargarPreferencias(){
        return pref.cargarPreferencias();
    }

    /**
     * Se guarda la informacion en memoria
     */
    private void guardarEnMemoria(){
        SharedPreferences sharedPref = this.getSharedPreferences(getString(R.string.archivo_preferencias),Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(getString(R.string.url_actual), urlActual);
        editor.putInt(getString(R.string.url_cambio), cambio);
        int posicion = preferencias.size()-1;
        editor.putInt(getString(R.string.selected), posicion);
        preferenciasAdapter.setPosicionSeleccionada(posicion);
        editor.apply();
    }

    /**
     * Verifica que la URL sea valida para el lector
     * de RSS.
     * @param fuente URL a validar
     */
    private void validarFuente(Preferencias fuente){
        RSSFeedTask rss = new RSSFeedTask();
        rss.setContext(context);
        rss.execute(fuente);
    }

    @Override
    public void onStop(){
        super.onStop();
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(getString(R.string.url_actual), urlActual);
        editor.putInt(getString(R.string.url_cambio), cambio);
        editor.putInt(getString(R.string.selected), preferencias.size()-1);
        editor.apply();
    }

    /**
     * Ejecuta el acceso a datos a traves de
     * una tarea asincrona. En este caso accede
     * a un servidor web para consumir a traves
     * de GET sus recursos.
     */
    public class RSSFeedTask extends AsyncTask<Preferencias, LinearLayout, List<DataRow>> {

        /** Logger de la aplicacion */
        private final Logger LOG = Logger.getLogger(MainActivity.DAORSSFeedTask.class.getName());

        /** Contexto para el manejo de la base de datos */
        private Context context;

        /** Dialogo de carga que invita al usuario a esperar */
        private ProgressDialog dialog;

        /** Base de datos con registros para consultas */
        DAODataRow db;

        /** Fuente que se cargara */
        Preferencias fuente;

        @Override
        protected void onPreExecute(){
            dialog = new ProgressDialog(context);
            this.dialog.setMessage(getString(R.string.pref_validando));
            this.dialog.setIndeterminate(true);
            this.dialog.setCancelable(false);
            this.dialog.setCanceledOnTouchOutside(false);
            this.dialog.show();
        }

        @Override
        protected List<DataRow> doInBackground(Preferencias... preferencias) {
            DAODataRow db = new DAODataRow(context);
            fuente = preferencias[0];

            // No hay se obtiene la informacion
            LOG.log(Level.INFO, "Obtiene de Internet");
            ContextWrapper cw = new ContextWrapper(getApplicationContext());
            // path to /data/data/yourapp/app_data/imageDir
            File directorio = cw.getDir("rssImg", Context.MODE_PRIVATE);
            Compresor.borrarDirectorio(directorio.getAbsolutePath());
            List<DataRow> list = WebUtil.get(fuente.getUrl(), db, directorio);

            return list;
        }

        @Override
        protected void onPostExecute(List<DataRow> info){
            if (this.dialog.isShowing()) {
                this.dialog.dismiss();
            }
            if(info.isEmpty()) {
                Toast.makeText(context, getString(R.string.pref_url_error), Toast.LENGTH_SHORT).show();
            } else {
                agregarRegistro(fuente);
            }
        }

        /**
         * Establece el contexto para
         * la tarea asincrona
         * @param context Contexto de la actividad
         */
        public void setContext(Context context) {
            this.context = context;
        }

    }
}
