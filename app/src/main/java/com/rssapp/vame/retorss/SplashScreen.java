package com.rssapp.vame.retorss;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ProgressBar;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Actividad encargada de mostrar la
 * presentacion de la empresa.
 */
public class SplashScreen extends Activity {

    /** Barra de progreso */
    private ProgressBar mProgress;

    /** Logger de la actividad */
    Logger LOGGER = Logger.getLogger(SplashScreen.class.getName());

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        // Show the splash screen
        setContentView(R.layout.splash_screen);
        mProgress = (ProgressBar) findViewById(R.id.splash_screen_progress_bar);

        // Start lengthy operation in a background thread
        new Thread(new Runnable() {
            public void run() {
                generaProgreso();
                arrancaAplicacion();
                finish();
            }
        }).start();
    }

    /** Presenta un retraso para que se vea la aplicacion */
    private void generaProgreso() {
        for (int progress=0; progress<100; progress+=20) {
            try {
                Thread.sleep(1000);
                mProgress.setProgress(progress);
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, e.getMessage());
            }
        }
    }

    /** Arranca la aplicacion */
    private void arrancaAplicacion() {
        Intent intent = new Intent(SplashScreen.this, MainActivity.class);
        startActivity(intent);
    }
}
