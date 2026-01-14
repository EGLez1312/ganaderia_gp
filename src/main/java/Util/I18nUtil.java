/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Util;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Clase utilitaria para la internacionalización de la aplicación mediante 
 * {@link ResourceBundle} y ficheros {@code messages_*.properties}.
 * 
 * Soporta cambio dinámico de idioma en runtime (ES/EN) sin cache estático.
 * Único método {@link #get(String, Object...)} crea ResourceBundle fresco cada llamada.
 * 
 * 
 * Uso:
 * 
 * I18nUtil.setLocale("en"); 
 * String title = I18nUtil.get("login.title");
 * String msg = I18nUtil.get("login.success", usuario.getNombre());
 * 
 * 
 * @author Elena González
 * @version 1.0
 */
public class I18nUtil {
    
    /**
     * Nombre base de los ficheros de recursos sin extensión. Los ficheros
     * reales serán: {@code messages_es.properties}, {@code messages_en.properties}
     */
    private static final String BASE_NAME = "messages";

    /**
     * Localidad actual de la aplicación. Español por defecto.
     * 
     * @see #setLocale(String)
     */
    private static Locale locale = Locale.forLanguageTag("es-ES");

    /**
     * Constructor privado (clase utilitaria estática).
     */
    private I18nUtil() {
        throw new UnsupportedOperationException("Utility class - no instances allowed");
    }

    /**
     * Cambia el idioma de la aplicación en tiempo de ejecución.
     * 
     * Soporta: {@code "es"} (Español), {@code "en"} (Inglés). 
     * Otros valores default a Español.
     * 
     *
     * @param lang Código de idioma ISO 639-1: {@code "es"} o {@code "en"}
     * @see Locale#forLanguageTag(String)
     */
    public static void setLocale(String lang) {
        String normalized = lang != null ? lang.toLowerCase().trim() : "es";
        switch (normalized) {
            case "en":
                locale = Locale.forLanguageTag("en");
                break;
            case "es":
            default:
                locale = Locale.forLanguageTag("es");
                break;
        }
    }

    /**
     * Obtiene un texto internacionalizado con formato de parámetros opcional.
     * 
     * Único método público. Crea ResourceBundle fresco cada llamada
     * (sin cache → soporta cambio runtime).
     * 
     *
     * @param key  Clave del mensaje en properties (ej: {@code "login.title"})
     * @param args Parámetros para {@link String#format(String, Object...)} 
     *             (opcional, varargs)
     * @return Texto traducido y formateado, o {@code "???key???" } si no existe
     * @throws MissingResourceException si properties no encontrado
     * @see ResourceBundle#getBundle(String, Locale)
     */
    public static String get(String key, Object... args) {
        System.out.println("? Buscando: " + key + " locale=" + locale);
        try {
            // ResourceBundle FRESCO cada vez → cambio runtime funciona
            ResourceBundle bundle = ResourceBundle.getBundle(BASE_NAME, locale);
            String message = bundle.getString(key);
            System.out.println("? Encontrado: " + message.substring(0, Math.min(20, message.length())));
            
            return args.length > 0 ? String.format(message, args) : message;
        } catch (MissingResourceException e) {
            System.err.println("? Clave NO encontrada: " + key + " en " + locale);
            return "???" + key + "???";
        }
    }

    /**
     * Obtiene localidad actual de la aplicación.
     *
     * @return Locale activo (ej: es_ES, en_UK)
     */
    public static Locale getLocale() {
        return (Locale) locale.clone();  // Inmutable
    }
}

