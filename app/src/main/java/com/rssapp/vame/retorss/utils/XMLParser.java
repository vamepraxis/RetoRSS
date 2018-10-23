package com.rssapp.vame.retorss.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.Html;
import android.util.Xml;

import com.rssapp.vame.retorss.bean.DataRow;
import com.rssapp.vame.retorss.dao.DAODataRow;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class XMLParser {

    /** Logger de la aplicacion */
    private static final Logger LOGGER = Logger.getLogger(XMLParser.class.getName());

    /** Espacio de nombres a leer */
    public static final String NAMESPACE = null;

    /** Variables para el procesamiento de etiquetas */
    private static final String RSS = "rss";
    private static final String CHANNEL = "channel";
    private static final String URL = "url";
    private static final String ITEM = "item";
    private static final String TITLE = "title";
    private static final String DESCRIPTION = "description";
    private static final String LINK = "link";
    private static final String PUB_DATE = "pubDate";
    private static final String MEDIA_DESCRIPTION = "media:description";
    private static final String MEDIA_CONTENT = "media:content";
    private static final String MEDIA_THUMBNAIL = "media:thumbnail";

    /** Se convierte en una utileria */
    private XMLParser(){
    }

    /**
     * Procesa el XML que se obtiene de la solicitud y coloca su informacion
     * en una lista
     * @param result Flujo a leer para el parser
     * @param db Base de datos a llenar
     * @return Lista con la informacion interpretada
     */
    public static List<DataRow> procesaLista(InputStream result, DAODataRow db, File ruta){
        try {
            XmlPullParser xmlParser = Xml.newPullParser();
            xmlParser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            xmlParser.setInput(result, null);

            xmlParser.nextTag();
            return leerFeed(xmlParser, db, ruta);
        } catch(XmlPullParserException | IOException e){
            LOGGER.log(Level.SEVERE,"Error: " + e);
        }
        return new ArrayList<DataRow>();
    }

    /**
     * Lee el interprete y genera una lista de data rows
     * para mostrar en el servidor
     * @param parser Lector o interprete de XML
     * @param db Base de datos a llenar
     * @return Listado con la informacion a mostrar en el
     * list view
     */
    private static List<DataRow> leerFeed(XmlPullParser parser, DAODataRow db, File ruta){
        List<DataRow> listado = new ArrayList<DataRow>();

        try{
            parser.require(XmlPullParser.START_TAG, NAMESPACE, RSS);
            // Se borran los contenidos del directorio
            Compresor.borrarDirectorio(ruta.getAbsolutePath());
            Bitmap defaultBitmap = null;
            while(parser.next() != XmlPullParser.END_DOCUMENT) {
                if (parser.getEventType() != XmlPullParser.START_TAG) {
                    continue;
                }

                String name = parser.getName();

                // Inicia busqueda de las etiquetas
                if(name.equals(CHANNEL)){
                    // Encuentra la etiqueta channel
                    continue;
                }else if(name.equals(ITEM)) {
                    // Agrega el elemento a la fila
                    DataRow dr = leeFila(parser);

                    // Se coloca en SO
                    dr.setPath(ruta.getAbsolutePath().concat(File.separator + dr.getId()));
                    LOGGER.log(Level.INFO, "Ruta: " + dr.getPath());
                    if(Compresor.saveImg(dr.getImg(), dr.getPath())) {
                        LOGGER.log(Level.INFO, "Se almacena img numero: "+listado.size()
                                +" para titulo: " + dr.getTitulo());
                        // Se agrega a la tabla
                        db.agregarATabla(dr);
                    }
                    // Se agrega a la lista
                    listado.add( dr );
                } else {
                    omitirEtiqueta(parser);
                }
            }
        } catch(XmlPullParserException | IOException e){
            LOGGER.log(Level.SEVERE,"Error: " + e);
        }
        return listado;
    }

    /**
     * Lee una fila del XML a interpretar
     * @param parser Objeto que se usa para leer el XML
     * @return Fila con la informacion de un registro
     */
    private static DataRow leeFila(XmlPullParser parser) throws XmlPullParserException {
        String titulo="";
        String desc="";
        String fecha="";
        String link="";
        Bitmap img = null;
        try {
            parser.require(XmlPullParser.START_TAG, NAMESPACE, ITEM);

            // Se recorren las etiquetas de un Item o row
            String etiqueta = "";
            int next = parser.next();
            while ( next != XmlPullParser.END_TAG || ( next == XmlPullParser.END_TAG && !etiqueta.equals(ITEM) )){
                int eventType = parser.getEventType();
                if (eventType == XmlPullParser.TEXT || eventType != XmlPullParser.START_TAG) {
                    next = parser.next();
                    etiqueta = parser.getName();
                    continue;
                }

                // Interpretacion de cada una de las etiquetas
                etiqueta = parser.getName();
                switch (etiqueta){
                    case TITLE:
                        titulo = leerTitulo(parser);
                        break;
                    case DESCRIPTION:
                        desc = leerDescripcion(parser);
                        break;
                    case MEDIA_DESCRIPTION:
                        String tmpDesc = leerDescripcion(parser);
                        // Previene que se elimine informacion mas grande
                        if(tmpDesc.trim().length() > desc.trim().length()){
                            desc = tmpDesc;
                        }
                        break;
                    case PUB_DATE:
                        fecha = leerFecha(parser);
                        break;
                    case LINK:
                        link = leerEnlace(parser);
                        break;
                    case MEDIA_THUMBNAIL:
                    case MEDIA_CONTENT:
                        img = leeImagen(parser);
                        break;
                }
                next = parser.next();
            }

        } catch(XmlPullParserException | IOException e){
            LOGGER.log( Level.SEVERE, "Error: " + e);
        }

        // Se retorna el primer registro leido
        DataRow dr = new DataRow();
        dr.setTitulo(titulo);
        dr.setLink(link);
        dr.setDescripcion(desc);
        dr.setFechaPub(fecha);
        dr.setImg(img);
        return dr;
    }

    /**
     * Lee el titulo de un XML
     * @param parser Lector que contiene el XML
     * @return String con el texto
     * @throws XmlPullParserException En caso de errores
     * @throws IOException En caso de que no se pueda leer la informacion
     */
    private static String leerTitulo(XmlPullParser parser)
            throws XmlPullParserException, IOException{
        parser.require(XmlPullParser.START_TAG, NAMESPACE, TITLE);
        String title = obtenTexto(parser);
        parser.require(XmlPullParser.END_TAG, NAMESPACE, TITLE);
        return title;
    }

    /**
     * Lee la descripcion del contenido de la noticia
     * @param parser Lector que contiene el texto XML
     * @return Cadena de texto con la descripcion
     * @throws XmlPullParserException En caso de errores
     * @throws IOException En caso de que no se pueda leer la informacion
     */
    private static String leerDescripcion(XmlPullParser parser)
            throws XmlPullParserException, IOException{
        String description = obtenTexto(parser);
        description = stripHtml(description);
        return description;
    }

    /**
     * Lee la fecha de publicacion a traves de la variable pubDate
     * @param parser Lector con la informacion del XML
     * @return Cadena de texto con la fecha de publicacion
     * @throws XmlPullParserException En caso de error al interpretar
     * @throws IOException En caso de no poder leer la informacion
     */
    private static String leerFecha(XmlPullParser parser)
            throws XmlPullParserException, IOException{
        parser.require(XmlPullParser.START_TAG, NAMESPACE, PUB_DATE);
        String pubDate = obtenTexto(parser);
        parser.require(XmlPullParser.END_TAG, NAMESPACE, PUB_DATE);
        return pubDate;
    }

    /**
     * Lee el enlace adjunto en el RSS de la noticia
     * @param parser Lector con la informacion del XML
     * @return Cadena de texto con la url de la publicacion
     * @throws XmlPullParserException En caso de error al interpretar
     * @throws IOException En caso de no poder leer la informacion
     */
    private static String leerEnlace(XmlPullParser parser)
            throws XmlPullParserException, IOException{
        parser.require(XmlPullParser.START_TAG, NAMESPACE, LINK);
        String link = obtenTexto(parser);
        parser.require(XmlPullParser.END_TAG, NAMESPACE, LINK);
        return link;
    }

    /**
     * Lee la url de la imagen que se cargara para mostrar en el ListView
     * @param parser Lector con la informacion del XML
     * @return Mapa de bits de la URL obtenida y lista para utilizarse
     * @throws XmlPullParserException En caso de error al interpretar
     * @throws IOException En caso de no poder leer la informacion
     */
    private static Bitmap leeImagen(XmlPullParser parser)
            throws XmlPullParserException, IOException {
        String imagen = parser.getAttributeValue(null, URL);
        URL url = new URL(imagen);
        Bitmap bitmap = BitmapFactory.decodeStream(url.openConnection().getInputStream());
        parser.nextTag();
        return bitmap;
    }

    /**
     * Lee un nodo y obtiene la informacion de texto requerida
     * @param parser Lector del XML con su informacion
     * @return Cadena de texto con los datos obtenidos
     * @throws XmlPullParserException En caso de no poder leer el XML
     *          de forma correcta
     * @throws IOException En caso de que ocurra un error de lectura
     *          en el flujo
     */
    private static String obtenTexto(XmlPullParser parser)
            throws XmlPullParserException, IOException {
        String result = "";
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.getText();
            parser.nextTag();
        }
        return result;
    }

    /**
     * Hace la omision de las etiquetas que no se deseean usar
     * @param parser Lector XML para actualizar mover el puntero de las etiquetas
     * @throws XmlPullParserException En caso de error al leer la informacion
     * @throws IOException En caso de no poder leer la informacion
     */
    private static void omitirEtiqueta(XmlPullParser parser) throws XmlPullParserException, IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }
        int depth = 1;
        // Procede a moverse a traves del XML
        while (depth != 0) {
            switch (parser.next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }

    /**
     * Elimina codigo de html de una etiqueta
     * @param html Cadena de texto con html
     * @return Texto sin html
     */
    public static String stripHtml(String html) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            return Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY).toString();
        } else {
            return Html.fromHtml(html).toString();
        }
    }
}
