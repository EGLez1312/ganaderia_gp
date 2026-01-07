/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DAO;

import org.hibernate.Session;
import Modelo.Usuario;
import Util.HibernateUtil;
import org.hibernate.Transaction;

/**
 * DAO para operaciones CRUD de usuarios con Hibernate, utilizando HibernateUtil para la gestión de sesiones.
 * Sustituye la versión anterior manteniendo la misma interfaz pública.
 * 
 * @author Elena González
 * @version 1.0
 */
public class UsuarioDAO {
    
    /**
     * Busca usuario por username (para validación de duplicados).
     * Solo devuelve usuarios activos (activo = 1).
     * 
     * @param username el nombre de usuario a buscar
     * @return el usuario encontrado o null si no existe
     */
    public Usuario findByUsername(String username) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            // SQL Nativo para evitar problemas de tipos Boolean/Integer en HQL
            String sql = "SELECT * FROM usuario WHERE username = :u AND activo = 1";
            return session.createNativeQuery(sql, Usuario.class)
                          .setParameter("u", username)
                          .uniqueResult();
        }
    }

    /**
     * Inserta un nuevo usuario en la base de datos.
     * 
     * @param usuario el objeto Usuario a insertar
     */
    public void insert(Usuario usuario) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            session.persist(usuario);
            tx.commit();
        }
    }
    
    /**
     * Actualiza un usuario existente en la base de datos. Utiliza merge() para
     * sincronizar los cambios de la entidad desconectada.
     *
     * @param usuario el objeto Usuario con los datos actualizados
     */
    public void update(Usuario usuario) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            // Usamos merge para volver a "enganchar" la entidad y actualizarla
            session.merge(usuario);
            tx.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Busca un usuario por su dirección de email. Solo devuelve usuarios
     * activos (activo = 1). Utiliza SQL nativo para máxima compatibilidad con
     * Hibernate 6 y MySQL.
     *
     * @param email la dirección de email a buscar
     * @return el usuario encontrado o null si no existe
     */
    public Usuario findByEmail(String email) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            // SQL Nativo para ir sobre seguro con Hibernate 6
            String sql = "SELECT * FROM usuario WHERE email = :email AND activo = 1";
            return session.createNativeQuery(sql, Usuario.class)
                    .setParameter("email", email)
                    .uniqueResult();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
