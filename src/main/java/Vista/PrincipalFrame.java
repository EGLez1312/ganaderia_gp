/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Vista;

import Modelo.Usuario;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

/**
 * Ventana principal post-login con 4 paneles dinámicos obligatorios.
 * Cumple todos los requisitos: ToolTips, mnemónicos, Enter, dinámica, Javadoc.
 * 
 * @author Elena González
 * @version 2.0
 */
public class PrincipalFrame extends JFrame {
    
    private final Usuario usuarioLogueado;
    private JTabbedPane tabbedPane;
    private JLabel lblStatus;
    
    // Paneles de las 4 pestañas obligatorias
    private OvejaPanel ovejasPanel;
    private EventoPanel eventosPanel;
    private EstadisticasPanel statsPanel;
    private PerfilPanel perfilPanel;

    /**
     * Constructor principal. Inicializa interfaz con usuario logueado.
     * 
     * @param usuario usuario autenticado desde LoginFrame.
     */
    public PrincipalFrame(Usuario usuario) {
        this.usuarioLogueado = usuario;
        initComponents();
        setLocationRelativeTo(null);
    }

    /**
     * Inicializa todos los componentes de la ventana principal.
     * Crea menú, pestañas dinámicas y barra de estado.
     */
    private void initComponents() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("Ganadería GP v2.0 - Bienvenido " + usuarioLogueado.getUsername());
        setMinimumSize(new Dimension(1000, 700));
        setLayout(new BorderLayout(5, 5));

        // 1. Barra de menú superior (acceso teclado Alt+1,2,3,4)
        JMenuBar menuBar = crearMenuBar();
        setJMenuBar(menuBar);

        // 2. Panel central: 4 pestañas dinámicas obligatorias
        tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        tabbedPane.setToolTipText("Ctrl+Tab para navegar | Alt+1,2,3,4 desde menú");
        tabbedPane.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        // Pestaña 1: Ovejas (CRUD completo)
        ovejasPanel = new OvejaPanel(usuarioLogueado);
        tabbedPane.addTab("Ovejas", ovejasPanel);
        
        // Pestaña 2: Eventos (partos, tratamientos)
        eventosPanel = new EventoPanel(usuarioLogueado);
        tabbedPane.addTab("Eventos", eventosPanel);
        
        // Pestaña 3: Estadísticas (KPIs y gráficos)
        statsPanel = new EstadisticasPanel();
        tabbedPane.addTab("Estadísticas", statsPanel);
        
        // Pestaña 4: Perfil usuario
        perfilPanel = new PerfilPanel(usuarioLogueado);
        tabbedPane.addTab("Perfil Usuario", perfilPanel);
        
        add(tabbedPane, BorderLayout.CENTER);

        // 3. Barra de estado inferior
        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setBorder(BorderFactory.createLoweredBevelBorder());
        statusBar.setPreferredSize(new Dimension(0, 25));
        
        JLabel lblUsuario = new JLabel("Usuario: " + usuarioLogueado.getUsername(), JLabel.LEFT);
        lblStatus = new JLabel("Listo | Pestaña activa: " + tabbedPane.getTitleAt(0), JLabel.RIGHT);
        
        statusBar.add(lblUsuario, BorderLayout.WEST);
        statusBar.add(lblStatus, BorderLayout.EAST);
        add(statusBar, BorderLayout.SOUTH);

        // 4. Listeners para navegación dinámica
        configurarListeners();
        
        // 5. Enter funciona en todas pestañas
        getRootPane().setDefaultButton(null);
        
        //Referencias paneles
        ovejasPanel = (OvejaPanel) tabbedPane.getComponentAt(0);
        eventosPanel = (EventoPanel) tabbedPane.getComponentAt(1);
        statsPanel = (EstadisticasPanel) tabbedPane.getComponentAt(2);
        perfilPanel = (PerfilPanel) tabbedPane.getComponentAt(3);

    }

    /**
     * MenúBar clásica compacta ocupando solo espacio necesario. Izquierda
     * navegación + derecha sistema.
     */
    private JMenuBar crearMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        menuBar.add(crearMenuConMnemonic("Ovejas", 'O', e -> tabbedPane.setSelectedIndex(0)));
        menuBar.add(crearMenuConMnemonic("Eventos", 'E', e -> tabbedPane.setSelectedIndex(1)));
        menuBar.add(crearMenuConMnemonic("Estadísticas", 'S', e -> tabbedPane.setSelectedIndex(2)));
        menuBar.add(crearMenuConMnemonic("Perfil", 'P', e -> tabbedPane.setSelectedIndex(3)));

        JMenu mnSistema = new JMenu("Sistema");
        mnSistema.setMnemonic('I');

        JMenuItem itemCerrar = new JMenuItem("Cerrar sesión");
        itemCerrar.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4, ActionEvent.ALT_MASK));
        itemCerrar.addActionListener(e -> cerrarSesion());

        JMenuItem itemAcerca = new JMenuItem("Acerca de...");
        itemAcerca.setMnemonic('A');
        itemAcerca.addActionListener(e -> mostrarAcerca());

        mnSistema.add(itemCerrar);
        mnSistema.addSeparator(); 
        mnSistema.add(itemAcerca);

        menuBar.add(mnSistema);

        return menuBar;
    }

    /**
     * Crea un JMenu que se comporta como un acceso directo (Estilo Windows)
     */
    private JMenu crearMenuConMnemonic(String texto, char mnemonic, ActionListener accion) {
        JMenu menu = new JMenu(texto);
        menu.setMnemonic(mnemonic); // Esto subraya la letra (Alt + Letra)

        // Al hacer clic en el nombre del menú, se cambia de pestaña
        menu.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                accion.actionPerformed(null);
            }
        });
        return menu;
    }

     /**
     * Configura listeners adicionales para navegación fluida y actualización de
     * estado.
     */
    private void configurarListeners() {
        // Listener para detectar el cambio de pestaña (ratón o teclado)
        tabbedPane.addChangeListener(e -> {
            int index = tabbedPane.getSelectedIndex();
            String titulo = tabbedPane.getTitleAt(index);

            // Actualiza la barra de estado inferior dinámicamente
            if (lblStatus != null) {
                lblStatus.setText("Listo | Pestaña activa: " + titulo);
            }

            // Si entramos en la pestaña de Eventos (índice 1), refrescamos el combo
            if (index == 1 && eventosPanel != null) {
                // Asegúrate de que el método en EventoPanel sea público
                eventosPanel.cargarOvejasCombo();
            }
        });

        // Ctrl+Tab cambia pestañas
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke("control TAB"), "nextTab");
        getRootPane().getActionMap().put("nextTab", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int index = tabbedPane.getSelectedIndex() + 1;
                if (index >= tabbedPane.getTabCount()) {
                    index = 0;
                }
                tabbedPane.setSelectedIndex(index);
            }
        });
    }

    /**
     * Muestra diálogo "Acerca de" con créditos.
     */
    private void mostrarAcerca() {
        JOptionPane.showMessageDialog(this,
            "Ganadería GP v2.0\n\n" +
            "Desarrollo Interfaces - Elena González\n" +
            "Hibernate + Swing + Maven\n" +
            "2026 © Todos los derechos reservados",
            "Acerca de Ganadería GP", JOptionPane.INFORMATION_MESSAGE);
    }
    
    /**
     * Crea JMenuItem reutilizable con mnemónico/accelerador.
     *
     * @param texto etiqueta visible
     * @param accion ActionListener
     * @param keyCode tecla acelerador (VK_F4, etc.)
     * @return JMenuItem listo
     */
    private JMenuItem createMenuItem(String texto, ActionListener accion, int keyCode) {
        JMenuItem item = new JMenuItem(texto);
        item.setAccelerator(KeyStroke.getKeyStroke(keyCode, InputEvent.ALT_DOWN_MASK));
        item.addActionListener(accion);
        return item;
    }

    /**
     * Cierra sesión: PrincipalFrame → LoginFrame. Limpia datos usuario +
     * dispose ventana.
     */
    private void cerrarSesion() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "¿Cerrar sesión y volver al login?",
                "Confirmar", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            dispose();  // Cierra PrincipalFrame
            new LoginFrame().setVisible(true);  // Reabre Login
        }
    }

    /**
     * Método main para pruebas independientes.
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Usuario usuario = new Usuario();
            usuario.setUsername("admin");
            usuario.setNombre("Elena");
            new PrincipalFrame(usuario).setVisible(true);
        });
    }
}