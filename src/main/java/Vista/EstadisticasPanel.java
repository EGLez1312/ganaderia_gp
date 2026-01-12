/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Vista;

import DAO.OvejaDAO;
import DAO.EventoDAO;
import Modelo.Oveja;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtils;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
import java.time.format.DateTimeFormatter;
import org.jfree.chart.plot.PlotOrientation;
import Util.I18nUtil;
import java.util.Locale;
import java.util.ResourceBundle;
import java.text.MessageFormat;


/**
 * Panel de estadísticas y KPIs del rebaño de ovejas.
 * 
 * Muestra métricas clave como número total de ovejas, eventos del mes,
 * peso medio del rebaño y distribución por sexo mediante KPIs y gráficos JFreeChart.
 * Incluye exportación a PDF con gráficos como imágenes.
 * 
 * @author Elena González
 * @version 2.0
 * @see OvejaDAO
 * @see EventoDAO
 */
public class EstadisticasPanel extends JPanel {
    
    /**
     * Contenedor principal para los paneles de gráficos JFreeChart.
     */
    private JPanel chartPanelContainer;
    
    /**
     * DAO para acceso a datos de ovejas.
     */
    private final OvejaDAO ovejaDAO = new OvejaDAO();
    
    /**
     * Panel que contiene los 4 KPIs principales en layout 2x2.
     */
    private JPanel kpiPanel;
    
    /** 
     * Labels de KPIs para actualización dinámica.      
     */
    private JLabel[] kpiLabels;
    
    private ResourceBundle bundle;
    
    /**
     * Constructor principal del panel de estadísticas.
     * 
     * Inicializa la interfaz gráfica con GridBagLayout, crea título,
     * panel de KPIs, botones de acción y contenedor de gráficos.
     * Llama a {@link #actualizarEstadisticas()} para carga inicial de datos.
     */
    public EstadisticasPanel() {
        bundle = ResourceBundle.getBundle("messages", Locale.forLanguageTag("es"));
        initComponents();
        actualizarEstadisticas();
    }

    /**
     * Inicializa todos los componentes Swing del panel.
     * 
     * Crea y configura:
     * -Título principal centrado.
     * -Panel de 4 KPIs con GridLayout 2x2.
     * -Botones "Actualizar" y "PDF Exportar" con mnemonics.
     * -Contenedor para dos gráficos (sexo y razas).
     * Usa GridBagLayout para responsive design.
     */
    private void initComponents() {
        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(50, 50, 50, 50);
        c.fill = GridBagConstraints.BOTH;

        // Título principal
        c.gridy = 1; c.gridwidth = 2;
        JPanel kpiContainer = new JPanel(new BorderLayout(10, 10));
        kpiContainer.setBorder(BorderFactory.createTitledBorder(I18nUtil.get("estadistica.border.kpis")));

        this.kpiLabels = new JLabel[6];
        kpiPanel = new JPanel(new GridLayout(3, 2, 10, 10));

        kpiLabels[0] = new JLabel(I18nUtil.get("estadistica.kpi.total", "--"), SwingConstants.CENTER);
        kpiLabels[1] = new JLabel(String.format(I18nUtil.get("estadistica.kpi.eventos"), "--"), SwingConstants.CENTER);
        kpiLabels[2] = new JLabel(String.format(I18nUtil.get("estadistica.kpi.peso"), "--"), SwingConstants.CENTER);
        kpiLabels[3] = new JLabel(String.format(I18nUtil.get("estadistica.kpi.hembras"), "--"), SwingConstants.CENTER);
        kpiLabels[4] = new JLabel(String.format(I18nUtil.get("estadistica.kpi.activas"), "--", "--"), SwingConstants.CENTER);
        kpiLabels[5] = new JLabel("", SwingConstants.CENTER);

        for (JLabel label : kpiLabels) {
            label.setFont(new Font("Segoe UI", Font.BOLD, 14));
            label.setOpaque(true);
            label.setBackground(new Color(100, 150, 255));
            label.setForeground(Color.WHITE);
            label.setBorder(BorderFactory.createLineBorder(Color.BLUE, 2));
            kpiPanel.add(label);
        }
        kpiLabels[5].setVisible(false);      

        // Botón refrescar datos
        JPanel botonesPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));
        JButton btnActualizar = new JButton(I18nUtil.get("estadistica.btn.actualizar"));
        btnActualizar.setMnemonic('A');
        btnActualizar.addActionListener(e -> actualizarEstadisticas());
        
        // Botón exportar a PDF
        JButton btnPDF = new JButton(I18nUtil.get("estadistica.btn.pdf"));
        btnPDF.setMnemonic('P');
        btnPDF.addActionListener(e -> exportarPDF());

        botonesPanel.add(btnActualizar);
        botonesPanel.add(btnPDF);

        kpiContainer.add(kpiPanel, BorderLayout.CENTER);
        kpiContainer.add(botonesPanel, BorderLayout.SOUTH);
        add(kpiContainer, c);
        
        // Contenedor de gráficos
        c.gridy = 2; c.gridwidth = 2; c.weightx = 1.0; c.weighty = 1.0;
        chartPanelContainer = new JPanel(new GridLayout(1, 3, 10, 10));  
        chartPanelContainer.setBorder(BorderFactory.createTitledBorder(I18nUtil.get("estadistica.border.graficos")));
        add(chartPanelContainer, c);
    }

    /**
     * Actualiza todos los KPIs y gráficos con datos frescos de la BD.
     * 
     * - Carga lista completa de ovejas.
     * - Actualiza los 4 labels de KPIs.
     * - Regenera y reemplaza gráficos de sexo y razas.
     * - Refresca UI con revalidate/repaint.
     */
    private void actualizarEstadisticas() {
        try {
            List<Oveja> ovejas = ovejaDAO.listarTodas();
           
            // Actualizar KPIs usando acceso directo a componentes
            MessageFormat fmtTotal = new MessageFormat(I18nUtil.get("estadistica.kpi.total"));
            kpiLabels[0].setText(fmtTotal.format(new Object[]{contarTotal()}));
            
            MessageFormat fmtEventos = new MessageFormat(I18nUtil.get("estadistica.kpi.eventos"));
            kpiLabels[1].setText(fmtEventos.format(new Object[]{calcularEventosMes()}));

            MessageFormat fmtPeso = new MessageFormat(I18nUtil.get("estadistica.kpi.peso"));
            kpiLabels[2].setText(fmtPeso.format(new Object[]{calcularPesoMedio(ovejas)}));

            MessageFormat fmtHembras = new MessageFormat(I18nUtil.get("estadistica.kpi.hembras"));
            kpiLabels[3].setText(fmtHembras.format(new Object[]{contarHembras(ovejas)}));

            MessageFormat fmtActivas = new MessageFormat(I18nUtil.get("estadistica.kpi.activas"));
            kpiLabels[4].setText(fmtActivas.format(new Object[]{contarActivas(), contarTotal()}));

            // Actualizar Gráficos
            chartPanelContainer.removeAll();
            chartPanelContainer.add(crearGraficoSexo(ovejas));        
            chartPanelContainer.add(crearGraficoRazas(ovejas));       
            chartPanelContainer.add(crearGraficoActivas());            

            chartPanelContainer.revalidate();
            chartPanelContainer.repaint();
            
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    String.format(I18nUtil.get("estadistica.stats.error"), e.getMessage()),
                    I18nUtil.get("estadistica.stats.error"), JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Calcula el peso medio actual de todas las ovejas del rebaño.
     * 
     * @param ovejas lista completa de ovejas cargadas de BD
     * @return peso medio en kg (0.0 si lista vacía o error)
     */
    private double calcularPesoMedio(List<Oveja> ovejas) {
        return ovejas.stream()
                .mapToDouble(o -> o.getPesoActual().doubleValue())
                .average().orElse(0.0);
    }

    /**
     * Cuenta el número total de ovejas hembras en el rebaño.
     * 
     * @param ovejas lista completa de ovejas
     * @return cantidad de ovejas con sexo "H"
     */
    private long contarHembras(List<Oveja> ovejas) {
        return ovejas.stream().filter(o -> "H".equals(o.getSexo())).count();
    }

    /**
     * Crea gráfico circular (PieChart) de distribución por sexo.
     * 
     * @param ovejas lista de ovejas para contar hembras/machos
     * @return ChartPanel listo para añadir al contenedor
     */
    private ChartPanel crearGraficoSexo(List<Oveja> ovejas) {
        DefaultPieDataset dataset = new DefaultPieDataset();
        long hembras = ovejas.stream().filter(o -> "H".equals(o.getSexo())).count();
        long machos = ovejas.size() - hembras;

        dataset.setValue(I18nUtil.get("estadistica.grafico.sexo.hembras") + " (" + hembras + ")", hembras);
        dataset.setValue(I18nUtil.get("estadistica.grafico.sexo.machos") + " (" + machos + ")", machos);

        JFreeChart chart = ChartFactory.createPieChart(I18nUtil.get("estadistica.grafico.sexo.title"), 
                dataset, true, true, false);
        return new ChartPanel(chart);
    }

    /**
     * Crea gráfico de barras de ovejas agrupadas por raza.
     * 
     * @param ovejas lista de ovejas para agrupar por raza
     * @return ChartPanel listo para añadir al contenedor
     */
    private ChartPanel crearGraficoRazas(List<Oveja> ovejas) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        // Agrupamos por raza y contamos
        Map<String, Long> conteoRazas = ovejas.stream()
                .collect(Collectors.groupingBy(Oveja::getRaza, Collectors.counting()));

        conteoRazas.forEach((raza, cantidad) -> {
            dataset.addValue(cantidad, I18nUtil.get("estadistica.grafico.razas.ovejas"), raza);
        });

        JFreeChart chart = ChartFactory.createBarChart(
                I18nUtil.get("estadistica.grafico.razas.title"),
                I18nUtil.get("estadistica.grafico.razas.raza"),
                I18nUtil.get("estadistica.grafico.razas.cantidad"),
                dataset);
        return new ChartPanel(chart);
    }

    /**
     * Crea gráfico circular (PieChart) de distribución ovejas por estado
     * activo/inactivo.
     *
     * Consulta directamente los métodos {@link #contarActivas()} y
     * {@link #contarTotal()} del DAO para obtener conteos optimizados (COUNT
     * SQL). Las inactivas se calculan como diferencia del total. Colores
     * automáticos JFreeChart.
     *
     * @return ChartPanel listo para añadir a contenedor Swing con resize
     * automático
     * @see OvejaDAO#contarActivas()
     * @see OvejaDAO#contarTotal()
     * @see DefaultPieDataset
     * @see ChartFactory#createPieChart(String, PieDataset, boolean, boolean,
     * boolean)
     */
    private ChartPanel crearGraficoActivas() {  
        DefaultPieDataset dataset = new DefaultPieDataset();
        long activas = contarActivas();
        long total = contarTotal();
        dataset.setValue(I18nUtil.get("estadistica.grafico.activas.label.activas") + " (" + activas + ")", activas);
        dataset.setValue(I18nUtil.get("estadistica.grafico.activas.label.inactivas") + " (" + (total - activas) + ")", total - activas);

        return new ChartPanel(ChartFactory.createPieChart(I18nUtil.get("estadistica.grafico.activas.title"), 
                dataset, true, true, false));
    }

    /**
     * Cuenta eventos del mes actual (filtrado por fecha).
     * 
     * @return número de eventos en el mes/año actual (0 si error)
     */
    private long calcularEventosMes() {
        try {
            EventoDAO dao = new EventoDAO();
            LocalDate ahora = LocalDate.now();
            int mesActual = ahora.getMonthValue();
            int anioActual = ahora.getYear();

            return dao.listarTodos().stream()
                    .filter(e -> e.getFechaEvento().getMonthValue() == mesActual
                        && e.getFechaEvento().getYear() == anioActual)
                    .count();
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Retorna conteo activas directo de BD (rápido para KPIs).
     */
    private long contarActivas() {
        return ovejaDAO.contarActivas(); 
    }

    /**
     * Retorna total ovejas (activas + inactivas) de BD.
     */
    private long contarTotal() {
        return ovejaDAO.contarTotal();
    }

    /**
     * Exporta estadísticas a PDF 3 páginas (apilado vertical - todos visibles).
     *
     * PÁGINA 1: Header, KPIs, Grafico SEXO (centrado) PÁGINA 2: Grafico RAZAS
     * (centrado) PÁGINA 3: Grafico ESTADO (centrado)
     *
     */
    private void exportarPDF() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle(I18nUtil.get("estadistica.chooser.title"));
        chooser.setSelectedFile(new File(String.format(I18nUtil.get("estadistica.chooser.filename"),
                LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")))));

        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        String ruta = chooser.getSelectedFile().getAbsolutePath();
        if (!ruta.toLowerCase().endsWith(".pdf")) {
            ruta += ".pdf";
        }

        try (PDDocument doc = new PDDocument()) {
            List<Oveja> ovejas = ovejaDAO.listarTodas();
            long total = contarTotal();
            long activas = contarActivas();

            // PÁGINA 1: KPIs + SEXO
            PDPage page1 = new PDPage(PDRectangle.A4);
            doc.addPage(page1);
            try (PDPageContentStream cs1 = new PDPageContentStream(doc, page1)) {
                // HEADER
                cs1.beginText();
                cs1.setFont(PDType1Font.HELVETICA_BOLD, 16);
                cs1.newLineAtOffset(50, 780);
                cs1.showText(I18nUtil.get("estadistica.pdf.header1"));
                cs1.endText();

                // FECHA
                cs1.beginText();
                cs1.setFont(PDType1Font.HELVETICA, 10);
                cs1.newLineAtOffset(50, 760);
                cs1.showText(I18nUtil.get("estadistica.pdf.fecha") + ": "
                        + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
                cs1.endText(); 

                // KPIs
                cs1.beginText();
                cs1.setFont(PDType1Font.HELVETICA_BOLD, 11);
                cs1.newLineAtOffset(50, 740);
                cs1.showText(I18nUtil.get("estadistica.pdf.kpis"));
                cs1.endText(); 

                // TÍTULO GRÁFICO
                cs1.beginText();
                cs1.setFont(PDType1Font.HELVETICA_BOLD, 14);
                cs1.newLineAtOffset(200, 680);
                cs1.showText(I18nUtil.get("estadistica.grafico.sexo.title"));
                cs1.endText();

                // GRÁFICO SEXO
                DefaultPieDataset sexoData = new DefaultPieDataset();
                sexoData.setValue(I18nUtil.get("estadistica.grafico.sexo.hembras"), contarHembras(ovejas));
                sexoData.setValue(I18nUtil.get("estadistica.grafico.sexo.machos"), total - contarHembras(ovejas));
                BufferedImage imgSexo = ChartFactory.createPieChart(I18nUtil.get("estadistica.grafico.sexo.title"), 
                        sexoData, false, false, false)
                        .createBufferedImage(380, 300);
                cs1.drawImage(LosslessFactory.createFromImage(doc, imgSexo), 100, 350, 380, 300);
            }

            // PÁGINA 2: RAZAS 
            PDPage page2 = new PDPage(PDRectangle.A4);
            doc.addPage(page2);
            try (PDPageContentStream cs2 = new PDPageContentStream(doc, page2)) {
                cs2.beginText();
                cs2.setFont(PDType1Font.HELVETICA_BOLD, 16);
                cs2.newLineAtOffset(50, 780);
                cs2.showText(I18nUtil.get("estadistica.pdf.header2"));
                cs2.endText();

                cs2.beginText();
                cs2.setFont(PDType1Font.HELVETICA_BOLD, 14);
                cs2.newLineAtOffset(200, 680);
                cs2.showText(I18nUtil.get("estadistica.grafico.razas.title"));
                cs2.endText();

                DefaultCategoryDataset razasData = new DefaultCategoryDataset();
                ovejas.stream().collect(Collectors.groupingBy(Oveja::getRaza, Collectors.counting()))
                        .forEach((r, c) -> razasData.addValue(c, I18nUtil.get("estadistica.grafico.razas.ovejas"), r));
                BufferedImage imgRazas = ChartFactory.createBarChart(I18nUtil.get("estadistica.grafico.razas.title"),
                        I18nUtil.get("estadistica.grafico.razas.raza"),
                        I18nUtil.get("estadistica.grafico.razas.cantidad"),
                        razasData,
                        PlotOrientation.VERTICAL, false, false, false).createBufferedImage(380, 300);
                cs2.drawImage(LosslessFactory.createFromImage(doc, imgRazas), 100, 350, 380, 300);
            }

            // PÁGINA 3: ESTADO
            PDPage page3 = new PDPage(PDRectangle.A4);
            doc.addPage(page3);
            try (PDPageContentStream cs3 = new PDPageContentStream(doc, page3)) {
                cs3.beginText();
                cs3.setFont(PDType1Font.HELVETICA_BOLD, 16);
                cs3.newLineAtOffset(50, 780);
                cs3.showText(I18nUtil.get("estadistica.pdf.header3"));
                cs3.endText();

                // KPIs ESTADO
                cs3.beginText();
                cs3.setFont(PDType1Font.HELVETICA_BOLD, 12);
                cs3.newLineAtOffset(50, 740);
                double pctAct = total > 0 ? activas * 100.0 / total : 0;
                cs3.showText(I18nUtil.get("estadistica.pdf.estado.kpis"));
                cs3.endText();

                cs3.beginText();
                cs3.setFont(PDType1Font.HELVETICA_BOLD, 14);
                cs3.newLineAtOffset(200, 680);
                cs3.showText(I18nUtil.get("estadistica.grafico.activas.title"));
                cs3.endText();

                DefaultPieDataset estadoData = new DefaultPieDataset();
                estadoData.setValue(I18nUtil.get("estadistica.grafico.activas.label.activas"), activas);
                estadoData.setValue(I18nUtil.get("estadistica.grafico.activas.label.inactivas"), total - activas);
                BufferedImage imgEstado = ChartFactory.createPieChart((I18nUtil.get("estadistica.grafico.activas.title")), estadoData, false, false, false)
                        .createBufferedImage(380, 300);
                cs3.drawImage(LosslessFactory.createFromImage(doc, imgEstado), 100, 350, 380, 300);
            }

            // GUARDAR
            doc.save(ruta);
            JOptionPane.showMessageDialog(this, String.format(I18nUtil.get("estadistica.pdf.success"), ruta));

        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, String.format(I18nUtil.get("estadistica.pdf.error.io"), e.getMessage()));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, String.format(I18nUtil.get("estadistica.stats.error"), e.getMessage()));
        }
    }
    
    /**
     * Actualiza TODOS los textos i18n del panel según locale actual. 
     *
     * @see PrincipalFrame#updateAllTexts()
     */
    protected void updateAllTexts() {
        // Título KPIs border (getComponent(0) = kpiContainer)
        if (getComponentCount() > 0) {
            Component kpiContainer = getComponent(0);
            if (kpiContainer instanceof JPanel) {
                JPanel containerPanel = (JPanel) kpiContainer;
                if (containerPanel.getComponentCount() > 0) {
                    ((JPanel) containerPanel.getComponent(0)).setBorder(
                            BorderFactory.createTitledBorder(I18nUtil.get("estadistica.border.kpis"))
                    );
                }
            }
        }

        // Botones PDF/Actualizar (botonesPanel → índices 1,2)
        if (getComponentCount() > 0) {
            Component kpiContainer = getComponent(0);
            if (kpiContainer instanceof JPanel) {
                JPanel containerPanel = (JPanel) kpiContainer;
                if (containerPanel.getComponentCount() > 1) {
                    Component botonesPanel = containerPanel.getComponent(1);
                    if (botonesPanel instanceof JPanel) {
                        Component[] buttons = ((JPanel) botonesPanel).getComponents();
                        if (buttons.length >= 2) {
                            ((JButton) buttons[0]).setText(I18nUtil.get("estadistica.btn.actualizar"));
                            ((JButton) buttons[1]).setText(I18nUtil.get("estadistica.btn.pdf"));
                        }
                    }
                }
            }
        }

        // Gráficos border (getComponent(1))
        if (getComponentCount() > 1) {
            chartPanelContainer.setBorder(
                    BorderFactory.createTitledBorder(I18nUtil.get("estadistica.border.graficos"))
            );
        }

        actualizarEstadisticas();  
        
        revalidate();
        repaint();
    }

}
