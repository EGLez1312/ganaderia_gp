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
 * Implementa inyección de dependencias con patrón Factory/Builder para DAOs.
 * Separa completamente lógica de negocio de persistencia.
 * 
 * @author Elena González
 * @version 1.0
 */
public class UsuarioService {
    
    /** DAO para operaciones de persistencia de usuarios */
    private final UsuarioDAO dao;
    
    /** Utilidad para codificación/verificación de contraseñas */
    private final PasswordEncoderUtil passwordEncoder;

    /**
     * Constructor con inyección de dependencias.
     * Permite testing y configuración externa de DAOs/encoders.
     * 
     * @param dao DAO para operaciones CRUD de usuarios
     * @param passwordEncoder utilidad para hash de contraseñas BCrypt
     */
    public UsuarioService(UsuarioDAO dao, PasswordEncoderUtil passwordEncoder) {
        this.dao = dao;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Registra un nuevo usuario en el sistema.
     * Valida unicidad de username y encripta contraseña antes de persistir.
     * 
     * @param username nombre de usuario único
     * @param password contraseña en texto plano
     * @param email dirección de correo electrónico
     * @param nombre nombre real del usuario
     * @param apellidos apellidos del usuario
     * @return usuario creado o null si username ya existe
     */
    public Usuario registrar(String username, String password, String email,
                             String nombre, String apellidos) {
        // Validación de unicidad delegada al DAO
        if (dao.findByUsername(username) != null) {
            return null; 
        }

        // Encriptación segura de contraseña
        String passHash = passwordEncoder.encode(password);
        Usuario nuevo = new Usuario(username, passHash, email, nombre, apellidos);
        
        dao.insert(nuevo);
        return nuevo;
    }

    /**
     * Autentica usuario y actualiza última conexión.
     * Verifica credenciales y actualiza timestamp de actividad.
     * 
     * @param username nombre de usuario
     * @param password contraseña en texto plano
     * @return usuario autenticado o null si falla
     */
    public Usuario login(String username, String password) {
        // Buscar usuario activo
        Usuario usuario = dao.findByUsername(username);
        
        // Verificar credenciales + actualizar actividad
        if (usuario != null && passwordEncoder.matches(password, usuario.getPassword())) {
            usuario.setUltimaConexion(java.time.LocalDateTime.now());
            dao.update(usuario); 
            return usuario;
        }
        return null;
    }

    /**
     * Busca usuario por ID primario.
     * Delega completamente al DAO de persistencia.
     * 
     * @param id identificador único del usuario
     * @return usuario encontrado o null
     */
    public Usuario findById(Integer id) {
        return dao.findById(id);
    }
}

