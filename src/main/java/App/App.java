/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package App;

import Vista.LoginFrame;
import javax.swing.UIManager;

/**
 * Punto de entrada de la aplicación de gestión ganadera.
 * 
 * @author Elena González
 * @version 1.0
 */
public class App {

    public static void main(String[] args) {
        try {
            // Establece el aspecto nativo de Windows
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        javax.swing.SwingUtilities.invokeLater(() -> {
            new LoginFrame().setVisible(true);
        });
    }
}   