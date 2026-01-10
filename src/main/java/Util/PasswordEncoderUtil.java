/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Utilidad singleton para encriptación/verificación segura de contraseñas. Usa
 * BCrypt (OWASP recomendado) con salt automático strength=10. Cumple
 * requisitos: "contraseña encriptada" tabla usuarios.
 *
 * @author Elena González
 * @version 1.0
 */
public class PasswordEncoderUtil {

    /**
     * Encoder BCrypt configurado con strength=10 (balance
     * seguridad/rendimiento).
     */
    private final BCryptPasswordEncoder encoder;

    /**
     * Constructor privado inicializa encoder estándar. Strength 10: ~100ms hash
     * (seguro para web/desktop).
     */
    public PasswordEncoderUtil() {
        this.encoder = new BCryptPasswordEncoder(10);
    }

    /**
     * Encripta contraseña plana generando salt único. Formato: $2a$10$hash+salt
     * (60 chars).
     *
     * @param rawPassword contraseña texto plano (UTF-8).
     * @return hash BCrypt irreversible.
     * @throws IllegalArgumentException si null/vacía.
     */
    public String encode(String rawPassword) {
        if (rawPassword == null || rawPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("Contraseña no puede ser null o vacía");
        }
        return encoder.encode(rawPassword);
    }

    /**
     * Verifica contraseña plana contra hash almacenado. Extrae salt del hash y
     * re-computa para comparar.
     *
     * @param rawPassword candidata (texto plano).
     * @param encodedPassword hash BCrypt almacenado.
     * @return true si coinciden (timing-safe).
     * @throws IllegalArgumentException si parámetros inválidos.
     */
    public boolean matches(String rawPassword, String encodedPassword) {
        if (rawPassword == null || encodedPassword == null) {
            throw new IllegalArgumentException("Parámetros no pueden ser null");
        }
        return encoder.matches(rawPassword, encodedPassword);
    }

    /**
     * Verifica si string es hash BCrypt válido (60 chars formato correcto).
     *
     * @param hash candidato.
     * @return true si formato BCrypt válido.
     */
    public boolean isValidHash(String hash) {
        return hash != null && hash.matches("^\\$2[ayb]?\\$\\d{2}\\$[\\./A-Za-z0-9]{53}");
    }
}
