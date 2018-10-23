package com.rssapp.vame.retorss;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.rssapp.vame.retorss.bean.DataRow;

/**
 * Muestra el detalle de la noticia en una
 * pantalla que permite ver la informacion
 * asi como tambien acceder a otros navegadores
 */
public class DetailActivity extends AppCompatActivity {

    /** Objeto del cual se obtiene la informacion */
    private DataRow dataRow;

    /** Imagen de la actividad */
    private ImageView image;

    /** Titulo de la pantalla */
    private TextView titulo;

    /** Descripcion de la informacion */
    private TextView descripcion;

    /** Boton de abrir en el navegador */
    private Button boton;

    @Override
    public void onCreate(Bundle savedInstance){
        super.onCreate(savedInstance);

        setContentView(R.layout.row_details);

        // Se obtiene el objeto de la fila
        Intent i = getIntent();
        dataRow = (DataRow) i.getSerializableExtra(MainActivity.FILA);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Se obtienen las vistas
        titulo = (TextView) findViewById(R.id.desc_title);
        descripcion = (TextView) findViewById(R.id.desc_description);
        image = (ImageView) findViewById(R.id.desc_img);
        boton = (Button) findViewById(R.id.desc_button);

        // Se establece el texto
        titulo.setText(dataRow.getTitulo());
        descripcion.setText(dataRow.getDescripcion());

        if(dataRow.getImg() != null) {
            image.setImageBitmap(dataRow.getImg());
        }

        boton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Snackbar.make(v, "Abriendo noticia", Snackbar.LENGTH_SHORT).show();
                Intent navegador = new Intent(Intent.ACTION_VIEW, Uri.parse(dataRow.getLink()));
                startActivity(navegador);
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return true;
    }
}