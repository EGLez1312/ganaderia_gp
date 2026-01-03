/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Util;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

/**
 * Utilidad centralizada para obtener la SessionFactory de Hibernate.
 * Se utiliza en toda la aplicación para acceder a las entidades JPA.
 * 
 * @author Elena González
 * @version 1.0
 */
public class HibernateUtil {
    private static final SessionFactory sessionFactory = buildSessionFactory();

    /**
     * Construye la SessionFactory a partir de hibernate.cfg.xml.
     * Se ejecuta una sola vez al cargar la clase.
     */
    private static SessionFactory buildSessionFactory() {
        try {
            return new Configuration()
                    .configure("hibernate.cfg.xml")
                    .addAnnotatedClass(Modelo.Usuario.class)
                    .addAnnotatedClass(Modelo.Oveja.class)
                    .addAnnotatedClass(Modelo.Evento.class)
                    .buildSessionFactory();
        } catch (Throwable ex) {
            System.err.println("Error al crear SessionFactory: " + ex.getMessage());
            throw new ExceptionInInitializerError(ex);
        }
    }

    /**
     * Devuelve la SessionFactory única de la aplicación.
     *
     * @return SessionFactory configurada.
     */
    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    /**
     * Cierra la SessionFactory al finalizar la aplicación.
     */
    public static void shutdown() {
        if (sessionFactory != null) {
            sessionFactory.close();
        }
    }
}