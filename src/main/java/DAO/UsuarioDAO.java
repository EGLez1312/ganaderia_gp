/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DAO;

import Modelo.Usuario;
import Util.HibernateUtil;
import Util.PasswordEncoderUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import java.util.List;

/**
 * DAO para operaciones CRUD de usuarios con Hibernate, soft-delete y validación de login.
 * Cumple requisitos: tabla usuarios con ID, username, password encriptada, email,
 * activo y operaciones INSERT/UPDATE/SELECT/DELETE lógicas.
 * 
 * @author Elena González
 * @version 1.0
 */
public class UsuarioDAO {

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
     * Lista todos los usuarios activos (activo = true).
     * 
     * @return lista de usuarios activos.
     */
    public List<Usuario> listarTodos() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM Usuario WHERE activo = true", Usuario.class).list();
        }
    }

    /**
     * Inserta nuevo usuario con password encriptada.
     * 
     * @param usuario objeto Usuario preparado (password ya encriptada).
     */
    public void insertar(Usuario usuario) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.persist(usuario);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        }
    }

    /**
     * Actualiza usuario existente (merge para entidades desconectadas).
     * 
     * @param usuario objeto con ID y datos actualizados.
     */
    public void actualizar(Usuario usuario) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.merge(usuario);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        }
    }

    /**
     * Da de baja usuario (soft-delete: activo = false).
     * 
     * @param id ID del usuario a eliminar lógicamente.
     */
    public void eliminar(int id) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            Usuario usuario = session.get(Usuario.class, id);
            if (usuario != null) {
                usuario.setActivo(false);
                session.merge(usuario);
                tx.commit();
            }
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        }
    }

    /**
     * Reincorpora usuario inactivo (activo = true).
     * 
     * @param id ID del usuario a reactivar.
     */
    public void reincorporar(int id) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            Usuario usuario = session.get(Usuario.class, id);
            if (usuario != null) {
                usuario.setActivo(true);
                session.merge(usuario);
                tx.commit();
            }
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        }
    }

    /**
     * Busca usuario por username (solo activos).
     * 
     * @param username nombre de usuario.
     * @return Usuario o null.
     */
    public Usuario findByUsername(String username) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Usuario> query = session.createQuery(
                    "FROM Usuario WHERE username = :u AND activo = true", Usuario.class);
            query.setParameter("u", username);
            return query.uniqueResult();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Busca usuario por email (solo activos).
     * 
     * @param email dirección de correo.
     * @return Usuario o null.
     */
    public Usuario findByEmail(String email) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Usuario> query = session.createQuery(
                    "FROM Usuario WHERE email = :email AND activo = true", Usuario.class);
            query.setParameter("email", email);
            return query.uniqueResult();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Busca usuario por ID.
     * 
     * @param id clave primaria.
     * @return Usuario o null.
     */
    public Usuario findById(Integer id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(Usuario.class, id);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Valida login: username + password hasheada + activo=true.
     * 
     * @param username nombre de usuario.
     * @param rawPassword contraseña plana.
     * @return Usuario autenticado o null.
     */
    public Usuario login(String username, String rawPassword) {
        Usuario usuario = findByUsername(username);
        if (usuario != null && usuario.isActivo()) {
            PasswordEncoderUtil encoder = new PasswordEncoderUtil();
            if (encoder.matches(rawPassword, usuario.getPassword())) {
                return usuario;
            }
        }
        return null;
    }

    /**
     * Cuenta usuarios totales (activos + inactivos).
     * 
     * @return número total de registros.
     */
    public long contarTotal() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Long resultado = session.createQuery("SELECT COUNT(u) FROM Usuario u", Long.class).uniqueResult();
            return resultado != null ? resultado : 0L;
        }
    }

    /**
     * Cuenta usuarios activos.
     * 
     * @return número de usuarios con activo = true.
     */
    public long contarActivos() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Long resultado = session.createQuery("SELECT COUNT(u) FROM Usuario u WHERE u.activo = true", Long.class).uniqueResult();
            return resultado != null ? resultado : 0L;
        }
    }

    /**
     * Lista usuarios inactivos.
     * 
     * @return lista de usuarios dados de baja.
     */
    public List<Usuario> listarInactivos() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM Usuario WHERE activo = false", Usuario.class).list();
        }
    }
}
