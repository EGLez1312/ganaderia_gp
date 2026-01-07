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
import java.awt.event.ActionListener;

/**
 * Ventana para recuperar contraseña vía email.
 * Busca usuario y muestra datos para reset.
 * 
 * @author Elena González
 * @version 1.0
 */
public class RecuperarPasswordFrame extends JFrame {
    private LoginFrame parent;
    private JTextField txtEmail;
    private UsuarioDAO usuarioDAO;
    private JLabel lblDatos;

    public RecuperarPasswordFrame(LoginFrame parent) {
        this.parent = parent;
        initComponents();
        setLocationRelativeTo(null);
    }

    private void initComponents() {
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setTitle("Recuperar Contraseña - Ganadería GP");
        setSize(400, 300);
        setLayout(new GridBagLayout());
        
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(12, 12, 12, 12);
        c.anchor = GridBagConstraints.WEST;
        
        // Instrucciones
        c.gridx = 0; c.gridy = 0; c.gridwidth = 2;
        c.fill = GridBagConstraints.HORIZONTAL;
        JLabel lblIntro = new JLabel("Introduce tu email:");
        lblIntro.setFont(new Font("Arial", Font.BOLD, 14));
        add(lblIntro, c);
        
        // Email
        c.gridwidth = 1;
        c.gridy = 1;
        c.gridx = 0;
        add(new JLabel("Email:"), c);
        c.gridx = 1;
        txtEmail = new JTextField(20);
        txtEmail.setToolTipText("Email registrado en el sistema");
        add(txtEmail, c);
        
        // Resultado
        c.gridx = 0; c.gridy = 2; c.gridwidth = 2;
        lblDatos = new JLabel(" ", JLabel.CENTER);
        lblDatos.setBorder(BorderFactory.createTitledBorder("Datos recuperados"));
        lblDatos.setPreferredSize(new Dimension(350, 80));
        add(lblDatos, c);
        
        // Botones
        c.gridy = 3; c.fill = GridBagConstraints.HORIZONTAL;
        JPanel pnlBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        JButton btnRecuperar = new JButton("RECUPERAR");
        btnRecuperar.setMnemonic('R');
        getRootPane().setDefaultButton(btnRecuperar);
        btnRecuperar.addActionListener(e -> recuperarPassword());
        
        JButton btnVolver = new JButton("Volver al Login");
        btnVolver.setMnemonic('V');
        btnVolver.addActionListener(e -> dispose());
        
        pnlBotones.add(btnVolver);
        pnlBotones.add(btnRecuperar);
        add(pnlBotones, c);
    }

    private void recuperarPassword() {
        String email = txtEmail.getText().trim();
        if (email.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Introduce tu email", "Error", JOptionPane.ERROR_MESSAGE);
            txtEmail.requestFocus();
            return;
        }

        try {
            usuarioDAO = new UsuarioDAO();
            Usuario usuario = usuarioDAO.findByEmail(email);

            if (usuario != null) {
                String passTemporal = new PasswordEncoderUtil().encode("admin123");
                usuario.setPassword(passTemporal);

                usuarioDAO.update(usuario);
                
                String datos = String.format(
                    "<html><b>Usuario:</b> %s<br>" +
                    "<b>Nombre:</b> %s %s<br>" +
                    "<b>Última conexión:</b> %s<br>" +
                    "<i>Nueva contraseña temporal: admin123</i></html>",
                    usuario.getUsername(),
                    usuario.getNombre(),
                    usuario.getApellidos(),
                    usuario.getUltimaConexion() != null ? 
                        usuario.getUltimaConexion().toString() : "Nunca"
                );
                lblDatos.setText(datos);
                
                JOptionPane.showMessageDialog(this, 
                    "¡Datos recuperados! Usa contraseña temporal.", 
                    "Éxito", JOptionPane.INFORMATION_MESSAGE);
                
                // Rellenar login
                parent.getTxtUsername().setText(usuario.getUsername());
                parent.getTxtPassword().requestFocus();
                
            } else {
                lblDatos.setText("<html><center>No se encontró usuario con ese email</center></html>");
                JOptionPane.showMessageDialog(this, 
                    "Email no registrado", "No encontrado", JOptionPane.WARNING_MESSAGE);
            }
            
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        new RecuperarPasswordFrame(null).setVisible(true);
    }
}
