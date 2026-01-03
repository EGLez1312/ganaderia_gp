/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Servicio;

import Modelo.Usuario;
import Util.PasswordEncoderUtil;
import Util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

/**
 * Servicio que gestiona las operaciones CRUD de usuarios con Hibernate.
 * Incluye registro, login y validación de credenciales encriptadas.
 * 
 * @author Elena González
 * @version 1.0
 */
public class UsuarioService {
    private final PasswordEncoderUtil passwordEncoder = new PasswordEncoderUtil();

    /**
     * Registra un nuevo usuario en la base de datos.
     * Encripta la contraseña y valida que el username no exista.
     *
     * @param username Nombre de usuario único.
     * @param password Contraseña en texto plano.
     * @param email Email del usuario.
     * @param nombre Nombre real.
     * @param apellidos Apellidos.
     * @return El usuario creado o null si ya existía el username.
     */
    public Usuario registrar(String username, String password, String email,
                             String nombre, String apellidos) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();

            // Verificar si ya existe
            Query<Usuario> query = session.createQuery("FROM Usuario WHERE username = :username", Usuario.class);
            query.setParameter("username", username);
            Usuario existente = query.uniqueResult();

            if (existente != null) {
                return null;  // Usuario ya existe
            }

            // Crear nuevo usuario
            Usuario nuevo = new Usuario(username, passwordEncoder.encode(password), email, nombre, apellidos);
            session.persist(nuevo);
            tx.commit();

            return nuevo;
        } catch (Exception e) {
            System.err.println("Error al registrar usuario: " + e.getMessage());
            return null;
        }
    }

    /**
     * Valida las credenciales de login.
     *
     * @param username Nombre de usuario.
     * @param password Contraseña en texto plano.
     * @return Usuario autenticado o null si falla.
     */
    public Usuario login(String username, String password) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Usuario> query = session.createQuery("FROM Usuario WHERE username = :username AND activo = true", Usuario.class);
            query.setParameter("username", username);
            Usuario usuario = query.uniqueResult();

            if (usuario != null && passwordEncoder.matches(password, usuario.getPassword())) {
                // Actualizar última conexión
                usuario.setUltimaConexion(java.time.LocalDateTime.now());
                session.beginTransaction();
                session.merge(usuario);
                session.getTransaction().commit();
                return usuario;
            }
            return null;
        } catch (Exception e) {
            System.err.println("Error en login: " + e.getMessage());
            return null;
        }
    }

    /**
     * Busca usuario por ID (para pantallas de perfil).
     */
    public Usuario findById(Integer id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(Modelo.Usuario.class, id);
        } catch (Exception e) {
            System.err.println("Error buscando usuario: " + e.getMessage());
            return null;
        }
    }
        
}