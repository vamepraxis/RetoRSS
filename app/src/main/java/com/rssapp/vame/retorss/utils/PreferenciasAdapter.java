package com.rssapp.vame.retorss.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.rssapp.vame.retorss.R;
import com.rssapp.vame.retorss.bean.Preferencias;
import com.rssapp.vame.retorss.dao.DAOPreferencias;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Adaptador personalizado que se encarga
 * de manejar las fuentes RSS que el usuario
 * conoce y ha dado de alta.
 *
 * Se encargara de manejar su evento de clic
 * asi como tambien de mostrar mas informacion
 * en la activdad.
 */
public class PreferenciasAdapter extends ArrayAdapter<Preferencias>
        implements View.OnClickListener {

    /** Set de datos para mostrar */
    private List<Preferencias> fuentes;

    /** Contexto de la aplicacion */
    private Context mContext;

    /** Constructor de dialogos */
    private AlertDialog.Builder builder;

    private int ultimaPosicion = -1;
    private int posicionSeleccionada;

    private static final Logger LOGGER = Logger.getLogger(PreferenciasAdapter.class.getName());
    /**
     * Clase intermedia que almacena las fuentes RSS
     */
    private static class PrefHolder  {
        TextView url;
        TextView nombre;
        Button eliminar;
    }

    /** Inicializa los datos */
    public PreferenciasAdapter(List<Preferencias> datos, Context context){
        super(context, R.layout.row, datos);
        this.fuentes = datos;
        this.mContext = context;
        SharedPreferences mPrefs = mContext.getSharedPreferences(
                mContext.getString(R.string.archivo_preferencias), Context.MODE_PRIVATE);
        posicionSeleccionada = mPrefs.getInt(mContext.getString(R.string.selected), -1);
    }

    @Override
    public View getView(int position, View convertibleView, ViewGroup parent) {
        Preferencias modelo = getItem(position);
        // Establece la posicion en el listado
        modelo.setPosicion(position);

        PrefHolder cache;

        final View resultado;

        if(convertibleView == null) {
            cache = new PrefHolder();
            LayoutInflater inflador = LayoutInflater.from(getContext());
            convertibleView = inflador.inflate(R.layout.row_pref, parent, false);

            // Guardando la informacion
            cache.nombre = (TextView) convertibleView.findViewById(R.id.pref_nombre);
            cache.url = (TextView) convertibleView.findViewById(R.id.pref_desc);
            cache.eliminar = (Button) convertibleView.findViewById(R.id.pref_eliminar);

            resultado = convertibleView;
            convertibleView.setTag(cache);

        } else {
            cache = (PrefHolder) convertibleView.getTag();
            resultado = convertibleView;
        }

        // Se determina el color
        if(position == posicionSeleccionada) {
            cache.nombre.setTextColor(mContext.getResources().getColor(R.color.colorPrimary));
        } else {
            cache.nombre.setTextColor(mContext.getResources().getColor(R.color.colorBlack));
        }

        ultimaPosicion = position;

        cache.nombre.setText(modelo.getNombre());
        cache.url.setText(modelo.getUrl());

        cache.eliminar.setTag(modelo);
        cache.nombre.setTag(modelo);
        cache.url.setTag(modelo);
        cache.eliminar.setOnClickListener(this);
        cache.nombre.setOnClickListener(this);
        cache.url.setOnClickListener(this);
        // Se retorna la vista para mostrar en pantalla
        return convertibleView;
    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        int id = v.getId();
        LOGGER.log(Level.INFO, ""+id);
        LOGGER.log(Level.INFO, ""+v.getTag());
        final Preferencias tag = (Preferencias) v.getTag();
        posicionSeleccionada = tag.getPosicion();
        switch(id) {
            // Se procede a eliminar
            case R.id.pref_eliminar:
                // 2. Chain together various setter methods to set the dialog characteristics
                builder.setMessage(R.string.dialog_cuerpo)
                        .setTitle(R.string.dialog_titulo);

                builder.setPositiveButton(R.string.dialog_aceptar, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        DAOPreferencias dao = new DAOPreferencias(mContext);
                        long eliminadas = dao.eliminarPreferencia(tag);
                        if(eliminadas == 1) {
                            Toast.makeText(mContext, mContext.getString(R.string.pref_eliminado), Toast.LENGTH_SHORT).show();
                            fuentes.remove(tag.getPosicion());
                            eliminarPreferencias();
                        } else if(eliminadas > 1) {
                            Toast.makeText(mContext, mContext.getString(R.string.pref_multi_borrar), Toast.LENGTH_SHORT).show();
                            fuentes.remove(tag.getPosicion());
                            eliminarPreferencias();
                        } else {
                            Toast.makeText(mContext, mContext.getString(R.string.pref_error_borrar), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                builder.setNegativeButton(R.string.dialog_cancelar, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // No hacer nada
                    }
                });
                // 3. Get the AlertDialog from create()
                AlertDialog dialog = builder.create();
                dialog.show();
                break;
            case R.id.pref_fila:
            case R.id.pref_desc:
            case R.id.pref_nombre:
                // Se almacena la informacion
                SharedPreferences settings = mContext.
                        getSharedPreferences(mContext.getString(R.string.archivo_preferencias), Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = settings.edit();
                editor.putString(mContext.getString(R.string.url_actual), tag.getUrl());
                editor.putInt(mContext.getString(R.string.url_cambio), 1);
                editor.putInt(mContext.getString(R.string.selected), posicionSeleccionada);
                Toast.makeText(mContext, "Estableciendo: " + tag.getUrl(), Toast.LENGTH_SHORT).show();
                editor.apply();
                notifyDataSetChanged();
                break;
        }

    }

    /**
     * Agrega un constructor de dialogos
     * @param builder Constructor de alertas
     */
    public void setBuilder(AlertDialog.Builder builder) {
        this.builder = builder;
    }

    /**
     * Elimina los repositorios de la lista
     * y actualiza la lista
     */
    private void eliminarPreferencias(){
        SharedPreferences settings = mContext
                .getSharedPreferences(mContext.getString(R.string.archivo_preferencias), Context.MODE_PRIVATE);
        settings.edit().clear().apply();
        notifyDataSetChanged();
    }

    /**
     * Establece la posicion seleccionada
     * @param posicionSeleccionada Posicion
     */
    public void setPosicionSeleccionada(int posicionSeleccionada) {
        this.posicionSeleccionada = posicionSeleccionada;
    }
}

