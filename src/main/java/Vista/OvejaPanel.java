/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Vista;

import DAO.OvejaDAO;
import DAO.EventoDAO;
import Modelo.Oveja;
import Modelo.Usuario;
import Modelo.Evento;
import Util.HibernateUtil;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;
import java.math.BigDecimal;
import com.toedter.calendar.JDateChooser;
import java.time.LocalDate;
import Util.I18nUtil;
import java.time.ZoneId;
import java.util.Date;
import org.hibernate.Session;
import org.hibernate.Transaction;

/**
 * Panel CRUD completo para gestión de ovejas.
 * Implementa listar, insertar, actualizar, eliminar y reincorporar ovejas con JTable + formulario.
 * Soporta filtro activo/bajas y doble-click para editar.
 * 
 * @author Elena González
 * @version 2.0
 */
public class OvejaPanel extends JPanel{
    
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

    /** Botón para registrar parto en oveja seleccionada (solo hembras adultas) */
    private JButton btnParto;
    
    /** Modelo de datos de la tabla */
    private DefaultTableModel model;
    
    /** ID de la oveja seleccionada para edición */
    private Integer idSeleccionado = null;
 
    /** Etiqueta que muestra el título principal del formulario o ventana. */
    private JLabel lblTitulo;
    
    /** Botón que permite crear un nuevo registro o limpiar el formulario. */
    private JButton btnNuevo;

    /** Botón utilizado para guardar o actualizar la información del registro actual. */
    private JButton btnGuardar;

    /** Botón que elimina el registro seleccionado o activo. */
    private JButton btnEliminar;

    /** Botón que recarga los datos desde la base o refresca la vista actual. */
    private JButton btnRecargar;

    /** Panel que contiene el conjunto de botones de acción. */
    private JPanel pnlBotones;

    /** Panel que agrupa los elementos del formulario principal. */
    private JPanel pnlForm;

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
        updateAllTexts();
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
        lblTitulo = new JLabel(I18nUtil.get("oveja.title"), SwingConstants.CENTER);
        lblTitulo.setToolTipText(I18nUtil.get("oveja.tooltip.title"));
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 20));
        add(lblTitulo, BorderLayout.NORTH);

        // Panel superior: Botones
        pnlBotones = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        btnNuevo = new JButton(I18nUtil.get("oveja.btn.nuevo"));
        btnNuevo.setMnemonic('N');
        btnNuevo.setToolTipText("Nueva oveja (Alt+N)");
        btnNuevo.addActionListener(e -> nuevaOveja());

        btnGuardar = new JButton(I18nUtil.get("oveja.btn.guardar"));
        btnGuardar.setMnemonic('G');
        btnGuardar.setToolTipText("Guardar cambios (Alt+G)");
        btnGuardar.addActionListener(e -> guardarOveja());

        btnEliminar = new JButton(I18nUtil.get("oveja.btn.eliminar"));
        btnEliminar.setMnemonic('E');
        btnEliminar.setToolTipText("Eliminar fila seleccionada (Alt+E)");
        btnEliminar.addActionListener(e -> eliminarOveja());

        btnRecargar = new JButton(I18nUtil.get("oveja.btn.recargar"));
        btnRecargar.setMnemonic('R');
        btnRecargar.setToolTipText("Actualizar tabla (Alt+R)");
        btnRecargar.addActionListener(e -> cargarOvejas());

        chkMostrarBajas = new JCheckBox(I18nUtil.get("oveja.chk.bajas"));
        chkMostrarBajas.addActionListener(e -> {
            boolean verBajas = chkMostrarBajas.isSelected();
            btnReincorporar.setVisible(verBajas);
            btnEliminar.setVisible(!verBajas);
            cargarOvejas();
        });

        btnReincorporar = new JButton(I18nUtil.get("oveja.btn.reincorporar"));
        btnReincorporar.setEnabled(false); // Empieza deshabilitado
        btnReincorporar.addActionListener(e -> reincorporarOveja());

        btnParto = new JButton(I18nUtil.get("oveja.btn.parto"));
        btnParto.setEnabled(false);
        btnParto.addActionListener(e -> registrarParto());

        pnlBotones.add(btnNuevo);
        pnlBotones.add(btnGuardar);
        pnlBotones.add(btnEliminar);
        pnlBotones.add(btnRecargar);
        pnlBotones.add(chkMostrarBajas);
        pnlBotones.add(btnReincorporar);
        pnlBotones.add(btnParto); 
        btnReincorporar.setVisible(false);
        add(pnlBotones, BorderLayout.NORTH);
        
        // Tabla central
        String[] columnas = {
            I18nUtil.get("oveja.col.id"),
            I18nUtil.get("oveja.col.numero"),
            I18nUtil.get("oveja.col.peso"),
            I18nUtil.get("oveja.col.raza"),
            I18nUtil.get("oveja.col.sexo"),
            I18nUtil.get("oveja.col.nacimiento"),
            I18nUtil.get("oveja.col.estado")
        };
        model = new DefaultTableModel(columnas, 0);
        tblOveja = new JTable(model);
        tblOveja.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int fila = tblOveja.getSelectedRow();
                boolean puedeParto = fila >= 0 && "H".equals((String) tblOveja.getValueAt(fila, 4));

                btnParto.setEnabled(puedeParto);
                btnReincorporar.setEnabled(fila >= 0 && chkMostrarBajas.isSelected());

                if (puedeParto) {
                    btnParto.setToolTipText(String.format(I18nUtil.get("oveja.tooltip.parto.ready"), 
                            tblOveja.getValueAt(fila, 1)));
                } else {
                    btnParto.setToolTipText(I18nUtil.get("oveja.tooltip.parto.select"));
                }

                btnParto.repaint();
            }
        });
        add(new JScrollPane(tblOveja), BorderLayout.CENTER);

        tblOveja.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    editarSeleccionada();
                }
            }
        });

        // Panel inferior: Formulario
        pnlForm = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 5, 5, 5);

        c.gridx = 0; c.gridy = 0;
        pnlForm.add(new JLabel(I18nUtil.get("oveja.form.numero")), c);
        c.gridx = 1;
        txtNumero = new JTextField(10);
        pnlForm.add(txtNumero, c);

        c.gridx = 0; c.gridy = 1;
        pnlForm.add(new JLabel(I18nUtil.get("oveja.form.peso")), c);
        c.gridx = 1;
        txtPeso = new JTextField(10);
        pnlForm.add(txtPeso, c);

        c.gridx = 0; c.gridy = 2;
        pnlForm.add(new JLabel(I18nUtil.get("oveja.form.raza")), c);
        c.gridx = 1;
        txtRaza = new JTextField(10);
        pnlForm.add(txtRaza, c);

        c.gridx = 0; c.gridy = 3;
        pnlForm.add(new JLabel(I18nUtil.get("oveja.form.sexo")), c);
        c.gridx = 1;
        cbSexo = new JComboBox<>(new String[]{"H", "M"});
        pnlForm.add(cbSexo, c);

        c.gridx = 0; c.gridy = 4;
        pnlForm.add(new JLabel(I18nUtil.get("oveja.form.estado")), c);
        c.gridx = 1;
        txtEstadoSalud = new JTextField(10);
        pnlForm.add(txtEstadoSalud, c);

        c.gridx = 0; c.gridy = 5;
        pnlForm.add(new JLabel(I18nUtil.get("oveja.form.nacimiento")), c);
        c.gridx = 1;
        jdFechaNacimiento = new JDateChooser();
        jdFechaNacimiento.setDateFormatString("yyyy-MM-dd");
        jdFechaNacimiento.setDate(new java.util.Date()); // Fecha por defecto: hoy
        pnlForm.add(jdFechaNacimiento, c);

        add(pnlForm, BorderLayout.SOUTH);

        // getRootPane().setDefaultButton(btnGuardar); // Enter = Guardar
    }

    /**
     * Carga la lista de ovejas en la tabla según filtro activo/bajas.
     * Limpia modelo y carga datos del DAO.
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
            JOptionPane.showMessageDialog(this, I18nUtil.get("oveja.error.load"));
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
     * Guarda o actualiza oveja en base de datos. Valida campos obligatorios,
     * duplicados y formato numérico. Recarga tabla y resetea formulario tras
     * éxito.
     */
    private void guardarOveja() {
        if (ovejaDAO == null) {
            JOptionPane.showMessageDialog(this, I18nUtil.get("oveja.error.dao"));
            return;
        }

        try {
            String numero = txtNumero.getText().trim();
            String pesoStr = txtPeso.getText().trim();
            String raza = txtRaza.getText().trim();
            String sexo = (String) cbSexo.getSelectedItem();
            String estadoSalud = txtEstadoSalud.getText().trim();

            // VALIDACIÓN CAMPOS VACÍOS
            if (numero.isEmpty() || raza.isEmpty() || pesoStr.isEmpty() || jdFechaNacimiento.getDate() == null) {
                JOptionPane.showMessageDialog(this, I18nUtil.get("oveja.error.required"));
                txtNumero.requestFocus();
                return;
            }

            // VALIDACIÓN DUPLICADO (solo INSERT)
            if (idSeleccionado == null) {
                Oveja existente = ovejaDAO.buscarPorNumero(numero);
                if (existente != null) {
                    JOptionPane.showMessageDialog(this,
                            String.format(I18nUtil.get("oveja.error.duplicate"), numero),
                            String.format(I18nUtil.get("oveja.error.duplicate"), numero),
                            JOptionPane.WARNING_MESSAGE);
                    txtNumero.requestFocus();
                    return;
                }
            }

            // VALIDACIÓN PESO
            BigDecimal peso;
            try {
                peso = new BigDecimal(pesoStr);
                if (peso.compareTo(BigDecimal.ZERO) <= 0) {
                    throw new NumberFormatException("Peso > 0");
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, I18nUtil.get("oveja.error.peso"));
                txtPeso.requestFocus();
                return;
            }

            // CREAR OVEJA
            Oveja oveja = new Oveja();
            if (idSeleccionado != null) {
                oveja.setId(idSeleccionado);
            }
            oveja.setNumeroIdentificacion(numero);
            oveja.setPesoActual(peso);  // ✅ Usa variable peso validada
            oveja.setRaza(raza);
            oveja.setSexo(sexo);
            java.util.Date date = jdFechaNacimiento.getDate();
            java.time.LocalDate localDate = date.toInstant()
                    .atZone(java.time.ZoneId.systemDefault())
                    .toLocalDate();
            oveja.setFechaNacimiento(localDate);
            oveja.setEstadoSalud(estadoSalud);
            oveja.setActivo(true);

            // GUARDAR EN BD
            if (idSeleccionado == null) {
                ovejaDAO.insertar(oveja);
                JOptionPane.showMessageDialog(this, I18nUtil.get("oveja.success.insert"));
            } else {
                ovejaDAO.actualizar(oveja);
                JOptionPane.showMessageDialog(this, I18nUtil.get("oveja.success.update"));
            }
            cargarOvejas();
            nuevaOveja();

        } catch (Exception e) {  
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    I18nUtil.get("oveja.error.dao") + ": " + e.getMessage());
        }
    }

    /**
     * Elimina oveja seleccionada (soft-delete: activo=false).
     * Confirma con diálogo y recarga tabla.
     */
    private void eliminarOveja() {
        int fila = tblOveja.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, I18nUtil.get("oveja.select.delete"));
            return;
        }

        int id = (Integer) model.getValueAt(fila, 0);
        String numero = model.getValueAt(fila, 1).toString();

        int confirm = JOptionPane.showConfirmDialog(this,
                String.format(I18nUtil.get("oveja.confirm.delete"), numero),
                I18nUtil.get("oveja.confirm.delete"), JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                ovejaDAO.eliminar(id);
                cargarOvejas();
                nuevaOveja();

                JOptionPane.showMessageDialog(this, I18nUtil.get("oveja.success.delete"));
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this,
                        String.format(I18nUtil.get("oveja.error.delete"), e.getMessage()));
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
        if (fila == -1) {
            return;
        }

        // UI baja/reincorporar
        btnReincorporar.setEnabled(chkMostrarBajas.isSelected());

        // Cargar datos fila → UI
        idSeleccionado = (Integer) model.getValueAt(fila, 0);
        txtNumero.setText(model.getValueAt(fila, 1).toString());
        txtPeso.setText(model.getValueAt(fila, 2).toString());
        txtRaza.setText(model.getValueAt(fila, 3).toString());

        String sexo = model.getValueAt(fila, 4).toString();
        cbSexo.setSelectedItem(sexo);

        // Fecha: LocalDate → Date (JDateChooser)
        Object fechaObj = model.getValueAt(fila, 5);
        if (fechaObj instanceof LocalDate ld) {
            jdFechaNacimiento.setDate(Date.from(ld.atStartOfDay(ZoneId.systemDefault()).toInstant()));
        } else if (fechaObj != null) {
            try {
                LocalDate ld = LocalDate.parse(fechaObj.toString());
                jdFechaNacimiento.setDate(Date.from(ld.atStartOfDay(ZoneId.systemDefault()).toInstant()));
            } catch (Exception e) {
                System.err.println("Error parse fecha: " + e.getMessage());
            }
        }

        txtEstadoSalud.setText(model.getValueAt(fila, 6) != null
                ? model.getValueAt(fila, 6).toString() : "");

        System.out.println("EDITAR: Cargada ID=" + idSeleccionado);
    }

    /**
     * Reincorpora oveja de baja al censo activo (activo=true).
     * Solo visible cuando chkMostrarBajas está activado.
     * Confirma con diálogo y recarga tabla.
     */
    private void reincorporarOveja() {
        int fila = tblOveja.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, I18nUtil.get("oveja.select.reincorporar"));
            return;
        }

        int id = (Integer) model.getValueAt(fila, 0);
        String numero = model.getValueAt(fila, 1).toString();

        int confirm = JOptionPane.showConfirmDialog(this,
                String.format(I18nUtil.get("oveja.confirm.reincorporar"), numero),
                I18nUtil.get("oveja.confirm.reincorporar.title"),
                JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                ovejaDAO.reincorporar(id);
                cargarOvejas();
                JOptionPane.showMessageDialog(this, I18nUtil.get("oveja.success.reincorporar"));
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, I18nUtil.get("oveja.error.reincorporar"));
            }
        }
                
    }

    /**
     * Registra parto completo para oveja madre seleccionada. 1. Valida hembra
     * adulta activa (>30kg) 2. Crea nueva oveja hija con ID ingresada 3. Guarda
     * hija (OvejaDAO) + evento parto (EventoDAO) 4. Recarga tabla y confirma
     *
     * @see OvejaDAO#insertar(Oveja)
     * @see EventoDAO#insertar(Evento)
     */
    private void registrarParto() {
        int fila = tblOveja.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, I18nUtil.get("oveja.parto.select"));
            return;
        }

        Oveja madre = obtenerOvejaPorFila(fila);

        // DEBUG: Verifica madre
        System.out.println("DEBUG Parto - Madre: " + madre.getNumeroIdentificacion()
                + " | Raza: '" + madre.getRaza() + "' | Sexo: " + madre.getSexo());

        if (!"H".equals(madre.getSexo()) || !madre.isActivo()
                || (madre.getPesoActual() != null && madre.getPesoActual().compareTo(new BigDecimal("30")) < 0)) {
            JOptionPane.showMessageDialog(this,
                    String.format(I18nUtil.get("oveja.parto.invalid"), madre.getNumeroIdentificacion()));
            return;
        }

        // ID sugerencia
        long total = ovejaDAO.contarActivas();
        int sugerencia = (int) Math.min(total + 1, Integer.MAX_VALUE);
        String numHija = JOptionPane.showInputDialog(this,
                String.format(I18nUtil.get("oveja.parto.id"), sugerencia),
                String.format(I18nUtil.get("oveja.parto.dialog.title"), madre.getNumeroIdentificacion()),
                JOptionPane.PLAIN_MESSAGE);

        if (numHija != null && !numHija.trim().isEmpty()) {
            String idHija = numHija.trim().toUpperCase();
            try {
                if (ovejaDAO.buscarPorNumero(idHija) != null) {
                    JOptionPane.showMessageDialog(this, String.format(I18nUtil.get("oveja.parto.duplicate"), idHija));
                    return;
                }

                String razaMadre = madre.getRaza() != null ? madre.getRaza() : "Sin raza";

                Session session = HibernateUtil.getSessionFactory().openSession();
                Transaction tx = null;

                try {
                    tx = session.beginTransaction();

                    // HIJA: copia raza madre (null-safe)
                    Oveja hija = new Oveja();
                    hija.setNumeroIdentificacion(idHija);
                    hija.setRaza(razaMadre);
                    hija.setSexo("H");
                    hija.setPesoActual(new BigDecimal("3.5"));
                    hija.setFechaNacimiento(LocalDate.now());
                    hija.setEstadoSalud("Sana - Recién nacida");
                    hija.setActivo(true);

                    ovejaDAO.insertar(hija);
                    System.out.println("DEBUG Parto - Hija guardada: " + idHija
                            + " | Raza: '" + hija.getRaza() + "'");

                    // Evento parto MADRE
                    EventoDAO eventoDAO = new EventoDAO();
                    Evento parto = new Evento();
                    parto.setTipoEvento("Parto");
                    parto.setFechaEvento(LocalDate.now());
                    parto.setOveja(madre);
                    parto.setOvejaMadre(madre);  // Madre misma
                    session.persist(parto);

                    tx.commit();

                    System.out.println("PARTO OK: Hija ID=" + hija.getId() + " Evento OK");

                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, I18nUtil.get("oveja.error.parto") + ": " + ex.getMessage());
                    ex.printStackTrace();
                    return;
                }

                cargarOvejas();
                JOptionPane.showMessageDialog(this, String.format(I18nUtil.get("oveja.parto.success"),
                        madre.getNumeroIdentificacion(), idHija, "nuevo"));

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, I18nUtil.get("oveja.error.parto") + ": " + ex.getMessage());
                ex.printStackTrace();
            }
        }
    }

    /**
     * Obtiene Oveja completa por fila de tabla (llamado desde registrarParto).
     * Usa ID para consulta completa en DAO.
     *
     * @param fila índice de fila seleccionada
     * @return Oveja completa con todos los datos
     */
    private Oveja obtenerOvejaPorFila(int fila) {
        Oveja m = new Oveja();
        m.setId((Integer) tblOveja.getValueAt(fila, 0));
        m.setNumeroIdentificacion((String) tblOveja.getValueAt(fila, 1));

        String pesoStr = tblOveja.getValueAt(fila, 2).toString().trim();
        m.setPesoActual(new BigDecimal(pesoStr.replace(",", ".")));

        m.setSexo((String) tblOveja.getValueAt(fila, 4));
        m.setActivo(!chkMostrarBajas.isSelected());
        return m;
    }
    
    /**
     * Actualiza textos i18n del panel según locale actual.
     */
    protected void updateAllTexts() {
        lblTitulo.setText(I18nUtil.get("oveja.title"));
        lblTitulo.setToolTipText(I18nUtil.get("oveja.tooltip.title"));

        // Botones
        btnNuevo.setText(I18nUtil.get("oveja.btn.nuevo"));
        btnGuardar.setText(I18nUtil.get("oveja.btn.guardar"));
        btnEliminar.setText(I18nUtil.get("oveja.btn.eliminar"));
        btnRecargar.setText(I18nUtil.get("oveja.btn.recargar"));
        btnReincorporar.setText(I18nUtil.get("oveja.btn.reincorporar"));
        btnParto.setText(I18nUtil.get("oveja.btn.parto"));

        // Checkbox
        chkMostrarBajas.setText(I18nUtil.get("oveja.chk.bajas"));

        // Tabla columnas
        model.setColumnIdentifiers(new String[]{
            I18nUtil.get("oveja.col.id"),
            I18nUtil.get("oveja.col.numero"),
            I18nUtil.get("oveja.col.peso"),
            I18nUtil.get("oveja.col.raza"),
            I18nUtil.get("oveja.col.sexo"),
            I18nUtil.get("oveja.col.nacimiento"),
            I18nUtil.get("oveja.col.estado")
        });

        // Labels formulario
        if (pnlForm != null) {
            Component[] labels = pnlForm.getComponents();
            if (labels.length >= 12) {
                ((JLabel) labels[0]).setText(I18nUtil.get("oveja.form.numero"));
                ((JLabel) labels[2]).setText(I18nUtil.get("oveja.form.peso"));
                ((JLabel) labels[4]).setText(I18nUtil.get("oveja.form.raza"));
                ((JLabel) labels[6]).setText(I18nUtil.get("oveja.form.sexo"));
                ((JLabel) labels[8]).setText(I18nUtil.get("oveja.form.estado"));
                ((JLabel) labels[10]).setText(I18nUtil.get("oveja.form.nacimiento"));
            }
        }
    }
    
}
