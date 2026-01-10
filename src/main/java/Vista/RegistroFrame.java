/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Vista;

import DAO.UsuarioDAO;
import Modelo.Usuario;
import Servicio.UsuarioService;
import Util.PasswordEncoderUtil;
import javax.swing.*;
import java.awt.*;

/**
 * Ventana de registro de nuevos usuarios para Ganadería App. Implementa
 * validaciones completas, ToolTips, mnemonics (Alt+R/C), Enter=Registrar y
 * comunicación con LoginFrame padre. Usa UsuarioService para MVC correcto.
 *
 * @author Elena González
 * @version 1.0
 */
public class RegistroFrame extends JFrame {

    /**
     * Referencia al LoginFrame padre para rellenar campos tras registro
     * exitoso.
     */
    private LoginFrame parent;

    /**
     * Campos de entrada de formulario.
     */
    private JTextField txtUsername, txtEmail, txtNombre, txtApellidos;
    private JPasswordField txtPassword, txtRepeatPassword;

    /**
     * Servicio de negocio inyectado para registro/encriptación.
     */
    private UsuarioService usuarioService;

    /**
     * Constructor principal. Inicializa UI y centra ventana.
     *
     * @param parent LoginFrame origen que abre registro.
     */
    public RegistroFrame(LoginFrame parent) {
        this.parent = parent;
        initComponents();
        setLocationRelativeTo(null);
    }

    /**
     * Inicializa todos los componentes Swing usando GridBagLayout responsive.
     * Configura ToolTips, mnemonics y botón por defecto (Enter=Registrar).
     * Cumple requisitos: acceso teclado, info contextual (ToolTips).
     */
    private void initComponents() {
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setTitle("Registro - Ganadería GP");
        setSize(450, 450);
        setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(10, 10, 10, 10);
        c.anchor = GridBagConstraints.WEST;

        int fila = 0;

        // Username requerido
        c.gridx = 0;
        c.gridy = fila++;
        add(new JLabel("Username *:"), c);
        c.gridx = 1;
        txtUsername = new JTextField(20);
        txtUsername.setToolTipText("Nombre único sin espacios (ej: admin123)");
        add(txtUsername, c);

        // Email requerido
        c.gridx = 0;
        c.gridy = fila++;
        add(new JLabel("Email *:"), c);
        c.gridx = 1;
        txtEmail = new JTextField(20);
        txtEmail.setToolTipText("Para recuperación de contraseña (ej: user@ganaderia.es)");
        add(txtEmail, c);

        // Nombre opcional
        c.gridx = 0;
        c.gridy = fila++;
        add(new JLabel("Nombre:"), c);
        c.gridx = 1;
        txtNombre = new JTextField(20);
        txtNombre.setToolTipText("Nombre real del usuario");
        add(txtNombre, c);

        // Apellidos opcionales
        c.gridx = 0;
        c.gridy = fila++;
        add(new JLabel("Apellidos:"), c);
        c.gridx = 1;
        txtApellidos = new JTextField(20);
        txtApellidos.setToolTipText("Apellidos completos");
        add(txtApellidos, c);

        // Contraseña requerida
        c.gridx = 0;
        c.gridy = fila++;
        add(new JLabel("Contraseña *:"), c);
        c.gridx = 1;
        txtPassword = new JPasswordField(20);
        txtPassword.setToolTipText("Mínimo 6 caracteres seguros");
        add(txtPassword, c);

        // Repetición requerida
        c.gridx = 0;
        c.gridy = fila++;
        add(new JLabel("Repetir *:"), c);
        c.gridx = 1;
        txtRepeatPassword = new JPasswordField(20);
        txtRepeatPassword.setToolTipText("Debe coincidir exactamente");
        add(txtRepeatPassword, c);

        // Panel botones con mnemonics
        c.gridx = 0;
        c.gridy = fila;
        c.gridwidth = 2;
        c.fill = GridBagConstraints.HORIZONTAL;
        JPanel pnlBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton btnRegistrar = new JButton("REGISTRAR");
        btnRegistrar.setMnemonic('R');  // Alt+R
        getRootPane().setDefaultButton(btnRegistrar);  // Enter = ejecutar
        btnRegistrar.addActionListener(e -> guardarUsuario());

        JButton btnCancelar = new JButton("Cancelar");
        btnCancelar.setMnemonic('C');  // Alt+C
        btnCancelar.addActionListener(e -> dispose());

        pnlBotones.add(btnRegistrar);
        pnlBotones.add(btnCancelar);
        add(pnlBotones, c);
    }

    /**
     * Procesa formulario: valida campos obligatorios/duplicados/coincidencia,
     * encripta vía Service y guarda en BD. Rellena LoginFrame padre si OK.
     * Maneja todos los errores requeridos (campos vacíos, duplicados).
     */
    private void guardarUsuario() {
        String username = txtUsername.getText().trim();
        String email = txtEmail.getText().trim();
        String nombre = txtNombre.getText().trim();
        String apellidos = txtApellidos.getText().trim();
        String password = new String(txtPassword.getPassword()).trim();
        String repeatPassword = new String(txtRepeatPassword.getPassword()).trim();

        // Validaciones obligatorias (cumple requisitos gestión errores)
        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Username, email y contraseña obligatorios", "Error", JOptionPane.ERROR_MESSAGE);
            txtUsername.requestFocus();
            return;
        }
        if (!password.equals(repeatPassword)) {
            JOptionPane.showMessageDialog(this, "Las contraseñas no coinciden", "Error", JOptionPane.ERROR_MESSAGE);
            txtPassword.requestFocus();
            return;
        }
        if (password.length() < 6) {
            JOptionPane.showMessageDialog(this, "Contraseña mínima 6 caracteres", "Error", JOptionPane.ERROR_MESSAGE);
            txtPassword.requestFocus();
            return;
        }

        try {
            // Inyección manual Service (MVC: Vista→Service→DAO)
            UsuarioDAO dao = new UsuarioDAO();
            PasswordEncoderUtil encoder = new PasswordEncoderUtil();
            usuarioService = new UsuarioService(dao, encoder);

            // Registrar delega encriptación/validación duplicados
            Usuario nuevo = usuarioService.registrar(username, password, email, nombre, apellidos);
            if (nuevo != null) {
                JOptionPane.showMessageDialog(this, "¡Usuario registrado correctamente!", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                // Comunicación padre-hijo: rellenar login
                parent.getTxtUsername().setText(username);
                parent.getTxtPassword().requestFocus();
                dispose();  // Cerrar modal
            } else {
                JOptionPane.showMessageDialog(this, "Username ya existe. Elige otro.", "Duplicado", JOptionPane.ERROR_MESSAGE);
                txtUsername.selectAll();
                txtUsername.requestFocus();
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error de base de datos: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * @return LoginFrame padre que originó esta ventana.
     */
    public LoginFrame getParent() {
        return parent;
    }

    /**
     * Método main para testing independiente.
     *
     * @param args argumentos de línea comandos (no usados).
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new RegistroFrame(null).setVisible(true));
    }
}
