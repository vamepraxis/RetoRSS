package com.rssapp.vame.retorss.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.rssapp.vame.retorss.R;
import com.rssapp.vame.retorss.bean.DataRow;

import java.util.List;

/**
 * Adaptador personalizado que se encarga
 * de manejar los feeds obtenidos por el
 * servicio.
 *
 * Se encargara de manejar su evento de clic
 * asi como tambien de mostrar mas informacion
 * en la activdad.
 */
public class CustomAdapter extends ArrayAdapter<DataRow> {

    /** Set de datos para mostrar */
    private List<DataRow> setDatos;

    private Context mContext;

    private int ultimaPosicion = -1;

    private static class ViewHolder {
        ImageView img;
        TextView descripcion;
        TextView fecha;
        TextView titulo;
    }

    /** Inicializa los datos */
    public CustomAdapter(List<DataRow> datos, Context context){
        super(context, R.layout.row, datos);
        this.setDatos = datos;
        this.mContext = context;
    }

    @Override
    public View getView(int position, View convertibleView, ViewGroup parent) {
        DataRow modelo = getItem(position);

        ViewHolder cache;

        final View resultado;

        if(convertibleView == null) {
            cache = new ViewHolder();
            LayoutInflater inflador = LayoutInflater.from(getContext());
            convertibleView = inflador.inflate(R.layout.row, parent, false);

            // Guardando la informacion
            cache.descripcion = (TextView) convertibleView.findViewById(R.id.txt_desc);
            cache.fecha = (TextView) convertibleView.findViewById(R.id.txt_fecha);
            cache.titulo = (TextView) convertibleView.findViewById(R.id.txt_titulo);
            cache.img = (ImageView) convertibleView.findViewById(R.id.img_foto);

            resultado = convertibleView;
            convertibleView.setTag(cache);

        } else {
            cache = (ViewHolder) convertibleView.getTag();
            resultado = convertibleView;
        }

        ultimaPosicion = position;

        cache.titulo.setText(modelo.getTitulo());
        cache.descripcion.setText(modelo.getDescripcion());
        cache.fecha.setText(modelo.getFechaPub());
        cache.img.setImageBitmap(modelo.getImg());
        cache.img.setTag(position);

        // Se retorna la vista para mostrar en pantalla
        return convertibleView;
    }
}
