/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Servicio;

import DAO.UsuarioDAO;
import Modelo.Usuario;
import Util.PasswordEncoderUtil;
import java.time.LocalDateTime;

/**
 * Servicio de negocio para operaciones de usuarios. Implementa inyección de
 * dependencias con patrón Factory/Builder para DAOs. Separa completamente
 * lógica de negocio de persistencia.
 *
 * @author Elena González
 * @version 1.0
 */
public class UsuarioService {

    private final UsuarioDAO dao;
    private final PasswordEncoderUtil passwordEncoder;

    public UsuarioService(UsuarioDAO dao, PasswordEncoderUtil passwordEncoder) {
        this.dao = dao;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Registra nuevo usuario validando duplicados y encriptando.
     */
    public Usuario registrar(String username, String password, String email,
            String nombre, String apellidos) {
        if (dao.findByUsername(username) != null) {
            return null;
        }
        String passHash = passwordEncoder.encode(password);
        Usuario nuevo = new Usuario(username, passHash, email, nombre, apellidos);
        dao.insertar(nuevo); 
        return nuevo;
    }

    /**
     * Login con actualización de última conexión.
     */
    public Usuario login(String username, String password) {
        Usuario usuario = dao.findByUsername(username);
        if (usuario != null && passwordEncoder.matches(password, usuario.getPassword())) {
            usuario.setUltimaConexion(LocalDateTime.now());
            dao.actualizar(usuario);  
            return usuario;
        }
        return null;
    }

    public Usuario findById(Integer id) {
        return dao.findById(id);
    }
}
