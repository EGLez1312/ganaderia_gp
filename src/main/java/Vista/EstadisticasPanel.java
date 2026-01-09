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


/**
 * Panel de estadísticas y KPIs del rebaño de ovejas.
 * 
 * Muestra métricas clave como número total de ovejas, eventos del mes,
 * peso medio del rebaño y distribución por sexo mediante KPIs y gráficos JFreeChart.
 * Incluye exportación a PDF con gráficos como imágenes.
 * 
 * @author Elena González
 * @version 1.0
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
    
    /**
     * Constructor principal del panel de estadísticas.
     * 
     * Inicializa la interfaz gráfica con GridBagLayout, crea título,
     * panel de KPIs, botones de acción y contenedor de gráficos.
     * Llama a {@link #actualizarEstadisticas()} para carga inicial de datos.
     */
    public EstadisticasPanel() {
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
        kpiContainer.setBorder(BorderFactory.createTitledBorder("KPIs Principales"));

        this.kpiLabels = new JLabel[6];
        kpiPanel = new JPanel(new GridLayout(3, 2, 10, 10));

        kpiLabels[0] = new JLabel("Ovejas Totales: --", SwingConstants.CENTER);
        kpiLabels[1] = new JLabel("Eventos Mes: --", SwingConstants.CENTER);
        kpiLabels[2] = new JLabel("Peso Medio: --", SwingConstants.CENTER);
        kpiLabels[3] = new JLabel("Hembras: --", SwingConstants.CENTER);
        kpiLabels[4] = new JLabel("Activas: --", SwingConstants.CENTER);
        kpiLabels[5] = new JLabel("", SwingConstants.CENTER);

        for (JLabel label : kpiLabels) {
            label.setFont(new Font("Arial", Font.BOLD, 14));
            label.setOpaque(true);
            label.setBackground(new Color(100, 150, 255));
            label.setForeground(Color.WHITE);
            label.setBorder(BorderFactory.createLineBorder(Color.BLUE, 2));
            kpiPanel.add(label);
        }
        kpiLabels[5].setVisible(false);      

        // Botón refrescar datos
        JPanel botonesPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));
        JButton btnActualizar = new JButton("🔄 Actualizar");
        btnActualizar.setMnemonic('A');
        btnActualizar.addActionListener(e -> actualizarEstadisticas());
        
        // Botón exportar a PDF
        JButton btnPDF = new JButton("📄 PDF Exportar");
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
        chartPanelContainer.setBorder(BorderFactory.createTitledBorder("Gráficos"));
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
            kpiLabels[0].setText("Ovejas Totales: " + contarTotal()); 
            kpiLabels[1].setText("Eventos Mes: " + calcularEventosMes());
            kpiLabels[2].setText("Peso Medio: " + String.format("%.1f kg", calcularPesoMedio(ovejas)));
            kpiLabels[3].setText("Hembras: " + contarHembras(ovejas));
            kpiLabels[4].setText("Activas: " + contarActivas() + "/" + contarTotal());

            // Actualizar Gráficos
            chartPanelContainer.removeAll();
            chartPanelContainer.add(crearGraficoSexo(ovejas));        
            chartPanelContainer.add(crearGraficoRazas(ovejas));       
            chartPanelContainer.add(crearGraficoActivas());            

            chartPanelContainer.revalidate();
            chartPanelContainer.repaint();
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error cargando estadísticas: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
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

        dataset.setValue("Hembras (" + hembras + ")", hembras);
        dataset.setValue("Machos (" + machos + ")", machos);

        JFreeChart chart = ChartFactory.createPieChart(
            "Distribución por Sexo", dataset, true, true, false);
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
            dataset.addValue(cantidad, "Ovejas", raza);
        });

        JFreeChart chart = ChartFactory.createBarChart(
            "Ovejas por Raza", "Raza", "Cantidad", dataset);
        return new ChartPanel(chart);
    }

    /**
     * Crea gráfico circular (PieChart) de distribución ovejas por estado
     * activo/inactivo.
     *
     * Consulta directamente los métodos {@link #contarActivas()} y
     * {@link #contarTotal()} del DAO para obtener conteos optimizados (COUNT
     * SQL). Las inactivas se calculan como diferencia del total. Colores
     * automáticos JFreeChart: verde(intenso) para Activas, rojo(naranja) para
     * Inactivas.
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
        dataset.setValue("Activas (" + activas + ")", activas);
        dataset.setValue("Inactivas (" + (total - activas) + ")", total - activas);

        return new ChartPanel(ChartFactory.createPieChart("Estado Rebaño", dataset, true, true, false));
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
     * Garantizado: sin cortes, todo visible, layout profesional.
     */
    private void exportarPDF() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Guardar Informe PDF");
        chooser.setSelectedFile(new File("Informe_Rebaño_"
                + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".pdf"));

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
                cs1.setFont(PDType1Font.HELVETICA_BOLD, 15);
                cs1.newLineAtOffset(50, 800);
                cs1.showText("INFORME DE ESTADISTICAS DE LA GANADERÍA GORJÓN-PASCUA");
                cs1.endText();

                // FECHA
                cs1.beginText();
                cs1.setFont(PDType1Font.HELVETICA, 9);
                cs1.newLineAtOffset(50, 785);
                cs1.showText("Fecha: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
                cs1.endText();

                // KPIs
                cs1.beginText();
                cs1.setFont(PDType1Font.HELVETICA_BOLD, 10);
                cs1.newLineAtOffset(50, 770);
                cs1.showText("TOTAL:" + total + " ACT:" + activas + "(" + String.format("%.0f%%", activas * 100.0 / total)
                        + ") H:" + contarHembras(ovejas) + " PESO:" + String.format("%.1fkg", calcularPesoMedio(ovejas))
                        + " EVT:" + calcularEventosMes());
                cs1.endText();

                // TITULO GRAFICO
                cs1.beginText();
                cs1.setFont(PDType1Font.HELVETICA_BOLD, 12);
                cs1.newLineAtOffset(200, 750);
                cs1.showText("DISTRIBUCION POR SEXO");
                cs1.endText();

                // GRAFICO SEXO
                DefaultPieDataset sexoData = new DefaultPieDataset();
                sexoData.setValue("Hembras", contarHembras(ovejas));
                sexoData.setValue("Machos", ovejas.size() - contarHembras(ovejas));
                BufferedImage imgSexo = ChartFactory.createPieChart("Sexo", sexoData, false, false, false)
                        .createBufferedImage(350, 280);
                cs1.drawImage(LosslessFactory.createFromImage(doc, imgSexo), 122, 420, 350, 280);
            }

            // PÁGINA 2: RAZAS
            PDPage page2 = new PDPage(PDRectangle.A4);
            doc.addPage(page2);

            try (PDPageContentStream cs2 = new PDPageContentStream(doc, page2)) {
                // HEADER página 2
                cs2.beginText();
                cs2.setFont(PDType1Font.HELVETICA_BOLD, 15);
                cs2.newLineAtOffset(50, 800);
                cs2.showText("OVEJAS POR RAZA");
                cs2.endText();

                // TITULO GRAFICO
                cs2.beginText();
                cs2.setFont(PDType1Font.HELVETICA_BOLD, 12);
                cs2.newLineAtOffset(200, 750);
                cs2.showText("DISTRIBUCION POR RAZA");
                cs2.endText();

                // GRAFICO RAZAS
                DefaultCategoryDataset razasData = new DefaultCategoryDataset();
                ovejas.stream().collect(Collectors.groupingBy(Oveja::getRaza, Collectors.counting()))
                        .forEach((r, c) -> razasData.addValue(c, "N", r));
                BufferedImage imgRazas = ChartFactory.createBarChart("Razas", "Raza", "N", razasData,
                        PlotOrientation.VERTICAL, false, false, false)
                        .createBufferedImage(350, 280);
                cs2.drawImage(LosslessFactory.createFromImage(doc, imgRazas), 122, 420, 350, 280);
            }

            // PÁGINA 3: ESTADO
            PDPage page3 = new PDPage(PDRectangle.A4);
            doc.addPage(page3);

            try (PDPageContentStream cs3 = new PDPageContentStream(doc, page3)) {
                // HEADER página 3
                cs3.beginText();
                cs3.setFont(PDType1Font.HELVETICA_BOLD, 15);
                cs3.newLineAtOffset(50, 800);
                cs3.showText("ESTADO DEL REBAÑO");
                cs3.endText();

                // KPIs ESTADO
                cs3.beginText();
                cs3.setFont(PDType1Font.HELVETICA_BOLD, 11);
                cs3.newLineAtOffset(50, 780);
                cs3.showText("ACTIVAS:" + activas + " (" + String.format("%.0f%%", activas * 100.0 / total)
                        + ") | INACTIVAS:" + (total - activas) + " ("
                        + String.format("%.0f%%", (total - activas) * 100.0 / total) + ")");
                cs3.endText();

                // TITULO GRAFICO
                cs3.beginText();
                cs3.setFont(PDType1Font.HELVETICA_BOLD, 12);
                cs3.newLineAtOffset(180, 750);
                cs3.showText("ACTIVAS vs INACTIVAS");
                cs3.endText();

                // GRAFICO ESTADO
                DefaultPieDataset estadoData = new DefaultPieDataset();
                estadoData.setValue("Activas", activas);
                estadoData.setValue("Inactivas", total - activas);
                BufferedImage imgEstado = ChartFactory.createPieChart("Estado", estadoData, false, false, false)
                        .createBufferedImage(400, 300);
                cs3.drawImage(LosslessFactory.createFromImage(doc, imgEstado), 98, 380, 400, 300);
            }

            // GUARDAR
            doc.save(ruta);
            JOptionPane.showMessageDialog(this, "PDF 3 paginas generado perfectamente:\n" + ruta);

        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error escritura PDF:\n" + e.getMessage());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

}
