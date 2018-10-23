package com.rssapp.vame.retorss;

import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.rssapp.vame.retorss.bean.DataRow;
import com.rssapp.vame.retorss.dao.DAODataRow;
import com.rssapp.vame.retorss.utils.CustomAdapter;
import com.rssapp.vame.retorss.utils.PreferencesManager;
import com.rssapp.vame.retorss.utils.WebUtil;

import java.io.File;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Actividad que se utilizara para mostrar
 * los objetos leidos del RSS.
 */
public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    /** Listado de feeds */
    private List<DataRow> filas;

    /** List view a utilizar en java */
    private ListView listView;

    /** Logger de clase */
    Logger LOGGER = Logger.getLogger(MainActivity.class.getName());

    /** Adaptador de codigo para el listview */
    private static CustomAdapter adapter;

    /** Vista del progress bar */
    private LinearLayout progressLayout;

    /** Variable de intents */
    public static String FILA="DATOS";

    /** Control de cambios de repositorios */
    PreferencesManager mng;
    private String urlActual;
    private int cambioURL;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mng = new PreferencesManager(this);
        urlActual = mng.getPreferenciaString(getString(R.string.url_actual));
        cambioURL = mng.getPreferenciaInt(getString(R.string.url_cambio));

        // Se genera la toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        progressLayout = (LinearLayout) findViewById(R.id.linlaHeaderProgress);

        // Revisa en las preferencias si existen datos
        if(!urlActual.trim().isEmpty()) {
            cargarInformacion(urlActual);
        } else {
            Toast.makeText(this, "No se ha elegido ninguna URL para cargar", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        urlActual = mng.getPreferenciaString(getString(R.string.url_actual));
        cambioURL = mng.getPreferenciaInt(getString(R.string.url_cambio));
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

        // Estableciendo la busqueda de informacion por titulo
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(new ComponentName(this, SearchActivity.class)));
        return true;
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        int id = item.getItemId();

        // Se obtienen los eventos de navegacion
        if (id == R.id.nav_manage) {
            Intent configuracion = new Intent(this, PreferencesActivity.class);
            startActivity(configuracion);
            finish();
        } else if (id == R.id.nav_refresh) {
            Toast.makeText(getApplicationContext(),getString(R.string.actualizar), Toast.LENGTH_SHORT).show();
            cambioURL = 1;
            cargarInformacion( urlActual );
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * Carga la informacion del RSS.
     */
    private void cargarInformacion(String url){
        DAORSSFeedTask task = new DAORSSFeedTask();
        task.setContext(this);
        task.execute(url);
    }

    /**
     * Coloca los objetos en la lista
     * @param filas Filas a establecer
     */
    public void establecerAdaptador(List<DataRow> filas){
        // Se coloca el adaptador con la lista recien obtenida
        adapter = new CustomAdapter(filas, getApplicationContext());
        listView = (ListView) findViewById(R.id.list);
        listView.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        // Se levanta un evento para hacer clic sobre las filas
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            Logger log = Logger.getLogger(MainActivity.class.getName());
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Dentro se obtiene el objeto  y se envia a la actividad de descripcion
                DataRow dataRow =(DataRow) parent.getItemAtPosition(position);
                Snackbar.make(view, getString(R.string.clic_noticia), Snackbar.LENGTH_SHORT).show();
                Intent intent = new Intent(getApplicationContext(), DetailActivity.class);
                intent.putExtra(FILA, dataRow);
                startActivity(intent);
            }
        });
    }

    /**
     * Ejecuta el acceso a datos a traves de
     * una tarea asincrona. En este caso accede
     * a un servidor web para consumir a traves
     * de GET sus recursos.
     */
    public class DAORSSFeedTask extends AsyncTask<String, LinearLayout, List<DataRow>> {

        /** Logger de la aplicacion */
        private final Logger LOG = Logger.getLogger(DAORSSFeedTask.class.getName());

        /** Contexto para el manejo de la base de datos */
        private Context context;

        /** Base de datos con registros para consultas */
        DAODataRow db;

        boolean cache = false;

        @Override
        protected void onPreExecute(){
            if(adapter != null) {
                adapter.clear();
                adapter.notifyDataSetChanged();
            }
            progressLayout.setVisibility(View.VISIBLE);
            cache = false;
        }

        @Override
        protected List<DataRow> doInBackground(String... strings) {
            db = new DAODataRow(context);
            String stringUrl = strings[0];

            List<DataRow> lista;
            if(cambioURL == 0) {
                // Se revisa primero si existe informacion en la base
                cache = true;
                lista = db.obtenerTodo();
                LOG.log(Level.INFO, "Obtuvo de base de datos " + lista.size() + " registros");
            } else {
                LOG.log(Level.INFO, "Preferencias cambiaron");
                lista = obtenerLista(stringUrl);
                cambioURL = 0;
                mng.setPreferenciaInt(getString(R.string.url_cambio), cambioURL);
                return lista;
            }

            // Se revisa si se obtuvo informacion
            if(lista.isEmpty()) {
                return obtenerLista(stringUrl);
            }

            return lista;
        }

        @Override
        protected void onPostExecute(List<DataRow> data){
            // Envia un mensaje cuando se carga de cache la informacion
            if(cache) {
                Toast.makeText(context, context.getString(R.string.cargando_cache), Toast.LENGTH_SHORT).show();
                cache = false;
            }

            // Vacio no se ocupa
            progressLayout.setVisibility(View.GONE);
            establecerAdaptador(data);
        }

        /**
         * Obtiene la lista de internet
         * @param stringUrl URL a buscar
         */
        private List<DataRow> obtenerLista(String stringUrl){
            db.truncarTabla();
            // No hay se obtiene la informacion
            LOG.log(Level.INFO, "Obtiene de Internet");
            ContextWrapper cw = new ContextWrapper(getApplicationContext());
            // path to /data/data/yourapp/app_data/imageDir
            File directorio = cw.getDir("rssImg", Context.MODE_PRIVATE);
            // Se vuelven a cargar las imagenes
            return WebUtil.get(stringUrl, db, directorio);
        }

        /**
         * Obtiene el contexto
         * @return Contexto de la applicacion
         */
        public Context getContext() {
            return context;
        }

        /**
         * Establece el contexto de la aplicacion
         * @param context Contexto de la aplicacion
         */
        public void setContext(Context context) {
            this.context = context;
        }

    }

}
