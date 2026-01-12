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
import Util.I18nUtil;
import java.util.Locale;

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
    private JLabel lblTitulo;
    private JPanel pnlForm;
    private JPanel pnlIdioma;
    private JLabel lblUsuario;
    private JLabel lblPassword;
   
    // Gestión de ventanas hijas
    private RegistroFrame registroFrame;
    private RecuperarPasswordFrame recuperarFrame;

    /**
     * Constructor que inicializa la ventana de login.
     */
    public LoginFrame() {
        this.usuarioService = new UsuarioService(new UsuarioDAO(), new PasswordEncoderUtil());
     
        // Español por defecto
        java.util.Locale.setDefault(new java.util.Locale("es", "ES"));

        setTitle(I18nUtil.get("login.title"));
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
        lblTitulo = new JLabel(I18nUtil.get("login.titulo"));
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitulo.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        add(lblTitulo, BorderLayout.NORTH);

        // Panel formulario
        pnlForm = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;

        // Username
        gbc.gridx = 0; gbc.gridy = 0;
        lblUsuario = new JLabel(I18nUtil.get("login.username"));
        pnlForm.add(lblUsuario, gbc);
        gbc.gridx = 1;
        txtUsername = new JTextField(15);
        txtUsername.setToolTipText(I18nUtil.get("login.tooltip.username"));
        pnlForm.add(txtUsername, gbc);

        // Password
        gbc.gridx = 0; gbc.gridy = 1;
        lblPassword = new JLabel(I18nUtil.get("login.password"));
        pnlForm.add(lblPassword, gbc); 
        gbc.gridx = 1;
        txtPassword = new JPasswordField(15);
        txtPassword.setToolTipText(I18nUtil.get("login.tooltip.password"));
        pnlForm.add(txtPassword, gbc);

        // Botones
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        JPanel pnlBotones = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnAcceso = new JButton(I18nUtil.get("login.btn.acceder"));
        btnRegistro = new JButton(I18nUtil.get("login.btn.registro")); 
        btnRecuperar = new JButton(I18nUtil.get("login.btn.recuperar"));

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

        // Selector de idioma
        pnlIdioma = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JComboBox<String> cmbIdioma = new JComboBox<>(new String[]{
            "ES", "EN"
        });
        cmbIdioma.setSelectedIndex(0);  // Español por defecto
        cmbIdioma.addActionListener(e -> {
            String lang = (String) cmbIdioma.getSelectedItem();  // "ES"/"EN"
            I18nUtil.setLocale(lang.toLowerCase());  // Cambia locale interno

            // RECARGA TODA LA UI
            updateAllTexts();  // PrincipalFrame.updateAllTexts()
        });

        pnlIdioma.add(new JLabel("Idioma:"));
        pnlIdioma.add(cmbIdioma);

        gbc.gridy = 3;
        gbc.gridwidth = 2;
        pnlForm.add(pnlIdioma, gbc);
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
            JOptionPane.showMessageDialog(this, I18nUtil.get("login.error.empty"));
            return;
        }

        Usuario usuario = usuarioService.login(username, password);
        if (usuario != null) {
            String mensaje = I18nUtil.get("login.success").replace("{0}", usuario.getNombre());
            JOptionPane.showMessageDialog(this, mensaje);
            new PrincipalFrame(usuario).setVisible(true);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this,I18nUtil.get("login.error.invalid"));
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

        registroFrame.updateAllTexts();
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
        
        recuperarFrame.updateAllTexts();
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
    
    /**
     * Actualiza todos los textos de la interfaz según el idioma actual
     * 
     */
    private void updateAllTexts() {
        lblTitulo.setText(I18nUtil.get("login.titulo"));
        lblUsuario.setText(I18nUtil.get("login.username"));
        lblPassword.setText(I18nUtil.get("login.password"));
        btnAcceso.setText(I18nUtil.get("login.btn.acceder"));
        btnRegistro.setText(I18nUtil.get("login.btn.registro"));
        btnRecuperar.setText(I18nUtil.get("login.btn.recuperar"));

        // Tooltips
        txtUsername.setToolTipText(I18nUtil.get("login.tooltip.username"));
        txtPassword.setToolTipText(I18nUtil.get("login.tooltip.password"));

        // Idioma label (ajusta índice 0):
        // ((JLabel)pnlIdioma.getComponent(0)).setText(I18nUtil.get("idioma.selector"));
    }
}