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
import Util.I18nUtil;

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
        setTitle(I18nUtil.get("registro.title"));
        setSize(450, 450);
        setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(10, 10, 10, 10);
        c.anchor = GridBagConstraints.WEST;

        int fila = 0;

        // Username requerido
        c.gridx = 0;
        c.gridy = fila++;
        add(new JLabel(I18nUtil.get("registro.username")), c);
        c.gridx = 1;
        txtUsername = new JTextField(20);
        txtUsername.setToolTipText(I18nUtil.get("registro.tooltip.username"));
        add(txtUsername, c);

        // Email requerido
        c.gridx = 0;
        c.gridy = fila++;
        add(new JLabel(I18nUtil.get("registro.email")), c);
        c.gridx = 1;
        txtEmail = new JTextField(20);
        txtEmail.setToolTipText(I18nUtil.get("registro.tooltip.email"));
        add(txtEmail, c);

        // Nombre opcional
        c.gridx = 0;
        c.gridy = fila++;
        add(new JLabel(I18nUtil.get("registro.nombre")), c);
        c.gridx = 1;
        txtNombre = new JTextField(20);
        txtNombre.setToolTipText(I18nUtil.get("registro.tooltip.nombre"));
        add(txtNombre, c);

        // Apellidos opcionales
        c.gridx = 0;
        c.gridy = fila++;
        add(new JLabel(I18nUtil.get("registro.apellidos")), c);
        c.gridx = 1;
        txtApellidos = new JTextField(20);
        txtApellidos.setToolTipText(I18nUtil.get("registro.tooltip.apellidos"));
        add(txtApellidos, c);

        // Contraseña requerida
        c.gridx = 0;
        c.gridy = fila++;
        add(new JLabel(I18nUtil.get("registro.password")), c);
        c.gridx = 1;
        txtPassword = new JPasswordField(20);
        txtPassword.setToolTipText(I18nUtil.get("registro.tooltip.password"));
        add(txtPassword, c);

        // Repetición requerida
        c.gridx = 0;
        c.gridy = fila++;
        add(new JLabel(I18nUtil.get("registro.repeat")), c);
        c.gridx = 1;
        txtRepeatPassword = new JPasswordField(20);
        txtRepeatPassword.setToolTipText(I18nUtil.get("registro.tooltip.repeat"));
        add(txtRepeatPassword, c);

        // Panel botones con mnemonics
        c.gridx = 0;
        c.gridy = fila;
        c.gridwidth = 2;
        c.fill = GridBagConstraints.HORIZONTAL;
        JPanel pnlBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton btnRegistrar = new JButton(I18nUtil.get("registro.btn.registrar"));
        btnRegistrar.setMnemonic('R');  // Alt+R
        getRootPane().setDefaultButton(btnRegistrar);  // Enter = ejecutar
        btnRegistrar.addActionListener(e -> guardarUsuario());

        JButton btnCancelar = new JButton(I18nUtil.get("registro.btn.cancelar"));
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
            JOptionPane.showMessageDialog(this,
                    I18nUtil.get("registro.error.required"),
                    I18nUtil.get("registro.error.required"), JOptionPane.ERROR_MESSAGE);
            txtUsername.requestFocus();
            return;
        }
        if (!password.equals(repeatPassword)) {
            JOptionPane.showMessageDialog(this,
                    I18nUtil.get("registro.error.repeat"),
                    I18nUtil.get("registro.error.repeat"), JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (password.length() < 6) {
            JOptionPane.showMessageDialog(this,
                    I18nUtil.get("registro.error.minlength"),
                    I18nUtil.get("registro.error.minlength"), JOptionPane.ERROR_MESSAGE);
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
                JOptionPane.showMessageDialog(this,
                        I18nUtil.get("registro.success"),
                        I18nUtil.get("registro.success"), JOptionPane.INFORMATION_MESSAGE);
                // Comunicación padre-hijo: rellenar login
                parent.getTxtUsername().setText(username);
                parent.getTxtPassword().requestFocus();
                dispose();  // Cerrar modal
            } else {
                   JOptionPane.showMessageDialog(this,
                        I18nUtil.get("registro.error.duplicate"),
                        I18nUtil.get("registro.error.duplicate"), JOptionPane.ERROR_MESSAGE);
                txtUsername.selectAll();
                txtUsername.requestFocus();
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    I18nUtil.get("registro.error.database", ex.getMessage()),
                    I18nUtil.get("registro.error.database.title"), JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * @return LoginFrame padre que originó esta ventana.
     */
    public LoginFrame getParent() {
        return parent;
    }
    
    /**
     * Actualiza TODOS los textos i18n del frame según locale actual.
     */
    protected void updateAllTexts() {
        setTitle(I18nUtil.get("registro.title"));

        // Labels
        ((JLabel) getContentPane().getComponent(0)).setText(I18nUtil.get("registro.username"));  
        ((JLabel) getContentPane().getComponent(2)).setText(I18nUtil.get("registro.email"));     
        ((JLabel) getContentPane().getComponent(4)).setText(I18nUtil.get("registro.nombre"));    
        ((JLabel) getContentPane().getComponent(6)).setText(I18nUtil.get("registro.apellidos")); 
        ((JLabel) getContentPane().getComponent(8)).setText(I18nUtil.get("registro.password"));  
        ((JLabel) getContentPane().getComponent(10)).setText(I18nUtil.get("registro.repeat"));   

        // Botones 
        JPanel pnlBotones = (JPanel) getContentPane().getComponent(12); 
        ((JButton) pnlBotones.getComponent(0)).setText(I18nUtil.get("registro.btn.registrar"));
        ((JButton) pnlBotones.getComponent(1)).setText(I18nUtil.get("registro.btn.cancelar"));

        // Tooltips 
        txtUsername.setToolTipText(I18nUtil.get("registro.tooltip.username"));
        txtEmail.setToolTipText(I18nUtil.get("registro.tooltip.email"));
        txtNombre.setToolTipText(I18nUtil.get("registro.tooltip.nombre"));
        txtApellidos.setToolTipText(I18nUtil.get("registro.tooltip.apellidos"));
        txtPassword.setToolTipText(I18nUtil.get("registro.tooltip.password"));
        txtRepeatPassword.setToolTipText(I18nUtil.get("registro.tooltip.repeat"));

        revalidate();
        repaint();
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
