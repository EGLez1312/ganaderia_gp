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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Ventana de registro de nuevos usuarios.
 * Valida campos, comprueba duplicados y encripta contraseñas.
 * 
 * @author Elena González
 * @version 1.0
 */
public class RegistroFrame extends JFrame {
    private LoginFrame parent;  // Referencia al login para comunicación
    private JTextField txtUsername, txtEmail, txtNombre, txtApellidos;
    private JPasswordField txtPassword, txtRepeatPassword;
    private UsuarioDAO usuarioDAO;
    private PasswordEncoderUtil encoder;

    public RegistroFrame(LoginFrame parent) {
        this.parent = parent;
        initComponents();
        setLocationRelativeTo(null);
    }

    private void initComponents() {
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setTitle("Registro - Ganadería GP");
        setSize(450, 450);
        setLayout(new GridBagLayout());
        
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(10, 10, 10, 10);
        c.anchor = GridBagConstraints.WEST;

        int fila = 0;
        
        // Username
        c.gridx = 0; c.gridy = fila++;
        add(new JLabel("Username *:"), c);
        c.gridx = 1;
        txtUsername = new JTextField(20);
        txtUsername.setToolTipText("Nombre único sin espacios");
        add(txtUsername, c);
        
        // Email
        c.gridx = 0; c.gridy = fila++;
        add(new JLabel("Email *:"), c);
        c.gridx = 1;
        txtEmail = new JTextField(20);
        txtEmail.setToolTipText("Para recuperación de contraseña");
        add(txtEmail, c);
        
        // Nombre
        c.gridx = 0;
        c.gridy = fila++;
        add(new JLabel("Nombre:"), c);
        c.gridx = 1;
        txtNombre = new JTextField(20);
        add(txtNombre, c);

        // Apellidos
        c.gridx = 0;
        c.gridy = fila++;
        add(new JLabel("Apellidos:"), c);
        c.gridx = 1;
        txtApellidos = new JTextField(20);
        add(txtApellidos, c);

        // Password
        c.gridx = 0; c.gridy = fila++;
        add(new JLabel("Contraseña *:"), c);
        c.gridx = 1;
        txtPassword = new JPasswordField(20);
        txtPassword.setToolTipText("Mínimo 6 caracteres");
        add(txtPassword, c);
        
        // Repeat Password
        c.gridx = 0; c.gridy = fila++;
        add(new JLabel("Repetir *:"), c);
        c.gridx = 1;
        txtRepeatPassword = new JPasswordField(20);
        add(txtRepeatPassword, c);
        
        // Botones
        c.gridx = 0; c.gridy = fila; c.gridwidth = 2;
        c.fill = GridBagConstraints.HORIZONTAL;
        JPanel pnlBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton btnRegistrar = new JButton("REGISTRAR");
        btnRegistrar.setMnemonic('R');
        getRootPane().setDefaultButton(btnRegistrar);  // Enter = Registrar
        btnRegistrar.addActionListener(e -> guardarUsuario());

        JButton btnCancelar = new JButton("Cancelar");
        btnCancelar.setMnemonic('C');
        btnCancelar.addActionListener(e -> dispose());

        pnlBotones.add(btnRegistrar);
        pnlBotones.add(btnCancelar);

        add(pnlBotones, c);
    }

    /**
     * Valida y guarda nuevo usuario en BD.
     */
    private void guardarUsuario() {
        String username = txtUsername.getText().trim();
        String email = txtEmail.getText().trim();
        String nombre = txtNombre.getText().trim();
        String apellidos = txtApellidos.getText().trim();
        String password = new String(txtPassword.getPassword()).trim();
        String repeatPassword = new String(txtRepeatPassword.getPassword()).trim();

        // Validaciones
        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Username, email y contraseña obligatorios", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (!password.equals(repeatPassword)) {
            JOptionPane.showMessageDialog(this, "Las contraseñas no coinciden", "Error", JOptionPane.ERROR_MESSAGE);
            txtPassword.requestFocus();
            return;
        }
        if (password.length() < 6) {
            JOptionPane.showMessageDialog(this, "Mínimo 6 caracteres", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            usuarioDAO = new UsuarioDAO();
            encoder = new PasswordEncoderUtil();

            // Comprobar duplicado
            if (usuarioDAO.findByUsername(username) != null) {
                JOptionPane.showMessageDialog(this, "Username ya existe", "Error", JOptionPane.ERROR_MESSAGE);
                txtUsername.requestFocus();
                return;
            }

            // Crear usuario
            Usuario nuevo = new Usuario();
            nuevo.setUsername(username);
            nuevo.setEmail(email);
            nuevo.setNombre(nombre);      // ← NUEVO
            nuevo.setApellidos(apellidos); // ← NUEVO
            nuevo.setPassword(encoder.encode(password));
            nuevo.setActivo(true);

            usuarioDAO.insert(nuevo);
            
            JOptionPane.showMessageDialog(this, "¡Usuario registrado!", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            
            // Rellenar login padre
            parent.getTxtUsername().setText(username);
            parent.getTxtPassword().requestFocus();
            
            dispose(); // Cerrar registro
            
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error BD: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
 
    // Getters para LoginFrame
    public LoginFrame getParent() {
        return parent;
    }


    public static void main(String[] args) {
        new RegistroFrame(null).setVisible(true);
    }
}