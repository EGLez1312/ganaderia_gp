/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Vista;

import DAO.UsuarioDAO;
import Modelo.Usuario;
import Util.PasswordEncoderUtil;
import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;

/**
 * Panel edición perfil usuario logueado.
 * Permite modificar nombre, apellidos, email y contraseña opcionalmente.
 */
public class PerfilPanel extends JPanel {
    
    /** Usuario autenticado que edita su perfil */
    private Usuario usuario;
    
    /** Campos de entrada del formulario */
    private JTextField txtNombre, txtApellidos, txtEmail;
    private JPasswordField txtPassword;
    
    /** DAO para persistir cambios del perfil */
    private UsuarioDAO usuarioDAO; // Instancia única para el panel

    /**
     * Constructor principal del panel de perfil.
     * Valida usuario no nulo e inicializa componentes y datos.
     * 
     * @param usuario usuario autenticado cuya información se editará
     * @throws IllegalArgumentException si el usuario es null
     */
    public PerfilPanel(Usuario usuario) {
        if (usuario == null) {
            throw new IllegalArgumentException("El usuario no puede ser nulo");
        }
        this.usuario = usuario;
        this.usuarioDAO = new UsuarioDAO(); // Inicializamos el DAO aquí
        initComponents();
        cargarDatos();
    }

    /**
     * Inicializa la interfaz usando BorderLayout + GridBagLayout.
     * Crea formulario profesional con username de solo lectura y campos editables.
     */
    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));

        // Título con margen inferior
        JLabel lblTitulo = new JLabel("Mi Perfil", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 22));
        lblTitulo.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        add(lblTitulo, BorderLayout.NORTH);

        // Formulario principal
        JPanel pnlForm = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(8, 8, 8, 8);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;

        // Fila 0: Username (solo lectura)
        c.gridy = 0;
        pnlForm.add(new JLabel("Username:"), c);
        c.gridx = 1;
        JLabel lblUsername = new JLabel(usuario.getUsername());
        lblUsername.setFont(new Font("Arial", Font.ITALIC, 14));
        lblUsername.setForeground(Color.DARK_GRAY);
        pnlForm.add(lblUsername, c);

        // Fila 1: Nombre
        c.gridy = 1; c.gridx = 0;
        pnlForm.add(new JLabel("Nombre:"), c);
        c.gridx = 1;
        txtNombre = new JTextField(20);
        pnlForm.add(txtNombre, c);

        // Fila 2: Apellidos
        c.gridy = 2; c.gridx = 0;
        pnlForm.add(new JLabel("Apellidos:"), c);
        c.gridx = 1;
        txtApellidos = new JTextField(20);
        pnlForm.add(txtApellidos, c);

        // Fila 3: Email
        c.gridy = 3; c.gridx = 0;
        pnlForm.add(new JLabel("Email:"), c);
        c.gridx = 1;
        txtEmail = new JTextField(20);
        pnlForm.add(txtEmail, c);

        // Fila 4: Nueva contraseña (opcional)
        c.gridy = 4; c.gridx = 0;
        pnlForm.add(new JLabel("Nueva contraseña:"), c);
        c.gridx = 1;
        txtPassword = new JPasswordField(20);
        txtPassword.setToolTipText("Dejar en blanco para mantener la actual");
        pnlForm.add(txtPassword, c);

        // Fila 5: Botón Guardar
        c.gridy = 5; 
        c.gridx = 0;
        c.gridwidth = 2;
        c.insets = new Insets(20, 8, 8, 8);
        JButton btnGuardar = new JButton("Actualizar Perfil");
        btnGuardar.setFont(new Font("Arial", Font.BOLD, 14));
        btnGuardar.setMnemonic('G');
        btnGuardar.addActionListener(e -> guardarPerfil());
        pnlForm.add(btnGuardar, c);

        add(pnlForm, BorderLayout.CENTER);
    }

    /**
     * Carga los datos actuales del usuario en los campos del formulario.
     * Limpia el campo de contraseña por seguridad.
     */
    private void cargarDatos() {
        txtNombre.setText(usuario.getNombre());
        txtApellidos.setText(usuario.getApellidos());
        txtEmail.setText(usuario.getEmail());
        txtPassword.setText(""); 
    }

    /**
     * Guarda los cambios del perfil del usuario en la base de datos. Implementa
     * validación robusta de campos, cifrado seguro de contraseña y limpieza de
     * memoria.
     */
    private void guardarPerfil() {
        String nombre = txtNombre.getText().trim();
        String apellidos = txtApellidos.getText().trim();
        String email = txtEmail.getText().trim();

        // Validación
        if (nombre.isEmpty() || apellidos.isEmpty() || !email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            JOptionPane.showMessageDialog(this, "Por favor, completa todos los campos correctamente.", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            // Actualizar datos básicos
            usuario.setNombre(nombre);
            usuario.setApellidos(apellidos);
            usuario.setEmail(email);

            // Nueva contraseña opcional
            char[] passChars = txtPassword.getPassword();
            if (passChars.length > 0) {
                PasswordEncoderUtil encoder = new PasswordEncoderUtil();  // Instancia separada
                String passwordCifrada = encoder.encode(new String(passChars));
                usuario.setPassword(passwordCifrada);
                java.util.Arrays.fill(passChars, '0');  // Limpieza memoria
            }

            // Persistir
            usuarioDAO.update(usuario);
            JOptionPane.showMessageDialog(this, "✅ Perfil actualizado correctamente");
            txtPassword.setText("");

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "❌ Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}

