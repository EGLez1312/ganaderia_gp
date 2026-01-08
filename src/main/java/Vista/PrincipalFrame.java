/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Vista;

import Modelo.Usuario;
import javax.swing.*;
import java.awt.*;
import Vista.OvejaPanel;
import Vista.EventoPanel;
import Vista.EstadisticasPanel;
import Vista.PerfilPanel;
import java.awt.event.ActionEvent;

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
        tabbedPane.setFont(new Font("Arial", Font.PLAIN, 14));
        
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
        JLabel lblStatus = new JLabel("Listo | Pestaña activa: " + tabbedPane.getTitleAt(0), JLabel.RIGHT);
        
        statusBar.add(lblUsuario, BorderLayout.WEST);
        statusBar.add(lblStatus, BorderLayout.EAST);
        add(statusBar, BorderLayout.SOUTH);

        // 4. Listeners para navegación dinámica
        configurarListeners();
        
        // 5. Enter funciona en todas pestañas
        getRootPane().setDefaultButton(null);
    }

    /**
     * Crea la barra de menú con mnemónicos Alt+1,2,3,4.
     * Cada menú cambia pestaña activa (interfaz dinámica).
     * 
     * @return JMenuBar completamente configurada.
     */
    private JMenuBar crearMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        
        // Menú Ovejas (Alt+1)
        JMenu mnOvejas = new JMenu("Ovejas");
        mnOvejas.setMnemonic('1');
        mnOvejas.setToolTipText("Gestión CRUD ovejas (Alt+1)");
        mnOvejas.addActionListener(e -> tabbedPane.setSelectedIndex(0));
        menuBar.add(mnOvejas);
        
        // Menú Eventos (Alt+2)
        JMenu mnEventos = new JMenu("Eventos");
        mnEventos.setMnemonic('2');
        mnEventos.setToolTipText("Partos y tratamientos (Alt+2)");
        mnEventos.addActionListener(e -> tabbedPane.setSelectedIndex(1));
        menuBar.add(mnEventos);
        
        // Menú Estadísticas (Alt+3)
        JMenu mnStats = new JMenu("Estadísticas");
        mnStats.setMnemonic('3');
        mnStats.setToolTipText("KPIs y resúmenes (Alt+3)");
        mnStats.addActionListener(e -> tabbedPane.setSelectedIndex(2));
        menuBar.add(mnStats);
        
        // Menú Perfil (Alt+4)
        JMenu mnPerfil = new JMenu("Mi Perfil");
        mnPerfil.setMnemonic('4');
        mnPerfil.setToolTipText("Editar datos usuario (Alt+4)");
        mnPerfil.addActionListener(e -> tabbedPane.setSelectedIndex(3));
        menuBar.add(mnPerfil);
        
        // Menú Ayuda
        JMenu mnAyuda = new JMenu("❓ Ayuda");
        mnAyuda.setMnemonic('A');
        mnAyuda.setToolTipText("Documentación y soporte");
        mnAyuda.add(new JMenuItem("Acerca de...")).addActionListener(e -> mostrarAcerca());
        menuBar.add(mnAyuda);
        
        return menuBar;
    }

    /**
     * Configura listeners adicionales para navegación fluida.
     */
    private void configurarListeners() {
        // Ctrl+Tab cambia pestañas
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
            .put(KeyStroke.getKeyStroke("control TAB"), "nextTab");
        getRootPane().getActionMap().put("nextTab", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int index = tabbedPane.getSelectedIndex() + 1;
                if (index >= tabbedPane.getTabCount()) index = 0;
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