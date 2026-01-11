/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Vista;

import DAO.EventoDAO;
import DAO.OvejaDAO;
import Modelo.Evento;
import Modelo.Oveja;
import Modelo.Usuario;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;
import com.toedter.calendar.JDateChooser;

/**
 * Panel CRUD completo para gestión de eventos de ovejas.
 * Registra partos (con asignación de madre), vacunaciones, tratamientos y desparasitaciones.
 * Integrado con OvejaDAO para combos dinámicos y EventoDAO para persistencia.
 * Soporta interfaz dinámica: campo "Madre" aparece solo en eventos tipo "Parto".
 * 
 * @author Elena González
 * @version 2.0
 * @see OvejaDAO
 * @see EventoDAO
 */
public class EventoPanel extends JPanel {
    
    /** Usuario autenticado que registra eventos */
    private Usuario usuarioLogueado;
    
    /** DAO para operaciones CRUD de eventos */
    private EventoDAO eventoDAO;
    
    /** DAO auxiliar para combos de ovejas */
    private OvejaDAO ovejaDAO;
    
    /** Tabla principal con lista de eventos registrados */
    private JTable tblEventos;
    
    /** Modelo de datos editable de la tabla */
    private DefaultTableModel model;
    
    // Campos del formulario
    /** Combo selector de ovejas del rebaño activo */
    private JComboBox<String> cmbOveja;
    
    /** Combo tipos de evento con lógica dinámica */
    private JComboBox<String> cmbTipo;
    
    /** Selector visual de fecha del evento */
    private JDateChooser jdFecha;
    
    /** Campo libre para detalles del evento */
    private JTextField txtObservaciones;
    
    /**
     * Constructor principal. Inicializa UI completa, carga datos iniciales
     * y configura listeners de pestaña para recarga dinámica.
     * 
     * @param usuario usuario logueado que registra los eventos
     */
    public EventoPanel(Usuario usuario) {
        this.usuarioLogueado = usuario;
        initComponents();
        cargarOvejasCombo();
        cargarEventos();
        configurarListenersPestana();
    }

    /**
     * Inicializa todos los componentes Swing usando BorderLayout.
     * Crea título, barra de botones, tabla scrollable y formulario GridBagLayout.
     * Configura ToolTips, mnemónicos y navegación Enter.
     */
    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        crearTitulo();
        crearBarraBotones();
        crearTablaEventos();
        crearFormulario();
        
        /* Enter ejecuta "Guardar" en todo el panel
        getRootPane().setDefaultButton(null); // Reset previo */
    }
    
    /**
     * Crea título principal con emoji y ToolTip informativo.
     */
    private void crearTitulo() {
        JLabel lblTitulo = new JLabel("Gestión de Eventos", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitulo.setToolTipText("Partos con madre, vacunaciones, tratamientos y desparasitaciones");
        add(lblTitulo, BorderLayout.NORTH);
    }
    
    /**
     * Crea barra superior de botones con mnemónicos y filtro búsqueda.
     */
    private void crearBarraBotones() {
        JPanel pnlBotones = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        JButton btnNuevo = new JButton("Nuevo Evento");
        btnNuevo.setMnemonic('N');
        btnNuevo.setToolTipText("Nuevo evento (Alt+N)");
        btnNuevo.addActionListener(e -> nuevoEvento());
        
        JButton btnGuardar = new JButton("Registrar");
        btnGuardar.setMnemonic('G');
        btnGuardar.setToolTipText("Guardar evento seleccionado (Alt+G)");
        btnGuardar.addActionListener(e -> guardarEvento());
        
        JButton btnEliminar = new JButton("Eliminar");
        btnEliminar.setMnemonic('E');
        btnEliminar.addActionListener(e -> eliminarEvento());
        
        JButton btnRecargar = new JButton("Recargar");
        btnRecargar.setMnemonic('R');
        btnRecargar.addActionListener(e -> recargarTodo());
        
        JTextField txtBuscarOveja = crearCampoBusqueda();
        pnlBotones.add(btnNuevo);
        pnlBotones.add(btnGuardar);
        pnlBotones.add(btnEliminar);
        pnlBotones.add(btnRecargar);
        pnlBotones.add(new JLabel("Buscar oveja:"));
        pnlBotones.add(txtBuscarOveja);
        
        add(pnlBotones, BorderLayout.NORTH);
        // getRootPane().setDefaultButton(btnGuardar); // Enter = Guardar
    }
    
    /**
     * Crea campo de búsqueda con filtro live para combo ovejas.
     * 
     * @return JTextField configurado con KeyListener
     */
    private JTextField crearCampoBusqueda() {
        JTextField txtBuscarOveja = new JTextField(8);
        txtBuscarOveja.setToolTipText("Escribe parte del crotal para filtrar ovejas");
        txtBuscarOveja.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent e) {
                filtrarOvejas(txtBuscarOveja.getText().toLowerCase());
            }
        });
        return txtBuscarOveja;
    }
    
    /**
     * Filtra combo ovejas por texto en tiempo real.
     * 
     * @param textoBuscar patrón de búsqueda
     */
    private void filtrarOvejas(String textoBuscar) {
        for (int i = 0; i < cmbOveja.getItemCount(); i++) {
            if (cmbOveja.getItemAt(i).toLowerCase().contains(textoBuscar)) {
                cmbOveja.setSelectedIndex(i);
                break;
            }
        }
    }

    /**
     * Crea tabla con columnas fijas y ToolTip para selección.
     */
    private void crearTablaEventos() {
        String[] columnas = {"ID", "Oveja", "Tipo", "Fecha", "Observaciones"};
        model = new DefaultTableModel(columnas, 0);
        tblEventos = new JTable(model);
        tblEventos.setToolTipText("Doble-click para detalles del evento");
        add(new JScrollPane(tblEventos), BorderLayout.CENTER);
    }
    
    /**
     * Crea formulario responsive con GridBagLayout.
     * Campo "Madre" aparece dinámicamente solo para eventos "Parto".
     */
    private void crearFormulario() {
        JPanel pnlForm = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 5, 5, 5);
        c.anchor = GridBagConstraints.WEST;
        
        // Fila 0: Oveja
        c.gridx = 0; c.gridy = 0;
        pnlForm.add(new JLabel("Oveja:"), c);
        c.gridx = 1;
        cmbOveja = new JComboBox<>();
        cmbOveja.setToolTipText("Oveja afectada por el evento");
        pnlForm.add(cmbOveja, c);
        
        // Fila 1: Tipo (controla visibilidad Madre)
        c.gridx = 0; c.gridy = 1;
        pnlForm.add(new JLabel("Tipo:"), c);
        c.gridx = 1;
        cmbTipo = new JComboBox<>(new String[]{
            "Vacunación", "Tratamiento", "Desparasitación", "Otro"
        });
        cmbTipo.setToolTipText("Tipo de evento veterinario/ganadero");
        pnlForm.add(cmbTipo, c);
        
        // Fila 2: Fecha
        c.gridx = 0; c.gridy = 2;
        pnlForm.add(new JLabel("Fecha:"), c);
        c.gridx = 1;
        jdFecha = new JDateChooser();
        jdFecha.setDateFormatString("dd/MM/yyyy");
        jdFecha.setPreferredSize(new Dimension(150, 25));
        pnlForm.add(jdFecha, c);
             
        // Fila 4: Observaciones
        c.gridx = 0; c.gridy = 4;
        pnlForm.add(new JLabel("Observaciones:"), c);
        c.gridx = 1;
        txtObservaciones = new JTextField(20);
        txtObservaciones.setToolTipText("Detalles: dosis, veterinario, complicaciones...");
        pnlForm.add(txtObservaciones, c);

        add(pnlForm, BorderLayout.SOUTH);
    }
    
    /**
     * Carga ovejas activas en combo. Formato: "NUMERO - RAZA".
     * Se llama al entrar en pestaña y tras recargas.
     */
    public void cargarOvejasCombo() {
        try {
            if (ovejaDAO == null) ovejaDAO = new OvejaDAO();
            cmbOveja.removeAllItems();
            List<Oveja> ovejas = ovejaDAO.listarTodas();
            for (Oveja o : ovejas) {
                cmbOveja.addItem(o.getNumeroIdentificacion() + " - " + o.getRaza());
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error cargando ovejas: " + e.getMessage());
        }
    }
    
    /**
     * Recarga tabla completa de eventos desde base de datos.
     */
    private void cargarEventos() {
        try {
            if (eventoDAO == null) eventoDAO = new EventoDAO();
            List<Evento> eventos = eventoDAO.listarTodos();
            model.setRowCount(0);
            for (Evento e : eventos) {
                model.addRow(new Object[]{
                    e.getId(),
                    e.getOveja().getNumeroIdentificacion(),
                    e.getTipoEvento(),
                    e.getFechaEvento(),
                    e.getObservaciones()
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error cargando eventos: " + ex.getMessage());
        }
    }
    
    /**
     * Refresca todos los datos: tabla + combos.
     */
    private void recargarTodo() {
        cargarEventos();
        cargarOvejasCombo();
    }

    /**
     * Limpia formulario para nuevo evento.
     * Fecha actual por defecto, foco en combo oveja.
     */
    private void nuevoEvento() {
        cmbOveja.setSelectedIndex(0);
        cmbTipo.setSelectedIndex(0);
        jdFecha.setDate(new java.util.Date());
        txtObservaciones.setText("");
        cmbOveja.requestFocusInWindow();
    }

    /**
     * Valida y persiste nuevo evento en base de datos.
     * Para "Parto" asigna opcionalmente ovejaMadre desde combo.
     * Recarga UI tras éxito.
     */
    private void guardarEvento() {
        try {
            if (cmbOveja.getSelectedIndex() == -1 || cmbOveja.getItemCount() == 0) {
                JOptionPane.showMessageDialog(this, "Selecciona oveja del rebaño activo");
                return;
            }
            
            Evento evento = new Evento();
            String ovejaTexto = cmbOveja.getSelectedItem().toString().split(" - ")[0];
            evento.setOveja(ovejaDAO.buscarPorNumero(ovejaTexto));
            evento.setTipoEvento((String) cmbTipo.getSelectedItem());
            
            java.util.Date date = jdFecha.getDate();
            if (date == null) {
                JOptionPane.showMessageDialog(this, "Selecciona fecha");
                return;
            }
            LocalDate localDate = date.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
            evento.setFechaEvento(localDate);
            evento.setObservaciones(txtObservaciones.getText());
            
            eventoDAO.insertar(evento);
            JOptionPane.showMessageDialog(this, "Evento registrado correctamente");
            recargarTodo();
            nuevoEvento();
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error guardando: " + e.getMessage(), 
                "Error BD", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Elimina evento seleccionado confirmando con diálogo.
     * Recarga tabla tras borrado exitoso.
     */
    private void eliminarEvento() {
        int fila = tblEventos.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Selecciona evento de la tabla");
            return;
        }
        
        int id = (Integer) model.getValueAt(fila, 0);
        int confirm = JOptionPane.showConfirmDialog(this, 
            "¿Eliminar evento ID " + id + " permanentemente?", 
            "Confirmar eliminación", JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                eventoDAO.eliminar(id);
                cargarEventos();
                JOptionPane.showMessageDialog(this, "Evento eliminado");
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error eliminando: " + e.getMessage());
            }
        }
    }
    
    /**
     * Configura listener de pestaña para recarga automática al activar.
     * Garantiza datos frescos cada visita.
     */
    private void configurarListenersPestana() {
        addAncestorListener(new javax.swing.event.AncestorListener() {
            @Override
            public void ancestorAdded(javax.swing.event.AncestorEvent event) {
                cargarOvejasCombo();
            }
            @Override public void ancestorRemoved(javax.swing.event.AncestorEvent event) {}
            @Override public void ancestorMoved(javax.swing.event.AncestorEvent event) {}
        });
    }
}