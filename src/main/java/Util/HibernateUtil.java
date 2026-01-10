/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Util;

import Modelo.Evento;
import Modelo.Oveja;
import Modelo.Usuario;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

/**
 * Factory Singleton Hibernate 5.x con inicialización tolerante a fallos.
 * Static block try-catch: NO falla la app si hibernate.cfg.xml/DB cae. DAOs
 * detectan sessionFactory=null y manejan graceful degradation. Perfecto para
 * desarrollo/debug sin parar app por config BD.
 *
 * @author Elena González
 * @version 1.0
 */
public class HibernateUtil {

    /**
     * SessionFactory única (nullable si falla inicialización). Static block
     * try-catch permite app continuar sin Hibernate.
     */
    private static SessionFactory sessionFactory;

    /**
     * **Static initializer tolerante**: construye SessionFactory o null. **NO
     * lanza ExceptionInInitializerError** → app sobrevive. Loggea error
     * console, DAOs manejan null graceful.
     */
    static {
        try {
            sessionFactory = new Configuration()
                    // Carga hibernate.cfg.xml)
                    .configure()
                    // 3 entidades JPA 2.2 javax.persistence
                    .addAnnotatedClass(Usuario.class) // tabla usuario
                    .addAnnotatedClass(Oveja.class) // tabla oveja
                    .addAnnotatedClass(Evento.class) // tabla evento
                    .buildSessionFactory();

            System.out.println("Hibernate inicializado correctamente");

        } catch (Throwable ex) {
            // Loggea pero NO falla app (desarrollo-friendly)
            System.err.println("Hibernate FALLÓ en static block:");
            System.err.println("   " + ex.getClass().getSimpleName() + ": " + ex.getMessage());
            sessionFactory = null;  // DAO detecta y maneja
        }
    }

    /**
     * Devuelve SessionFactory o lanza excepción clara si fallo. **DAOs llaman
     * este método** → fallan solo cuando intentan BD.
     *
     * @return SessionFactory activa.
     * @throws IllegalStateException si hibernate.cfg.xml/DB/entidades fallan.
     */
    public static SessionFactory getSessionFactory() {
        if (sessionFactory == null) {
            throw new IllegalStateException(
                    "Hibernate no inicializado. Revisar console:\n"
                    + "• hibernate.cfg.xml existe en src/\n"
                    + "• MySQL puerto 3308 corriendo\n"
                    + "• DB ganaderia_gp existe\n"
                    + "• Librerías Hibernate 5 + javax.persistence-api-2.2.jar");
        }
        return sessionFactory;
    }

    /**
     * Cierra SessionFactory graceful (solo si inicializada). Libera pool
     * conexiones MySQL, previene "Too many connections".
     */
    public static void shutdown() {
        if (sessionFactory != null && !sessionFactory.isClosed()) {
            try {
                sessionFactory.close();
                System.out.println("Hibernate SessionFactory cerrada");
            } catch (Exception ex) {
                System.err.println("Error shutdown: " + ex.getMessage());
            }
        }
    }

    /**
     * Estado diagnóstico (debug desarrollo).
     *
     * @return true si Hibernate listo para DAOs.
     */
    public static boolean isAvailable() {
        return sessionFactory != null && !sessionFactory.isClosed();
    }
}
