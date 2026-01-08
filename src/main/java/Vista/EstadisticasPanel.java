/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Vista;

import DAO.OvejaDAO;
import DAO.EventoDAO;
import DAO.UsuarioDAO;
import Modelo.Oveja;
import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Panel de estadísticas y KPIs del rebaño.
 * Muestra métricas clave como número de ovejas, eventos, peso medio y distribución por sexo.
 */
public class EstadisticasPanel extends JPanel {
    
    /** Panel de KPIs principales */
    private JPanel kpiPanel;
    
    /**
     * Constructor principal del panel de estadísticas.
     * Inicializa la interfaz y carga datos iniciales.
     */
    public EstadisticasPanel() {
        initComponents();
        actualizarEstadisticas();
    }

    /**
     * Inicializa los componentes Swing usando GridBagLayout.
     * Crea título, panel de KPIs y botón de actualización.
     */
    private void initComponents() {
        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(15, 15, 15, 15);
        c.fill = GridBagConstraints.BOTH;

        // Título principal
        c.gridx = 0; c.gridy = 0; c.gridwidth = 2;
        JLabel lblTitulo = new JLabel("Estadísticas del Rebaño", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 22));
        add(lblTitulo, c);

        // Panel de KPIs (2x2 grid)
        kpiPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        kpiPanel.setBorder(BorderFactory.createTitledBorder("KPIs Principales"));
        
        String[] kpis = {"Ovejas Totales", "Eventos Mes", "Peso Medio", "Hembras"};
        for (String kpi : kpis) {
            JLabel lblKpi = new JLabel(kpi + ": --", SwingConstants.CENTER);
            lblKpi.setFont(new Font("Arial", Font.BOLD, 16));
            lblKpi.setOpaque(true);
            lblKpi.setBackground(new Color(100, 150, 255));
            lblKpi.setForeground(Color.WHITE);
            lblKpi.setBorder(BorderFactory.createLineBorder(Color.BLUE));
            kpiPanel.add(lblKpi);
        }
        
        c.gridy = 1; c.gridwidth = 1;
        add(kpiPanel, c);

        // Botón refrescar
        c.gridx = 1;
        JButton btnRefrescar = new JButton("🔄 Actualizar");
        btnRefrescar.setMnemonic('A');
        btnRefrescar.addActionListener(e -> actualizarEstadisticas());
        add(btnRefrescar, c);
    }

    /**
     * Actualiza todos los KPIs con datos reales de la base de datos.
     * Carga ovejas y recalcula métricas en tiempo real.
     */
    private void actualizarEstadisticas() {
        try {
            OvejaDAO ovejaDAO = new OvejaDAO();
            EventoDAO eventoDAO = new EventoDAO();
            List<Oveja> ovejas = ovejaDAO.listarTodas();
            
            // Actualizar KPIs usando acceso directo a componentes
            Component[] kpiLabels = kpiPanel.getComponents();
            ((JLabel) kpiLabels[0]).setText("Ovejas Totales: " + ovejas.size());
            ((JLabel) kpiLabels[1]).setText("Eventos Mes: 12"); // TODO: Implementar cálculo real
            ((JLabel) kpiLabels[2]).setText("Peso Medio: " + calcularPesoMedio(ovejas) + "kg");
            ((JLabel) kpiLabels[3]).setText("Hembras: " + contarHembras(ovejas));
            
            revalidate();
            repaint();
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al cargar estadísticas: " + e.getMessage());
        }
    }

    /**
     * Calcula el peso medio de todas las ovejas del rebaño.
     * 
     * @param ovejas lista completa de ovejas
     * @return peso medio en kg (0 si no hay ovejas)
     */
    private double calcularPesoMedio(List<Oveja> ovejas) {
        return ovejas.stream()
                .mapToDouble(o -> o.getPesoActual().doubleValue())
                .average().orElse(0);
    }

    /**
     * Cuenta el número de hembras en el rebaño.
     * 
     * @param ovejas lista completa de ovejas
     * @return número de ovejas hembras (sexo = "M")
     */
    private long contarHembras(List<Oveja> ovejas) {
        return ovejas.stream().filter(o -> "M".equals(o.getSexo())).count();
    }
}

