/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Vista;

import Modelo.Usuario;
import Servicio.UsuarioService;

import javax.swing.*;
import java.awt.*;

/**
 * Ventana de registro de nuevos usuarios.
 * Valida campos y gestiona errores como usuario duplicado o campos vacíos.
 * 
 * @author Elena González
 * @version 1.0
 */
public class RegistroFrame extends JDialog {

    private final LoginFrame parent;
    private final UsuarioService usuarioService = new UsuarioService();

    // Componentes
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JPasswordField txtRepeatPassword;
    private JTextField txtEmail;
    private JTextField txtNombre;
    private JTextField txtApellidos;

    /**
     * Constructor del diálogo de registro.
     *
     * @param parent Ventana padre (LoginFrame).
     */
    public RegistroFrame(LoginFrame parent) {
        super(parent, "Registro de nuevo usuario", true);
        this.parent = parent;
        initComponents();
        pack();
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));

        // Título
        JLabel lblTitulo = new JLabel("Crear cuenta nueva", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 18));
        lblTitulo.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        add(lblTitulo, BorderLayout.NORTH);

        // Formulario
        JPanel pnlForm = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;

        int fila = 0;

        // Username
        gbc.gridx = 0; gbc.gridy = fila++;
        pnlForm.add(new JLabel("Usuario *:"), gbc);
        gbc.gridx = 1;
        txtUsername = new JTextField(15);
        txtUsername.setToolTipText("Nombre de usuario único (sin espacios)");
        pnlForm.add(txtUsername, gbc);

        // Password
        gbc.gridx = 0; gbc.gridy = fila++;
        pnlForm.add(new JLabel("Contraseña *:"), gbc);
        gbc.gridx = 1;
        txtPassword = new JPasswordField(15);
        txtPassword.setToolTipText("Contraseña segura (mínimo 6 caracteres)");
        pnlForm.add(txtPassword, gbc);

        // Repetir password
        gbc.gridx = 0; gbc.gridy = fila++;
        pnlForm.add(new JLabel("Repetir contraseña *:"), gbc);
        gbc.gridx = 1;
        txtRepeatPassword = new JPasswordField(15);
        pnlForm.add(txtRepeatPassword, gbc);

        // Email
        gbc.gridx = 0; gbc.gridy = fila++;
        pnlForm.add(new JLabel("Email *:"), gbc);
        gbc.gridx = 1;
        txtEmail = new JTextField(15);
        txtEmail.setToolTipText("Email para recuperación de contraseña");
        pnlForm.add(txtEmail, gbc);

        // Nombre
        gbc.gridx = 0; gbc.gridy = fila++;
        pnlForm.add(new JLabel("Nombre:"), gbc);
        gbc.gridx = 1;
        txtNombre = new JTextField(15);
        pnlForm.add(txtNombre, gbc);

        // Apellidos
        gbc.gridx = 0; gbc.gridy = fila++;
        pnlForm.add(new JLabel("Apellidos:"), gbc);
        gbc.gridx = 1;
        txtApellidos = new JTextField(15);
        pnlForm.add(txtApellidos, gbc);

        // Botones
        gbc.gridx = 0; gbc.gridy = fila++; gbc.gridwidth = 2;
        JPanel pnlBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnGuardar = new JButton("Crear usuario");
        btnGuardar.setMnemonic('C');
        getRootPane().setDefaultButton(btnGuardar);  // Enter = Guardar

        JButton btnCancelar = new JButton("Cancelar");
        btnCancelar.setMnemonic('A');

        pnlBotones.add(btnCancelar);
        pnlBotones.add(btnGuardar);
        pnlForm.add(pnlBotones, gbc);

        add(pnlForm, BorderLayout.CENTER);

        // Listeners
        btnGuardar.addActionListener(e -> guardarUsuario());
        btnCancelar.addActionListener(e -> dispose());

        // Enter en campos ejecuta guardar
        txtApellidos.addActionListener(e -> btnGuardar.doClick());
    }

    private void guardarUsuario() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword()).trim();
        String repeatPassword = new String(txtRepeatPassword.getPassword()).trim();
        String email = txtEmail.getText().trim();
        String nombre = txtNombre.getText().trim();
        String apellidos = txtApellidos.getText().trim();

        // Validaciones
        if (username.isEmpty() || password.isEmpty() || email.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Usuario, contraseña y email son obligatorios", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!password.equals(repeatPassword)) {
            JOptionPane.showMessageDialog(this, "Las contraseñas no coinciden", "Error", JOptionPane.ERROR_MESSAGE);
            txtPassword.setText("");
            txtRepeatPassword.setText("");
            txtPassword.requestFocus();
            return;
        }

        if (password.length() < 6) {
            JOptionPane.showMessageDialog(this, "La contraseña debe tener al menos 6 caracteres", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Intentar registrar
        Usuario nuevo = usuarioService.registrar(username, password, email, nombre, apellidos);
        if (nuevo != null) {
            JOptionPane.showMessageDialog(this, "¡Usuario creado correctamente!", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            parent.getTxtUsername().setText(username);  // Pone el username en el login
            parent.getTxtPassword().requestFocus();     // Foco en contraseña del login
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "El usuario '" + username + "' ya existe", "Error", JOptionPane.ERROR_MESSAGE);
            txtUsername.setText("");
            txtUsername.requestFocus();
        }
    }
}