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
import Util.I18nUtil;

/**
 * Ventana modal para recuperación de contraseña por email. Busca usuario
 * activo, genera contraseña temporal (demo), actualiza BD y muestra datos de
 * cuenta. Cumple requisitos: botón 2 login, ToolTips, mnemonics (Alt+R/V),
 * Enter=recuperar, errores gestionados.
 *
 * @author Elena González
 * @version 1.0
 */
public class RecuperarPasswordFrame extends JFrame {

    /**
     * Referencia LoginFrame padre para comunicación post-recuperación.
     */
    private final LoginFrame parent;
    
    /**
     * Campo entrada email y panel resultados dinámicos.
     */
    private JTextField txtEmail;
    private JLabel lblDatos;

    /**
     * Servicios inyectados para negocio/persistencia.
     */
    private UsuarioService usuarioService;

    /**
     * Constructor inicializa UI y centra ventana respecto padre.
     *
     * @param parent LoginFrame que abre recuperación.
     */
    public RecuperarPasswordFrame(LoginFrame parent) {
        this.parent = parent;
        initComponents();
        setLocationRelativeTo(parent);
    }

    /**
     * Crea interfaz completa: instrucciones, email, resultados dinámicos,
     * botones con mnemonics y Enter=RECUPERAR. Usa GridBagLayout responsive con
     * ToolTips informativos y bordes visuales.
     */
    private void initComponents() {
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setTitle(I18nUtil.get("recuperar.title"));
        setSize(420, 320);
        setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(12, 12, 12, 12);
        c.anchor = GridBagConstraints.WEST;

        // Título introductorio
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 2;
        c.fill = GridBagConstraints.HORIZONTAL;
        JLabel lblIntro = new JLabel(I18nUtil.get("recuperar.intro"));
        lblIntro.setFont(new Font("Segoe UI", Font.BOLD, 14));
        add(lblIntro, c);

        // Campo email
        c.gridwidth = 1;
        c.gridy = 1;
        c.gridx = 0;
        add(new JLabel(I18nUtil.get("recuperar.email")), c);
        c.gridx = 1;
        txtEmail = new JTextField(20);
        txtEmail.setToolTipText(I18nUtil.get("recuperar.tooltip.email"));
        add(txtEmail, c);

        // Panel resultados dinámico
        c.gridx = 0;
        c.gridy = 2;
        c.gridwidth = 2;
        lblDatos = new JLabel(" ", JLabel.CENTER);
        lblDatos.setBorder(BorderFactory.createTitledBorder(I18nUtil.get("recuperar.panel.title")));
        lblDatos.setPreferredSize(new Dimension(370, 85));
        lblDatos.setOpaque(true);
        lblDatos.setBackground(Color.WHITE);
        add(lblDatos, c);

        // Botones alineados derecha
        c.gridy = 3;
        c.fill = GridBagConstraints.HORIZONTAL;
        JPanel pnlBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton btnVolver = new JButton(I18nUtil.get("recuperar.btn.volver"));
        btnVolver.setMnemonic('V');  // Alt+V
        btnVolver.addActionListener(e -> dispose());

        JButton btnRecuperar = new JButton(I18nUtil.get("recuperar.btn.recuperar"));
        btnRecuperar.setMnemonic('R');  // Alt+R
        getRootPane().setDefaultButton(btnRecuperar);  // Enter = Recuperar
        btnRecuperar.addActionListener(e -> recuperarPassword());

        pnlBotones.add(btnVolver);
        pnlBotones.add(btnRecuperar);
        add(pnlBotones, c);
    }

    /**
     * Valida email → busca usuario → genera pass temporal (demo) → actualiza BD
     * → muestra datos → rellena LoginFrame. Gestiona todos errores: vacío, no
     * encontrado, BD.
     */
    private void recuperarPassword() {
        String email = txtEmail.getText().trim();
        if (email.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    I18nUtil.get("recuperar.error.empty"),
                    I18nUtil.get("recuperar.error.empty"), JOptionPane.ERROR_MESSAGE);
            txtEmail.requestFocus();
            return;
        }

        try {
            // Inyección Service (MVC correcto)
            UsuarioDAO dao = new UsuarioDAO();
            PasswordEncoderUtil encoder = new PasswordEncoderUtil();
            usuarioService = new UsuarioService(dao, encoder);

            Usuario usuario = dao.findByEmail(email);
            if (usuario != null && usuario.isActivo()) {
                // PASS TEMPORAL (NO encriptar para mostrarla)
                String passTemporal = "admin123";
                String passHash = encoder.encode(passTemporal);
                usuario.setPassword(passHash);
                dao.actualizar(usuario); 

                // Mostrar datos recuperados (HTML para formato)
                String datosUsuario = String.format(I18nUtil.get("recuperar.datos.usuario"), usuario.getUsername());
                String datos = String.format(
                        "<html><div style='text-align:left;padding:10px;'>"
                        + "<b>%s</b> <code>%s</code><br>"
                        + "<b>%s</b> %s %s<br>"
                        + "<b>%s</b> %s<br>"
                        + "<b>%s</b> <font color='blue'><b>admin123</b></font><br>"
                        + "<i style='color:orange;'>%s</i>"
                        + "</div></html>",
                        datosUsuario,
                        usuario.getNombre() != null ? usuario.getNombre() : "",
                        usuario.getApellidos() != null ? usuario.getApellidos() : "",
                        I18nUtil.get("recuperar.datos.ultima"),
                        usuario.getUltimaConexion() != null
                        ? usuario.getUltimaConexion().toString()
                        : I18nUtil.get("recuperar.datos.nunca")
                );
                lblDatos.setText(datos);

                JOptionPane.showMessageDialog(this,
                        I18nUtil.get("recuperar.success", usuario.getUsername()),
                        I18nUtil.get("recuperar.success"), JOptionPane.INFORMATION_MESSAGE);

                // Rellenar login padre
                parent.getTxtUsername().setText(usuario.getUsername());
                parent.getTxtPassword().requestFocus();

            } else {
                lblDatos.setText("<html><center>" + I18nUtil.get("recuperar.notfound.active") + "</center></html>");
                JOptionPane.showMessageDialog(this, I18nUtil.get("recuperar.error.notfound"),
                        I18nUtil.get("recuperar.error.notfound"), JOptionPane.WARNING_MESSAGE);
                txtEmail.selectAll();
                txtEmail.requestFocus();
            }

        } catch (Exception ex) {
            lblDatos.setText("<html><center>" + I18nUtil.get("recuperar.error.bd") + "</center></html>");
            JOptionPane.showMessageDialog(this,
                    I18nUtil.get("recuperar.error.database", ex.getMessage()),
                    I18nUtil.get("recuperar.error.database"), JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * @return LoginFrame padre para comunicación.
     */
    public LoginFrame getParent() {
        return parent;
    }
    
    /**
     * Actualiza TODOS los textos i18n del frame según locale actual.
     */
    protected void updateAllTexts() {
        setTitle(I18nUtil.get("recuperar.title"));

        // Components
        ((JLabel) getContentPane().getComponent(0)).setText(I18nUtil.get("recuperar.intro"));
        ((JLabel) getContentPane().getComponent(1)).setText(I18nUtil.get("recuperar.email"));
        lblDatos.setBorder(BorderFactory.createTitledBorder(I18nUtil.get("recuperar.panel.title")));

        JPanel pnlBotones = (JPanel) getContentPane().getComponent(4);
        ((JButton) pnlBotones.getComponent(0)).setText(I18nUtil.get("recuperar.btn.volver"));
        ((JButton) pnlBotones.getComponent(1)).setText(I18nUtil.get("recuperar.btn.recuperar"));

        txtEmail.setToolTipText(I18nUtil.get("recuperar.tooltip.email"));

        revalidate();
        repaint();
    }



    /**
     * main para pruebas independientes (thread-safe).
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new RecuperarPasswordFrame(null).setVisible(true);
        });
    }
}
