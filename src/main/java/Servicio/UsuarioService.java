/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Servicio;

import DAO.UsuarioDAO;
import Util.HibernateUtil;
import org.hibernate.Session;
import Modelo.Usuario;
import Util.PasswordEncoderUtil;

/**
 * Servicio de negocio para operaciones de usuarios.
 * Maneja la lógica de registro, login y búsqueda, delegando persistencia al UsuarioDAO.
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
        UsuarioDAO dao = new UsuarioDAO();
        if (dao.findByUsername(username) != null) {
            return null; // Usuario ya existe
        }

        // Encriptar contraseña
        String passHash = passwordEncoder.encode(password);
        Usuario nuevo = new Usuario(username, passHash, email, nombre, apellidos);
        dao.insert(nuevo);
        return nuevo;
    }

    /**
     * Valida las credenciales de login.
     *
     * @param username Nombre de usuario.
     * @param password Contraseña en texto plano.
     * @return Usuario autenticado o null si falla.
     */
    public Usuario login(String username, String password) {
        UsuarioDAO dao = new UsuarioDAO();
        Usuario usuario = dao.findByUsername(username);
        
        if (usuario != null && passwordEncoder.matches(password, usuario.getPassword())) {
            // Actualizar última conexión
            usuario.setUltimaConexion(java.time.LocalDateTime.now());
            dao.update(usuario); 
            return usuario;
        }
        return null;
    }

    /**
     * Busca usuario por ID (para pantallas de perfil).
     *
     * @param id ID del usuario.
     * @return Usuario encontrado o null si no existe.
     */
    public Usuario findById(Integer id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(Usuario.class, id);
        } catch (Exception e) {
            System.err.println("Error buscando usuario: " + e.getMessage());
            return null;
        }
    }
}
