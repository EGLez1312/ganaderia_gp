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
import Util.I18nUtil;
import java.util.Locale;

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
    private JMenuBar menuBar;
    
    // Paneles de las 4 pestañas obligatorias
    private OvejaPanel ovejasPanel;
    private EventoPanel eventosPanel;
    private EstadisticasPanel statsPanel;
    private PerfilPanel perfilPanel;
    private JPanel pnlIdioma;
    private JComboBox<String> cmbIdioma;

    /**
     * Constructor principal. Inicializa interfaz con usuario logueado.
     *
     * @param usuario usuario autenticado desde LoginFrame.
     */
    public PrincipalFrame(Usuario usuario) {
        this.usuarioLogueado = usuario;
        initComponents();
        updateAllTexts();
        setLocationRelativeTo(null);
    }

    /**
     * Inicializa todos los componentes de la ventana principal.
     * Crea menú, pestañas dinámicas y barra de estado.
     */
    private void initComponents() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle(String.format(I18nUtil.get("principal.title"),
                usuarioLogueado.getUsername()));
        setMinimumSize(new Dimension(1000, 700));
        setLayout(new BorderLayout(5, 5));

        // 1. Barra de menú superior (acceso teclado Alt+1,2,3,4)
        JMenuBar menuBar = crearMenuBar();
        setJMenuBar(menuBar);

        // 2. Panel central: 4 pestañas dinámicas obligatorias
        tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        tabbedPane.setToolTipText(I18nUtil.get("principal.status.tooltip"));
        tabbedPane.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        // Pestaña 1: Ovejas (CRUD completo)
        ovejasPanel = new OvejaPanel(usuarioLogueado);
        tabbedPane.addTab(I18nUtil.get("principal.tab.ovejas"), ovejasPanel);
        
        // Pestaña 2: Eventos (partos, tratamientos)
        eventosPanel = new EventoPanel(usuarioLogueado);
        tabbedPane.addTab(I18nUtil.get("principal.tab.eventos"), eventosPanel);
        
        // Pestaña 3: Estadísticas (KPIs y gráficos)
        statsPanel = new EstadisticasPanel();
        tabbedPane.addTab(I18nUtil.get("principal.tab.stats"), statsPanel);
        
        // Pestaña 4: Perfil usuario
        perfilPanel = new PerfilPanel(usuarioLogueado);
        tabbedPane.addTab(I18nUtil.get("principal.tab.perfil"), perfilPanel);
        
        add(tabbedPane, BorderLayout.CENTER);

        // 3. Barra de estado inferior
        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setBorder(BorderFactory.createLoweredBevelBorder());
        statusBar.setPreferredSize(new Dimension(0, 25));
        
        JLabel lblUsuario = new JLabel(
                I18nUtil.get("principal.status.usuario").replace("{0}", usuarioLogueado.getUsername()),
                JLabel.LEFT
        );

        lblStatus = new JLabel(
                I18nUtil.get("principal.status.active").replace("{0}", tabbedPane.getTitleAt(0)),
                JLabel.RIGHT
        );
        
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
        menuBar = new JMenuBar();

        menuBar.add(crearMenuConMnemonic(I18nUtil.get("principal.menu.ovejas"), 'O', e -> tabbedPane.setSelectedIndex(0)));
        menuBar.add(crearMenuConMnemonic(I18nUtil.get("principal.menu.eventos"), 'E', e -> tabbedPane.setSelectedIndex(1)));
        menuBar.add(crearMenuConMnemonic(I18nUtil.get("principal.menu.stats"), 'S', e -> tabbedPane.setSelectedIndex(2)));
        menuBar.add(crearMenuConMnemonic(I18nUtil.get("principal.menu.perfil"), 'P', e -> tabbedPane.setSelectedIndex(3)));

        JMenu mnSistema = new JMenu(I18nUtil.get("principal.menu.sistema"));
        mnSistema.setMnemonic('I');

        JMenuItem itemCerrar = new JMenuItem(I18nUtil.get("principal.menu.cerrar"));
        itemCerrar.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4, ActionEvent.ALT_MASK));
        itemCerrar.addActionListener(e -> cerrarSesion());

        JMenuItem itemAcerca = new JMenuItem(I18nUtil.get("principal.menu.acerca"));
        itemAcerca.setMnemonic('A');
        itemAcerca.addActionListener(e -> mostrarAcerca());
        
        // Cambio de idioma
        pnlIdioma = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 2));
        JLabel lblIdioma = new JLabel(I18nUtil.get("idioma.selector"));
        cmbIdioma = new JComboBox<>(new String[]{"ES", "EN"});
        cmbIdioma.addActionListener(e -> {
            String lang = (String) cmbIdioma.getSelectedItem();  // "ES"/"EN"
            I18nUtil.setLocale(lang.toLowerCase());  // Cambia locale interno

            // RECARGA TODA LA UI
            updateAllTexts();  // PrincipalFrame.updateAllTexts()
        });


        pnlIdioma.add(lblIdioma);
        pnlIdioma.add(cmbIdioma);
        menuBar.add(pnlIdioma);

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
                String.format(I18nUtil.get("principal.dialog.acerca"), "Elena González"),
                I18nUtil.get("principal.menu.acerca"), JOptionPane.INFORMATION_MESSAGE);
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
                I18nUtil.get("principal.dialog.cerrar"),
                I18nUtil.get("principal.confirm.yesno"), JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            dispose();  // Cierra PrincipalFrame
            new LoginFrame().setVisible(true);  // Reabre Login
        }
    }

    /**
     * Actualiza TODOS textos i18n al cambiar idioma.
     */
    private void updateAllTexts() {
        // Pestañas
        if (tabbedPane != null && tabbedPane.getTabCount() > 0) {
            tabbedPane.setTitleAt(0, I18nUtil.get("principal.tab.ovejas"));
            tabbedPane.setTitleAt(1, I18nUtil.get("principal.tab.eventos"));
            tabbedPane.setTitleAt(2, I18nUtil.get("principal.tab.stats"));
            tabbedPane.setTitleAt(3, I18nUtil.get("principal.tab.perfil"));
        }

        // Panel idioma
        if (pnlIdioma != null && pnlIdioma.getComponentCount() > 0) {
            ((JLabel) pnlIdioma.getComponent(0)).setText(I18nUtil.get("idioma.selector"));
        }

        // MenúBar principal
        if (menuBar != null) {
            menuBar.getMenu(0).setText(I18nUtil.get("principal.menu.ovejas"));
            menuBar.getMenu(1).setText(I18nUtil.get("principal.menu.eventos"));
            menuBar.getMenu(2).setText(I18nUtil.get("principal.menu.stats"));
            menuBar.getMenu(3).setText(I18nUtil.get("principal.menu.perfil"));

            // Menú Sistema 
            menuBar.getMenu(5).setText(I18nUtil.get("principal.menu.sistema"));
            menuBar.getMenu(5).getItem(0).setText(I18nUtil.get("principal.menu.cerrar")); 
            menuBar.getMenu(5).getItem(2).setText(I18nUtil.get("principal.menu.acerca")); 
        }

        // Panels HIJOS
        if (ovejasPanel != null) {
            ovejasPanel.updateAllTexts();
        }
        if (eventosPanel != null) {
            eventosPanel.updateAllTexts();
        }
        if (statsPanel != null) {
            statsPanel.updateAllTexts();
        }
        if (perfilPanel != null) {
            perfilPanel.updateAllTexts();
        }

        revalidate();
        repaint();
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