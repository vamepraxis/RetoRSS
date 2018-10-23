package com.rssapp.vame.retorss.utils;

import com.rssapp.vame.retorss.bean.DataRow;
import com.rssapp.vame.retorss.dao.DAODataRow;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utileria para el acceso a internet
 * Se utiliza para obtener RSS o flujos
 * de alguna URL en particular
 */
public class WebUtil {

    /** Metodo de la solicitud */
    public static final String REQUEST_METHOD = "GET";

    /** Time out de lectura */
    public static final int READ_TIMEOUT = 15000;

    /** Time out de conexion */
    public static final int CONNECTION_TIMEOUT = 15000;

    /** Prevee redirecciones */
    public static final boolean FOLLOW_REDIRECTS = true;

    /** Logger de la aplicacion */
    private static final Logger LOG = Logger.getLogger(WebUtil.class.getName());

    private WebUtil(){
        // Vacio
    }

    /**
     * Se conecta a un lugar mediante el metodo GET
     * @param url URL a conectarse
     * @param db Base de datos para almacenar cache
     * @param ruta Ruta donde se almacenaran la url
     * @return Listado de DataRows con los datos obtenidos
     */
    public static List<DataRow> get(String url, DAODataRow db, File ruta){
        // Flujos cerrables
        HttpURLConnection connection = null;
        try {
            //Create a URL object holding our url
            URL myUrl = new URL(url);
            //Create a connection
            connection =(HttpURLConnection)
                    myUrl.openConnection();
            //Set methods and timeouts
            connection.setRequestMethod(REQUEST_METHOD);
            connection.setReadTimeout(READ_TIMEOUT);
            connection.setConnectTimeout(CONNECTION_TIMEOUT);
            LOG.info("URL " + myUrl);
            // Se siguen redirecciones
            int statusCode = connection.getResponseCode();
            LOG.log(Level.INFO, "Respuesta: " + statusCode);
            while(isRedirect(statusCode)) {
                LOG.log(Level.INFO, "Redireccion!");
                String nuevaUrl = connection.getHeaderField("Location");

                LOG.info("A " + nuevaUrl);
                connection =(HttpURLConnection)
                        new URL(nuevaUrl).openConnection();
                connection.setRequestMethod(REQUEST_METHOD);
                connection.setReadTimeout(READ_TIMEOUT);
                connection.setConnectTimeout(CONNECTION_TIMEOUT);

                statusCode = connection.getResponseCode();
            }

            // Se procesa la lista
            return XMLParser.procesaLista(connection.getInputStream(), db, ruta);

        } catch (IOException e){
            LOG.log(Level.SEVERE,"Ocurrio un error " + e );
        } finally{
            if(connection != null) {
                connection.disconnect();
            }
        }
        return new ArrayList<>();
    }

    private static boolean isRedirect(int status){
        // Revisa que no se trate de un 200
        if (status == HttpURLConnection.HTTP_OK) {
            return false;
        }
        if (status == HttpURLConnection.HTTP_MOVED_TEMP
                || status == HttpURLConnection.HTTP_MOVED_PERM
                || status == HttpURLConnection.HTTP_SEE_OTHER) {
            return true;
        }
        return false;
    }
}
