/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Vista;

import DAO.OvejaDAO;
import Modelo.Oveja;
import Modelo.Usuario;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;
import java.math.BigDecimal;
import com.toedter.calendar.JDateChooser;

/**
 * Panel CRUD completo para gestión de ovejas.
 * Implementa listar, insertar, actualizar, eliminar y reincorporar ovejas con JTable + formulario.
 * Soporta filtro activo/bajas y doble-click para editar.
 * 
 * @author Sistema de Gestión Ganadera
 */
public class OvejaPanel extends JPanel {
    
    /** Usuario autenticado que opera el panel */
    private Usuario usuarioLogueado;
    
    /** DAO para operaciones CRUD de ovejas */
    private OvejaDAO ovejaDAO;
    
    /** Tabla principal con datos de ovejas */
    private JTable tblOveja;
    
    /** Campos de formulario: número de identificación */
    private JTextField txtNumero, txtPeso, txtRaza, txtEstadoSalud;
    
    /** Selector de sexo (Hembra/Macho) */
    private JComboBox<String> cbSexo;
    
    /** Selector de fecha de nacimiento */
    private JDateChooser jdFechaNacimiento;
    
    /** Checkbox para mostrar ovejas dadas de baja */
    private JCheckBox chkMostrarBajas;
    
    /** Botón para reincorporar oveja al censo activo */
    private JButton btnReincorporar;
    
    /** Modelo de datos de la tabla */
    private DefaultTableModel model;
    
    /** ID de la oveja seleccionada para edición */
    private Integer idSeleccionado = null;

    /**
     * Constructor principal del panel de ovejas.
     * Inicializa componentes UI, DAO y carga datos iniciales.
     * 
     * @param usuario usuario logueado que usa el panel
     */
    public OvejaPanel(Usuario usuario) {
        this.usuarioLogueado = usuario;
        initComponents();
        this.ovejaDAO = new OvejaDAO();
        cargarOvejas();
    }

    /**
     * Inicializa todos los componentes Swing del panel.
     * Configura layout BorderLayout con título, botones, tabla y formulario.
     */
    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Título
        JLabel lblTitulo = new JLabel("🐑 Gestión de Ovejas", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 20));
        lblTitulo.setToolTipText("CRUD completo oveja");
        add(lblTitulo, BorderLayout.NORTH);

        // Panel superior: Botones
        JPanel pnlBotones = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnNuevo = new JButton("➕ Nueva");
        btnNuevo.setMnemonic('N');
        btnNuevo.setToolTipText("Nueva oveja (Alt+N)");
        btnNuevo.addActionListener(e -> nuevaOveja());

        JButton btnGuardar = new JButton("💾 Guardar");
        btnGuardar.setMnemonic('G');
        btnGuardar.setToolTipText("Guardar cambios (Alt+G)");
        btnGuardar.addActionListener(e -> guardarOveja());

        JButton btnEliminar = new JButton("🗑️ Dar oveja de baja");
        btnEliminar.setMnemonic('E');
        btnEliminar.setToolTipText("Eliminar fila seleccionada (Alt+E)");
        btnEliminar.addActionListener(e -> eliminarOveja());

        JButton btnRecargar = new JButton("🔄 Recargar");
        btnRecargar.setMnemonic('R');
        btnRecargar.setToolTipText("Actualizar tabla (Alt+R)");
        btnRecargar.addActionListener(e -> cargarOvejas());

        chkMostrarBajas = new JCheckBox("Mostrar ovejas de baja");
        chkMostrarBajas.addActionListener(e -> {
            boolean verBajas = chkMostrarBajas.isSelected();
            btnReincorporar.setVisible(verBajas);
            btnEliminar.setVisible(!verBajas);
            cargarOvejas();
        });

        btnReincorporar = new JButton("Reincorporar al Censo");
        btnReincorporar.setEnabled(false); // Empieza deshabilitado
        btnReincorporar.addActionListener(e -> reincorporarOveja());

        pnlBotones.add(btnNuevo);
        pnlBotones.add(btnGuardar);
        pnlBotones.add(btnEliminar);
        pnlBotones.add(btnRecargar);
        pnlBotones.add(chkMostrarBajas);
        pnlBotones.add(btnReincorporar);
        btnReincorporar.setVisible(false);
        add(pnlBotones, BorderLayout.NORTH);

        // Tabla central
        String[] columnas = {"ID", "Número", "Peso (kg)", "Raza", "Sexo", "Nacimiento", "Estado de salud"};
        model = new DefaultTableModel(columnas, 0);
        tblOveja = new JTable(model);
        tblOveja.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int fila = tblOveja.getSelectedRow();
                // Habilitamos reincorporar solo si hay una fila seleccionada Y estamos viendo bajas
                btnReincorporar.setEnabled(fila != -1 && chkMostrarBajas.isSelected());
            }
        });
        tblOveja.setToolTipText("Doble-click para editar");
        tblOveja.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) editarSeleccionada();
            }
        });
        add(new JScrollPane(tblOveja), BorderLayout.CENTER);

        // Panel inferior: Formulario
        JPanel pnlForm = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 5, 5, 5);

        c.gridx = 0; c.gridy = 0;
        pnlForm.add(new JLabel("Número:"), c);
        c.gridx = 1;
        txtNumero = new JTextField(10);
        pnlForm.add(txtNumero, c);

        c.gridx = 0; c.gridy = 1;
        pnlForm.add(new JLabel("Peso (kg):"), c);
        c.gridx = 1;
        txtPeso = new JTextField(10);
        pnlForm.add(txtPeso, c);

        c.gridx = 0; c.gridy = 2;
        pnlForm.add(new JLabel("Raza:"), c);
        c.gridx = 1;
        txtRaza = new JTextField(10);
        pnlForm.add(txtRaza, c);

        c.gridx = 0; c.gridy = 3;
        pnlForm.add(new JLabel ("Sexo:"), c);
        c.gridx = 1;
        cbSexo = new JComboBox<>(new String[]{"H", "M"});
        pnlForm.add(cbSexo, c);

        c.gridx = 0; c.gridy = 4;
        pnlForm.add(new JLabel("Estado de salud:"), c);
        c.gridx = 1;
        txtEstadoSalud = new JTextField(10);
        pnlForm.add(txtEstadoSalud, c);

        c.gridx = 0; c.gridy = 5;
        pnlForm.add(new JLabel ("Fecha de nacimiento:"), c);
        c.gridx = 1;
        jdFechaNacimiento = new JDateChooser();
        jdFechaNacimiento.setDateFormatString("yyyy-MM-dd");
        jdFechaNacimiento.setDate(new java.util.Date()); // Fecha por defecto: hoy
        pnlForm.add(jdFechaNacimiento, c);

        add(pnlForm, BorderLayout.SOUTH);

        // getRootPane().setDefaultButton(btnGuardar); Enter = Guardar
    }

    /**
     * Carga la lista de ovejas en la tabla según filtro activo/bajas.
     * Limpia modelo y popula con datos del DAO.
     */
    private void cargarOvejas() {
        try {
            boolean verActivas = !chkMostrarBajas.isSelected();

            List<Oveja> ovejas = ovejaDAO.listarSegunEstado(verActivas);
            model.setRowCount(0);
            for (Oveja o : ovejas) {
                model.addRow(new Object[]{
                    o.getId(),
                    o.getNumeroIdentificacion(),
                    o.getPesoActual(),
                    o.getRaza(),
                    o.getSexo(),
                    o.getFechaNacimiento(),
                    o.getEstadoSalud(),
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al cargar tabla");
        }
    }

    /**
     * Prepara formulario para nueva oveja.
     * Limpia todos los campos y enfoca número de identificación.
     */
    private void nuevaOveja() {
        // Reseteo total
        idSeleccionado = null;
        txtNumero.setText("");
        txtPeso.setText("");
        txtRaza.setText("");
        cbSexo.setSelectedIndex(0);
        txtEstadoSalud.setText("");
        jdFechaNacimiento.setDate(new java.util.Date());
        btnReincorporar.setEnabled(false);
        txtNumero.requestFocus();
    }

    /**
     * Guarda o actualiza oveja en base de datos.
     * Valida campos obligatorios, duplicados y formato numérico.
     * Recarga tabla y resetea formulario tras éxito.
     */
    private void guardarOveja() {
        if (ovejaDAO == null) {
            JOptionPane.showMessageDialog(this, "DAO no inicializado");
            return;
        }

        try {
            String numero = txtNumero.getText().trim();
            String pesoStr = txtPeso.getText().trim();
            String raza = txtRaza.getText().trim();
            String sexo = (String) cbSexo.getSelectedItem();
            String estadoSalud = txtEstadoSalud.getText().trim();

            System.out.println("DEBUG - Numero raw: '" + txtNumero.getText() + "'");
            System.out.println("DEBUG - Numero trim: '" + numero + "'");
            System.out.println("DEBUG - Is empty: " + numero.isEmpty());

            if (numero.isEmpty() || raza.isEmpty() || pesoStr.isEmpty() || jdFechaNacimiento.getDate() == null) {
                JOptionPane.showMessageDialog(this, "Todos los campos son OBLIGATORIOS y no pueden estar vacíos");
                txtNumero.requestFocus();
                return;
            }

            if (idSeleccionado == null) { // Solo si es una inserción nueva
                Oveja existente = ovejaDAO.buscarPorNumero(numero);
                if (existente != null) {
                    JOptionPane.showMessageDialog(this,
                        "Ya existe una oveja registrada con el número: " + numero,
                        "Error de duplicado",
                        JOptionPane.WARNING_MESSAGE);
                    txtNumero.requestFocus();
                    return;
                }
            }

            BigDecimal peso;
            try {
                peso = new BigDecimal(pesoStr);
                if (peso.compareTo(BigDecimal.ZERO) <= 0) {
                    throw new NumberFormatException("Peso > 0");
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Peso inválido (ej: 45.5)");
                txtPeso.requestFocus();
                return;
            }

            Oveja oveja = new Oveja();
            if (idSeleccionado != null) {
                oveja.setId(idSeleccionado);
            }

            if (oveja.getFechaNacimiento() == null) {
                oveja.setFechaNacimiento(java.time.LocalDate.now());
            }

            oveja.setNumeroIdentificacion(numero);
            System.out.println("DEBUG - Oveja.numeroIdentificacion: '" + oveja.getNumeroIdentificacion() + "'");
            oveja.setPesoActual(new BigDecimal(pesoStr));
            oveja.setRaza(raza);
            oveja.setSexo(sexo);
            java.util.Date date = jdFechaNacimiento.getDate();
            java.time.LocalDate localDate = date.toInstant()
                .atZone(java.time.ZoneId.systemDefault())
                .toLocalDate();
            oveja.setFechaNacimiento(localDate);
            oveja.setEstadoSalud(estadoSalud);
            oveja.setActivo(true);

            if (idSeleccionado == null) {
                if (ovejaDAO.buscarPorNumero(numero) != null) {
                    JOptionPane.showMessageDialog(this, "El número de identificación ya existe.");
                    return;
                }
                ovejaDAO.insertar(oveja);
                JOptionPane.showMessageDialog(this, "Oveja registrada con éxito");
            } else {
                ovejaDAO.actualizar(oveja);
                JOptionPane.showMessageDialog(this, "Oveja actualizada con éxito");
            }

            cargarOvejas();
            nuevaOveja();

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage() + "\n" + e.getClass().getSimpleName());
        }
    }

    /**
     * Elimina oveja seleccionada (soft-delete: activo=false).
     * Confirma con diálogo y recarga tabla.
     */
    private void eliminarOveja() {
        int fila = tblOveja.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Selecciona una oveja de la tabla para darla de baja.");
            return;
        }

        int id = (Integer) model.getValueAt(fila, 0);
        String numero = model.getValueAt(fila, 1).toString();

        int confirm = JOptionPane.showConfirmDialog(this,
            "¿Estás seguro de que deseas dar de baja esta oveja " + numero + "?",
            "Confirmar Eliminación", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                ovejaDAO.eliminar(id);
                cargarOvejas();
                nuevaOveja();

                JOptionPane.showMessageDialog(this, "Oveja dada de baja correctamente.");
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error al dar de baja: " + e.getMessage());
            }
        }
    }

    /**
     * Carga datos de oveja seleccionada en formulario para edición.
     * Maneja conversión LocalDate &lt;-&gt; Date para JDateChooser.
     * Double-click en tabla activa este método.
     */
    private void editarSeleccionada() {
        int fila = tblOveja.getSelectedRow();
        if (fila != -1) {
            if (chkMostrarBajas.isSelected()) {
                btnReincorporar.setEnabled(true);
            } else {
                btnReincorporar.setEnabled(false);
            }

            idSeleccionado = (Integer) model.getValueAt(fila, 0);
            txtNumero.setText(model.getValueAt(fila, 1).toString());
            txtPeso.setText(model.getValueAt(fila, 2).toString());
            txtRaza.setText(model.getValueAt(fila, 3).toString());

            String sexo = model.getValueAt(fila, 4).toString();
            cbSexo.setSelectedItem(sexo);

            Object fechaObj = model.getValueAt(fila, 5);
            if (fechaObj instanceof java.time.LocalDate) {
                java.time.LocalDate ld = (java.time.LocalDate) fechaObj;
                // Convertimos LocalDate -> java.util.Date para el JCalendar
                java.util.Date date = java.util.Date.from(ld.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant());
                jdFechaNacimiento.setDate(date);
            } else if (fechaObj != null) {
                // Si viene como String, intentamos parsearlo
                try {
                    java.time.LocalDate ld = java.time.LocalDate.parse(fechaObj.toString());
                    java.util.Date date = java.util.Date.from(ld.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant());
                    jdFechaNacimiento.setDate(date);
                } catch (Exception e) {
                    System.err.println("Error al parsear fecha en edición: " + e.getMessage());
                }
            }

            if (model.getValueAt(fila, 6) != null) {
                txtEstadoSalud.setText(model.getValueAt(fila, 6).toString());
            } else {
                txtEstadoSalud.setText("");
            }
        }
    }

    /**
     * Reincorpora oveja de baja al censo activo (activo=true).
     * Solo visible cuando chkMostrarBajas está activado.
     * Confirma con diálogo y recarga tabla.
     */
    private void reincorporarOveja() {
        int fila = tblOveja.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Selecciona una oveja de la lista de bajas.");
            return;
        }

        int id = (Integer) model.getValueAt(fila, 0);
        String numero = model.getValueAt(fila, 1).toString();

        int confirm = JOptionPane.showConfirmDialog(this,
            "¿Desea dar de alta nuevamente a la oveja " + numero + "?",
            "Confirmar Alta", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                ovejaDAO.reincorporar(id);
                cargarOvejas(); // Refresca la tabla
                JOptionPane.showMessageDialog(this, "La oveja ha vuelto al censo activo.");
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error al reincorporar.");
            }
        }
    }
}
