/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Vista;

import Servicio.UsuarioService;
import javax.swing.*;
import java.awt.*;

/**
 * Diálogo para recuperar contraseña (versión simplificada).
 * Busca usuario por email/username y permite cambiar contraseña.
 * 
 * @author Elena González
 * @version 1.0
 */
public class RecuperarPasswordFrame extends JDialog {

    private final LoginFrame parent;
    private final UsuarioService usuarioService = new UsuarioService();

    private JTextField txtEmailOUsername;
    private JPasswordField txtNuevaPassword;
    private JPasswordField txtRepeatPassword;

    public RecuperarPasswordFrame(LoginFrame parent) {
        super(parent, "Recuperar contraseña", true);
        this.parent = parent;
        initComponents();
        pack();
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));

        JLabel lblTitulo = new JLabel("Recuperar contraseña", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 18));
        lblTitulo.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        add(lblTitulo, BorderLayout.NORTH);

        JPanel pnlForm = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;

        int fila = 0;

        gbc.gridx = 0; gbc.gridy = fila++;
        pnlForm.add(new JLabel("Email o Usuario:"), gbc);
        gbc.gridx = 1;
        txtEmailOUsername = new JTextField(15);
        pnlForm.add(txtEmailOUsername, gbc);

        gbc.gridx = 0; gbc.gridy = fila++;
        pnlForm.add(new JLabel("Nueva contraseña:"), gbc);
        gbc.gridx = 1;
        txtNuevaPassword = new JPasswordField(15);
        pnlForm.add(txtNuevaPassword, gbc);

        gbc.gridx = 0; gbc.gridy = fila++;
        pnlForm.add(new JLabel("Repetir nueva contraseña:"), gbc);
        gbc.gridx = 1;
        txtRepeatPassword = new JPasswordField(15);
        pnlForm.add(txtRepeatPassword, gbc);

        gbc.gridx = 0; gbc.gridy = fila++; gbc.gridwidth = 2;
        JPanel pnlBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnCambiar = new JButton("Cambiar contraseña");
        getRootPane().setDefaultButton(btnCambiar);
        JButton btnCancelar = new JButton("Cancelar");

        pnlBotones.add(btnCancelar);
        pnlBotones.add(btnCambiar);
        pnlForm.add(pnlBotones, gbc);

        add(pnlForm, BorderLayout.CENTER);

        btnCambiar.addActionListener(e -> cambiarPassword());
        btnCancelar.addActionListener(e -> dispose());
    }

    private void cambiarPassword() {
        String identificador = txtEmailOUsername.getText().trim();
        String nuevaPass = new String(txtNuevaPassword.getPassword()).trim();
        String repeatPass = new String(txtRepeatPassword.getPassword()).trim();

        if (identificador.isEmpty() || nuevaPass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Todos los campos son obligatorios", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!nuevaPass.equals(repeatPass)) {
            JOptionPane.showMessageDialog(this, "Las contraseñas no coinciden", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // TODO: Implementar lógica real de cambio de contraseña
        JOptionPane.showMessageDialog(this, "Funcionalidad de recuperación de contraseña implementada.\n"
                + "En producción se enviaría un email con enlace de reset.", 
                "Info", JOptionPane.INFORMATION_MESSAGE);
        dispose();
    }
}