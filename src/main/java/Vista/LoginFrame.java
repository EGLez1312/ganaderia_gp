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
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * Ventana principal de login de la aplicación de gestión ganadera.
 * Cumple con los requisitos: 3 botones (Acceso, Registro, Recuperar contraseña).
 * 
 * @author Elena González
 * @version 1.0
 */
public class LoginFrame extends JFrame {

    private final UsuarioService usuarioService;

    // Componentes
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton btnAcceso;
    private JButton btnRegistro;
    private JButton btnRecuperar;
    
    // Gestión de ventanas hijas
    private RegistroFrame registroFrame;
    private RecuperarPasswordFrame recuperarFrame;

    /**
     * Constructor que inicializa la ventana de login.
     */
    public LoginFrame() {
        this.usuarioService = new UsuarioService(new UsuarioDAO(), new PasswordEncoderUtil());
        setTitle("Ganadería GP - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        initComponents();
        pack();
        setLocationRelativeTo(null);
        btnAcceso.requestFocusInWindow();  // Foco en botón acceso
    }

    /**
     * Inicializa todos los componentes de la ventana de login.
     */
    private void initComponents() {
        setLayout(new BorderLayout(20, 20));

        // Panel título
        JLabel lblTitulo = new JLabel("Gestor de Rebaños Ovinos", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitulo.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        add(lblTitulo, BorderLayout.NORTH);

        // Panel formulario
        JPanel pnlForm = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;

        // Username
        gbc.gridx = 0; gbc.gridy = 0;
        pnlForm.add(new JLabel("Usuario:"), gbc);
        gbc.gridx = 1;
        txtUsername = new JTextField(15);
        txtUsername.setToolTipText("Introduce tu nombre de usuario");
        pnlForm.add(txtUsername, gbc);

        // Password
        gbc.gridx = 0; gbc.gridy = 1;
        pnlForm.add(new JLabel("Contraseña:"), gbc);
        gbc.gridx = 1;
        txtPassword = new JPasswordField(15);
        txtPassword.setToolTipText("Introduce tu contraseña");
        pnlForm.add(txtPassword, gbc);

        // Botones
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        JPanel pnlBotones = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnAcceso = new JButton("Acceder");
        btnRegistro = new JButton("Registrarse");
        btnRecuperar = new JButton("Recuperar contraseña");

        // Mnemónicos (Alt+A, Alt+R, Alt+C)
        btnAcceso.setMnemonic('A');
        btnRegistro.setMnemonic('R');
        btnRecuperar.setMnemonic('C');

        // Enter ejecuta Acceder
        getRootPane().setDefaultButton(btnAcceso);

        pnlBotones.add(btnAcceso);
        pnlBotones.add(btnRegistro);
        pnlBotones.add(btnRecuperar);

        pnlForm.add(pnlBotones, gbc);
        add(pnlForm, BorderLayout.CENTER);

        // Listeners
        configurarListeners();
    }

    /**
     * Configura los ActionListener y KeyListener de la ventana.
     */
    private void configurarListeners() {
        // Botón Acceder
        btnAcceso.addActionListener(e -> realizarLogin());

        // Enter en cualquier campo ejecuta login
        txtUsername.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    realizarLogin();
                }
            }
        });

        txtPassword.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    realizarLogin();
                }
            }
        });

        // Botón Registro
        btnRegistro.addActionListener(e -> abrirRegistro());

        // Botón Recuperar contraseña
        btnRecuperar.addActionListener(e -> abrirRecuperarPassword());
    }

    /**
     * Realiza el proceso de login.
     */
    private void realizarLogin() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Usuario y contraseña obligatorios", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Usuario usuario = usuarioService.login(username, password);
        if (usuario != null) {
            JOptionPane.showMessageDialog(this, "¡Bienvenido " + usuario.getNombre() + "!");
            // Abrir ventana principal
            new PrincipalFrame(usuario).setVisible(true);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Usuario o contraseña incorrectos", "Login fallido", JOptionPane.ERROR_MESSAGE);
            txtPassword.setText("");
            txtUsername.requestFocus();
        }
    }

    /**
     * Abre RegistroFrame (máximo 1 instancia).
     */
    private void abrirRegistro() {
        if (registroFrame == null || !registroFrame.isVisible()) {
            registroFrame = new RegistroFrame(this);
            registroFrame.setVisible(true);
        } else {
            registroFrame.toFront();
        }
    }

    /**
     * Abre RecuperarPasswordFrame (máximo 1 instancia).
     */
    private void abrirRecuperarPassword() {
        if (recuperarFrame == null || !recuperarFrame.isVisible()) {
            recuperarFrame = new RecuperarPasswordFrame(this);
            recuperarFrame.setVisible(true);
        } else {
            recuperarFrame.toFront();
        }
    }

    /**
     * Devuelve el campo de texto del nombre de usuario.
     *
     * @return JTextField con el username introducido.
     */
    public JTextField getTxtUsername() {
        return txtUsername;
    }

    /**
     * Establece el texto del campo de usuario.
     *
     * @param txtUsername Nuevo valor para el campo username.
     */
    public void setTxtUsername(JTextField txtUsername) {
        this.txtUsername = txtUsername;
    }

    /**
     * Devuelve el campo de contraseña del formulario.
     *
     * @return JPasswordField con la contraseña introducida.
     */
    public JPasswordField getTxtPassword() {
        return txtPassword;
    }

    /**
     * Establece el texto del campo de contraseña.
     *
     * @param txtPassword Nuevo valor para el campo password.
     */
    public void setTxtPassword(JPasswordField txtPassword) {
        this.txtPassword = txtPassword;
    }
}