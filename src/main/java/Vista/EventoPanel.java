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

import java.time.LocalDate;

/**
 * Panel CRUD completo para gestión de eventos de ovejas.
 * Registra partos, vacunaciones, tratamientos y desparasitaciones con selección de oveja.
 * Integrado con OvejaDAO para combo y EventoDAO para persistencia.
 * 
 * @author Sistema de Gestión Ganadera
 */
public class EventoPanel extends JPanel {
    
    /** Usuario autenticado que registra eventos */
    private Usuario usuarioLogueado;
    
    /** DAO para operaciones CRUD de eventos */
    private EventoDAO eventoDAO;
    
    /** DAO auxiliar para búsqueda de ovejas */
    private OvejaDAO ovejaDAO;
    
    /** Tabla principal con lista de eventos */
    private JTable tblEventos;
    
    /** Modelo de datos de la tabla */
    private DefaultTableModel model;
    
    // Formulario
    /** Combo selector de ovejas del rebaño */
    private JComboBox<String> cmbOveja;
    
    /** Combo tipos de evento (Parto, Vacunación, etc.) */
    private JComboBox<String> cmbTipo;
    
    /** Selector de fecha del evento */
    private com.toedter.calendar.JDateChooser jdFecha;
    
    private JTextField txtObservaciones, txtBuscarOveja; 

    /**
     * Constructor principal del panel de eventos.
     * Inicializa UI, carga combo de ovejas y tabla de eventos.
     * 
     * @param usuario usuario logueado que registra los eventos
     */
    public EventoPanel(Usuario usuario) {
        this.usuarioLogueado = usuario;
        initComponents();
        cargarOvejasCombo();
        cargarEventos();

        this.addAncestorListener(new javax.swing.event.AncestorListener() {
            @Override
            public void ancestorAdded(javax.swing.event.AncestorEvent event) {
                // Este código se ejecuta cada vez que el usuario hace clic en la pestaña "Eventos"
                cargarOvejasCombo();
            }

            @Override
            public void ancestorRemoved(javax.swing.event.AncestorEvent event) {
            }

            @Override
            public void ancestorMoved(javax.swing.event.AncestorEvent event) {
            }
        });
    }

    /**
     * Inicializa componentes Swing del panel.
     * Layout BorderLayout: título, botones, tabla central y formulario inferior.
     */
    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Título
        JLabel lblTitulo = new JLabel("📅 Gestión de Eventos", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 20));
        lblTitulo.setToolTipText("Partos, vacunaciones, tratamientos");
        add(lblTitulo, BorderLayout.NORTH);

        // Botones
        JPanel pnlBotones = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnNuevo = new JButton("➕ Nuevo Evento");
        btnNuevo.setMnemonic('N');
        btnNuevo.setToolTipText("Nuevo evento (Alt+N)");
        btnNuevo.addActionListener(e -> nuevoEvento());
        
        JButton btnGuardar = new JButton("💾 Registrar");
        btnGuardar.setMnemonic('G');
        btnGuardar.setToolTipText("Guardar evento (Alt+G)");
        btnGuardar.addActionListener(e -> guardarEvento());
        
        JButton btnEliminar = new JButton("🗑️ Eliminar");
        btnEliminar.setMnemonic('E');
        btnEliminar.addActionListener(e -> eliminarEvento());
        
        JButton btnRecargar = new JButton("🔄 Recargar");
        btnRecargar.setMnemonic('R');
        btnRecargar.addActionListener(e -> {
            cargarEventos();      // Refresca la tabla
            cargarOvejasCombo(); // Refresca el desplegable
        });

        JTextField txtBuscarOveja = new JTextField(5);
        txtBuscarOveja.setToolTipText("Escribe parte del crotal para filtrar");
        txtBuscarOveja.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent e) {
                String texto = txtBuscarOveja.getText().toLowerCase();
                for (int i = 0; i < cmbOveja.getItemCount(); i++) {
                    if (cmbOveja.getItemAt(i).toLowerCase().contains(texto)) {
                        cmbOveja.setSelectedIndex(i);
                        break;
                    }
                }
            }
        });
        
        
        pnlBotones.add(btnNuevo);
        pnlBotones.add(btnGuardar);
        pnlBotones.add(btnEliminar);
        pnlBotones.add(btnRecargar);
        pnlBotones.add(new JLabel("Buscar/Oveja:"));
        pnlBotones.add(txtBuscarOveja, BorderLayout.EAST);
        add(pnlBotones, BorderLayout.NORTH);

        // Tabla
        String[] columnas = {"ID", "Oveja", "Tipo", "Fecha", "Observaciones"};
        model = new DefaultTableModel(columnas, 0);
        tblEventos = new JTable(model);
        tblEventos.setToolTipText("Doble-click para editar");
        add(new JScrollPane(tblEventos), BorderLayout.CENTER);

        // Formulario
        JPanel pnlForm = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 5, 5, 5);
        
        // Oveja
        c.gridx = 0; c.gridy = 0;
        pnlForm.add(new JLabel("Oveja:"), c);
        c.gridx = 1;
        cmbOveja = new JComboBox<>();
        cmbOveja.setToolTipText("Selecciona oveja del rebaño");
        pnlForm.add(cmbOveja, c);
        
        // Tipo evento
        c.gridx = 0; c.gridy = 1;
        pnlForm.add(new JLabel("Tipo:"), c);
        c.gridx = 1;
        cmbTipo = new JComboBox<>(new String[]{"Parto", "Vacunación", "Tratamiento", "Desparasitación"});
        cmbTipo.setToolTipText("Tipo de evento");
        pnlForm.add(cmbTipo, c);
        
        // Fecha
        c.gridx = 0;
        c.gridy = 2;
        pnlForm.add(new JLabel("Fecha:"), c);
        c.gridx = 1;
        jdFecha = new com.toedter.calendar.JDateChooser();
        jdFecha.setDateFormatString("yyyy-MM-dd");
        jdFecha.setPreferredSize(new Dimension(150, 25));
        pnlForm.add(jdFecha, c);
        
        // Observaciones
        c.gridx = 0; c.gridy = 3;
        pnlForm.add(new JLabel("Observaciones:"), c);
        c.gridx = 1;
        txtObservaciones = new JTextField(20);
        pnlForm.add(txtObservaciones, c);
        
        add(pnlForm, BorderLayout.SOUTH);
        
        // getRootPane().setDefaultButton(btnGuardar);
    }

    /**
     * Carga todas las ovejas activas en el combo selector.
     * Formato: "NUMERO - RAZA" para fácil identificación.
     */
    private void cargarOvejasCombo() {
        try {
            if (ovejaDAO == null) {
                ovejaDAO = new OvejaDAO();
            }

            cmbOveja.removeAllItems();
            List<Oveja> ovejas = ovejaDAO.listarTodas(); 

            for (Oveja o : ovejas) {
                cmbOveja.addItem(o.getNumeroIdentificacion() + " - " + o.getRaza());
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error cargando ovejas");
        }
    }

    /**
     * Recarga la tabla con todos los eventos registrados.
     * Muestra ID, oveja, tipo, fecha y observaciones.
     */
    private void cargarEventos() {
        try {
            eventoDAO = new EventoDAO();
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
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    /**
     * Resetea formulario para nuevo evento.
     * Fecha por defecto: hoy, enfoca combo oveja.
     */
    private void nuevoEvento() {
        cmbOveja.setSelectedIndex(0);
        cmbTipo.setSelectedIndex(0);
        jdFecha.setDate(new java.util.Date());
        txtObservaciones.setText("");
        cmbOveja.requestFocus();
    }

    /**
     * Crea y persiste nuevo evento en base de datos.
     * Valida selección de oveja, parsea fecha yyyy-MM-dd.
     * Recarga tabla y resetea formulario tras éxito.
     */
    private void guardarEvento() {
        try {
            if (cmbOveja.getSelectedIndex() == -1) {
                JOptionPane.showMessageDialog(this, "Selecciona oveja");
                return;
            }
            if (cmbOveja.getItemCount() == 0) {
                JOptionPane.showMessageDialog(this, "No hay ovejas activas para registrar eventos.");
                return;
            }
            
            Evento evento = new Evento();
            // Extrae número de oveja del formato "NUMERO - RAZA"
            String ovejaTexto = cmbOveja.getSelectedItem().toString().split(" - ")[0];
            evento.setOveja(ovejaDAO.buscarPorNumero(ovejaTexto));
            evento.setTipoEvento((String) cmbTipo.getSelectedItem());
            
            java.util.Date date = jdFecha.getDate();
            LocalDate localDate = date.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
            evento.setFechaEvento(localDate);
            
            evento.setObservaciones(txtObservaciones.getText());
            
            eventoDAO = new EventoDAO();
            eventoDAO.insertar(evento);
            
            JOptionPane.showMessageDialog(this, "Evento registrado!");
            cargarEventos();
            nuevoEvento();
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    /**
     * Elimina evento seleccionado por ID.
     * Confirma con diálogo antes de ejecutar DAO.eliminar().
     */
    private void eliminarEvento() {
        int fila = tblEventos.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Selecciona evento");
            return;
        }
        
        int id = (Integer) model.getValueAt(fila, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "¿Eliminar evento ID " + id + "?");
        
        if (confirm == JOptionPane.YES_OPTION) {
            eventoDAO.eliminar(id);
            cargarEventos();
        }
    }
}


