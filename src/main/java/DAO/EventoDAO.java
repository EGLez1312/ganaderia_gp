/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DAO;

import Modelo.Evento;
import Util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import java.util.List;

/**
 * DAO para operaciones CRUD de eventos relacionados con ovejas.
 * Gestiona la persistencia de eventos usando Hibernate con HibernateUtil.
 */
public class EventoDAO {

    /**
     * Obtiene Session Hibernate lazy (maneja Hibernate no inicializado).
     *
     * @return Session activa.
     * @throws IllegalStateException si HibernateUtil falló.
     */
    protected Session getSession() {
        try {
            return HibernateUtil.getSessionFactory().openSession();
        } catch (Exception ex) {
            throw new IllegalStateException("Hibernate no disponible: " + ex.getMessage(), ex);
        }
    }

    /**
     * Inserta un nuevo evento en la base de datos.
     *
     * @param evento evento a registrar.
     */
    public void insertar(Evento evento) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.persist(evento);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        }
    }

    /**
     * Actualiza un evento existente.
     *
     * @param evento evento con los datos actualizados.
     */
    public void actualizar(Evento evento) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.merge(evento);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        }
    }

    /**
     * Lista todos los eventos registrados.
     *
     * @return lista completa de eventos.
     */
    public List<Evento> listarTodos() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM Evento WHERE activo = true", Evento.class).list();
        }
    }

    /**
     * Obtiene los eventos de una oveja concreta.
     *
     * @param idOveja ID de la oveja.
     * @return lista de eventos asociados.
     */
    public List<Evento> obtenerEventosPorOveja(Integer idOveja) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Evento> query =
                    session.createQuery("from Evento where oveja.id = :idOveja", Evento.class);
            query.setParameter("idOveja", idOveja);
            return query.list();
        }
    }

    /**
     * Elimina un evento de forma permanente de la base de datos.
     *
     * @param id ID del evento a eliminar.
     */
    public void eliminar(int id) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();

            // Buscamos la entidad en la base de datos
            Evento evento = session.get(Evento.class, id);

            if (evento != null) {
                // Borrado físico: elimina la fila de la tabla permanentemente
                session.remove(evento);
                tx.commit();
                System.out.println("Evento eliminado con éxito.");
            }
        } catch (Exception e) {
            if (tx != null) {
                tx.rollback(); // Deshace cambios si hay error
            }
            e.printStackTrace();
        }
    }
}
