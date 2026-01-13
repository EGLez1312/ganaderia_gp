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
 * @version 2.0
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
        if (email.isEmpty() || !email.contains("@")) {
            lblDatos.setText("<html><center style='color:red;'>" + I18nUtil.get("recuperar.error.empty") + "</center></html>");
            txtEmail.requestFocus();
            return;
        }

        UsuarioDAO dao = new UsuarioDAO();
        try {
            System.out.println("RECUPERAR: Buscando email='" + email + "'");
            Usuario usuario = dao.findByEmail(email);
            System.out.println("RECUPERAR: Resultado usuario=" + (usuario != null ? usuario.getUsername() : "NULL"));

            if (usuario == null || !usuario.isActivo()) {
                lblDatos.setText("<html><center style='color:orange;'>" + I18nUtil.get("recuperar.notfound.active") + "</center></html>");
                JOptionPane.showMessageDialog(this, I18nUtil.get("recuperar.error.notfound"));
                return;
            }

            // ÉXITO: Temp pass
            String tempPass = "admin123";
            PasswordEncoderUtil encoder = new PasswordEncoderUtil();
            usuario.setPassword(encoder.encode(tempPass));
            dao.actualizar(usuario);

            // Rellena lblDatos con datos reales
            String ultima = usuario.getUltimaConexion() != null ? usuario.getUltimaConexion().toString() : I18nUtil.get("recuperar.datos.nunca");
            lblDatos.setText(String.format("<html><div style='padding:10px;text-align:left;'>"
                    + "<b>%s</b> <code>%s</code><br>"
                    + "%s %s %s<br>"
                    + "%s <font color='blue'>%s</font><br>"
                    + "<i style='color:orange;'>%s</i>"
                    + "</div></html>",
                    I18nUtil.get("recuperar.datos.usuario", usuario.getUsername()),
                    usuario.getEmail(),
                    usuario.getNombre(), usuario.getApellidos(),
                    I18nUtil.get("recuperar.datos.ultima"), ultima,
                    I18nUtil.get("recuperar.datos.temporal"), tempPass,
                    I18nUtil.get("recuperar.datos.cambia")));

            // Alert + rellena login padre
            String successMsg = String.format(I18nUtil.get("recuperar.success"), usuario.getUsername())
                    .replace("{0}", usuario.getUsername()); 
            JOptionPane.showMessageDialog(this, successMsg, "¡Recuperado!", JOptionPane.INFORMATION_MESSAGE);
            if (parent != null) {
                parent.getTxtUsername().setText(usuario.getUsername());
                parent.getTxtPassword().setText(tempPass);
                parent.getTxtPassword().requestFocus();
            }

        } catch (Exception ex) {
            String msgError = ex.getMessage() != null ? ex.getMessage() : ex.getClass().getSimpleName() + " (ver console)";
            System.err.println("RECUPERAR ERROR: " + msgError);
            ex.printStackTrace();
            lblDatos.setText(String.format("<html><center style='color:red;'>%s<br><small>%s</small></center></html>",
                    I18nUtil.get("recuperar.error.bd"), msgError));
            JOptionPane.showMessageDialog(this, String.format(I18nUtil.get("recuperar.error.database"), msgError));
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
