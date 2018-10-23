package com.rssapp.vame.retorss;

import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.rssapp.vame.retorss.bean.DataRow;
import com.rssapp.vame.retorss.dao.DAODataRow;
import com.rssapp.vame.retorss.utils.CustomAdapter;

import java.util.List;
import java.util.logging.Logger;

import static com.rssapp.vame.retorss.MainActivity.FILA;

/**
 * Actividad cuyo proposito es buscar en el listado
 * obtenido de internet la informacion correspondiente
 */
public class SearchActivity extends AppCompatActivity {

    /** Objeto con el acceso a una tabla virtual */
    DAODataRow db = new DAODataRow(this);

    /** List view a utilizar en java */
    ListView listView;

    private static List<DataRow> filas;

    /** Logger de la actividad */
    Logger LOGGER = Logger.getLogger(SearchActivity.class.getName());

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.preferencias);

        listView = (ListView) findViewById(R.id.prefList);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Se procesa el intento
        handleIntent(getIntent());

    }

    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {

        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            // Se procede a realizar la consulta de informacion
            String query = intent.getStringExtra(SearchManager.QUERY);
            filas = db.buscarCoincidencias(query, null);

            // Se coloca el adaptador con la lista recien obtenida
            CustomAdapter adapter = new CustomAdapter(filas, getApplicationContext());
            listView.setAdapter(adapter);

                // Se levanta un evento para hacer clic sobre las filas
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    Logger log = Logger.getLogger(MainActivity.class.getName());
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        DataRow dataRow =(DataRow) parent.getItemAtPosition(position);
                        Snackbar.make(view, getString(R.string.clic_noticia), Snackbar.LENGTH_SHORT).show();
                        Intent intent = new Intent(getApplicationContext(), DetailActivity.class);
                        intent.putExtra(FILA, dataRow);
                        startActivity(intent);
                    }
                });
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
    public boolean onSupportNavigateUp(){
        finish();
        return true;
    }
}
