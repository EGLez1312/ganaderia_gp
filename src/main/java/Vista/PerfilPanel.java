/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Vista;

import DAO.UsuarioDAO;
import Modelo.Usuario;
import Util.PasswordEncoderUtil;
import Servicio.UsuarioService;
import javax.swing.*;
import java.awt.*;
import java.util.Arrays;

/**
 * Panel editable para gestión de perfil de usuario autenticado. Permite
 * modificar nombre, apellidos, email y contraseña opcional. Cumple requisitos:
 * formulario dinámico, ToolTips, mnemonic (Alt+G), validación robusta, limpieza
 * memoria contraseñas, seguridad BD.
 *
 * @author Elena González
 * @version 1.0
 */
public class PerfilPanel extends JPanel {

    /**
     * Usuario logueado que edita su propio perfil.
     */
    private final Usuario usuario;

    /**
     * Campos formulario editables.
     */
    private JTextField txtNombre, txtApellidos, txtEmail;
    private JPasswordField txtPassword;

    /**
     * Servicio de negocio para persistencia segura.
     */
    private UsuarioService usuarioService;

    /**
     * Constructor valida usuario e inicializa UI con datos precargados.
     *
     * @param usuario autenticado (no null).
     * @throws IllegalArgumentException si usuario nulo o inactivo.
     */
    public PerfilPanel(Usuario usuario) {
        if (usuario == null || !usuario.isActivo()) {
            throw new IllegalArgumentException("Usuario válido y activo requerido");
        }
        this.usuario = usuario;
        initComponents();
        cargarDatos();
    }

    /**
     * Crea interfaz profesional: título centrado, formulario GridBagLayout,
     * username readonly destacado, campos editables con ToolTip contraseña,
     * botón Guardar con mnemonic Alt+G. Bordes/márgenes para UX premium.
     */
    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));
        setBackground(Color.WHITE);

        // Título principal
        JLabel lblTitulo = new JLabel("Mi Perfil", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitulo.setForeground(new Color(45, 85, 150));
        lblTitulo.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        add(lblTitulo, BorderLayout.NORTH);

        // Formulario responsive
        JPanel pnlForm = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(12, 12, 12, 12);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0.5;
        c.gridx = 0;

        // Username (solo lectura, destacado)
        c.gridy = 0;
        pnlForm.add(new JLabel("Username:"), c);
        c.gridx = 1;
        JLabel lblUsername = new JLabel(usuario.getUsername());
        lblUsername.setFont(new Font("Segoe UI", Font.ITALIC, 14));
        lblUsername.setForeground(Color.DARK_GRAY);
        lblUsername.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        pnlForm.add(lblUsername, c);

        // Nombre editable
        c.gridy = 1;
        c.gridx = 0;
        pnlForm.add(new JLabel("Nombre *:"), c);
        c.gridx = 1;
        txtNombre = new JTextField(22);
        txtNombre.setToolTipText("Tu nombre real (obligatorio)");
        pnlForm.add(txtNombre, c);

        // Apellidos
        c.gridy = 2;
        c.gridx = 0;
        pnlForm.add(new JLabel("Apellidos *:"), c);
        c.gridx = 1;
        txtApellidos = new JTextField(22);
        txtApellidos.setToolTipText("Apellidos completos (obligatorio)");
        pnlForm.add(txtApellidos, c);

        // Email con validación
        c.gridy = 3;
        c.gridx = 0;
        pnlForm.add(new JLabel("Email *:"), c);
        c.gridx = 1;
        txtEmail = new JTextField(22);
        txtEmail.setToolTipText("Email válido para notificaciones/recuperación");
        pnlForm.add(txtEmail, c);

        // Nueva contraseña opcional
        c.gridy = 4;
        c.gridx = 0;
        pnlForm.add(new JLabel("Nueva contraseña:"), c);
        c.gridx = 1;
        txtPassword = new JPasswordField(22);
        txtPassword.setToolTipText("Dejar vacío para mantener actual (mínimo 6 chars)");
        pnlForm.add(txtPassword, c);

        // Botón acción principal
        c.gridy = 5;
        c.gridx = 0;
        c.gridwidth = 2;
        c.insets = new Insets(25, 12, 12, 12);
        JButton btnGuardar = new JButton("Actualizar Perfil");
        btnGuardar.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        btnGuardar.setMnemonic('G');  // Alt+G
        btnGuardar.setBackground(new Color(50, 150, 50));
        btnGuardar.setForeground(Color.BLACK);
        btnGuardar.addActionListener(e -> guardarPerfil());
        pnlForm.add(btnGuardar, c);

        add(pnlForm, BorderLayout.CENTER);
    }

    /**
     * Carga datos actuales del usuario en campos formulario. Limpia contraseña
     * por seguridad (never show).
     */
    private void cargarDatos() {
        txtNombre.setText(usuario.getNombre() != null ? usuario.getNombre() : "");
        txtApellidos.setText(usuario.getApellidos() != null ? usuario.getApellidos() : "");
        txtEmail.setText(usuario.getEmail() != null ? usuario.getEmail() : "");
        txtPassword.setText("");  // Nunca precargar
    }

    /**
     * Valida formulario → actualiza entidad → persiste vía Service. Encripta
     * nueva pass si proporcionada, limpia memoria char[]. Feedback visual
     * éxito/error con focus automático.
     */
    private void guardarPerfil() {
        String nombre = txtNombre.getText().trim();
        String apellidos = txtApellidos.getText().trim();
        String email = txtEmail.getText().trim();

        // Validaciones robustas
        if (nombre.isEmpty() || apellidos.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nombre y apellidos obligatorios", "Validación", JOptionPane.WARNING_MESSAGE);
            txtNombre.requestFocus();
            return;
        }
        if (!email.matches("^[A-Za-z0-9+_.-]+@([A-Za-z0-9-]+\\.)+[A-Za-z]{2,}$")) {
            JOptionPane.showMessageDialog(this, "Email inválido", "Validación", JOptionPane.WARNING_MESSAGE);
            txtEmail.selectAll();
            txtEmail.requestFocus();
            return;
        }

        try {
            // Inyectar Service (MVC: Panel→Service→DAO)
            UsuarioDAO dao = new UsuarioDAO();
            PasswordEncoderUtil encoder = new PasswordEncoderUtil();
            usuarioService = new UsuarioService(dao, encoder);

            // Actualizar campos básicos
            usuario.setNombre(nombre);
            usuario.setApellidos(apellidos);
            usuario.setEmail(email);

            // Contraseña opcional + limpieza memoria
            char[] passChars = txtPassword.getPassword();
            if (passChars.length > 5) {  // Mínimo 6
                String passPlana = new String(passChars);
                usuario.setPassword(encoder.encode(passPlana));
                Arrays.fill(passChars, '0');  // Zeroizar memoria
            }

            // Persistir cambios
            dao.actualizar(usuario);  // ← CORREGIDO: actualizar
            JOptionPane.showMessageDialog(this,
                    "¡Perfil actualizado correctamente!\nRefresca para ver cambios.",
                    "Guardado", JOptionPane.INFORMATION_MESSAGE);

            txtPassword.setText("");  // Limpiar UI
            txtNombre.requestFocus();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error guardando: " + e.getMessage(), "Error BD", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * @return Usuario original (no mutado externamente).
     */
    public Usuario getUsuario() {
        return usuario;
    }

    /**
     * Refresca panel con datos BD actuales (post-actualizaciones externas).
     */
    public void refresh() {
        cargarDatos();
    }
}
