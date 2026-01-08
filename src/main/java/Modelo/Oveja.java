/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Modelo;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Entidad que representa una oveja del rebaño en la base de datos.
 * Se mapea a la tabla "ovejas" y está relacionada con la tabla "eventos".
 * 
 * @author Elena González
 * @version 1.0
 */
@Entity
@Table(name = "oveja")
public class Oveja {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(unique = true, nullable = false, length = 20)
    private String numeroIdentificacion;

    @Column(length = 50)
    private String raza;

    @Column(name = "fecha_nacimiento")
    private LocalDate fechaNacimiento;

    @Column(length = 1, nullable = false)
    private String sexo;  // 'H' o 'M'

    @Column(name = "peso_actual", columnDefinition = "DECIMAL(5,2)")
    private BigDecimal pesoActual;

    @Column(length = 50)
    private String estadoSalud;
    
    @Column(nullable = false)
    private boolean activo = true;

    // Constructores
    public Oveja() {}

    /**
     * Constructor para nueva oveja (sin ID).
     */
    public Oveja(String numeroIdentificacion, String raza, LocalDate fechaNacimiento,
                 String sexo, BigDecimal pesoActual, String estadoSalud) {
        this.numeroIdentificacion = numeroIdentificacion;
        this.raza = raza;
        this.fechaNacimiento = fechaNacimiento;
        this.sexo = sexo;
        this.pesoActual = pesoActual;
        this.estadoSalud = estadoSalud;
        this.activo = true;
    }

    // Getters y Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getNumeroIdentificacion() { return numeroIdentificacion; }
    public void setNumeroIdentificacion(String numeroIdentificacion) { this.numeroIdentificacion = numeroIdentificacion; }

    public String getRaza() { return raza; }
    public void setRaza(String raza) { this.raza = raza; }

    public LocalDate getFechaNacimiento() { return fechaNacimiento; }
    public void setFechaNacimiento(LocalDate fechaNacimiento) { this.fechaNacimiento = fechaNacimiento; }

    public String getSexo() { return sexo; }
    public void setSexo(String sexo) { this.sexo = sexo; }

    public BigDecimal getPesoActual() { return pesoActual; }
    public void setPesoActual(BigDecimal pesoActual) { this.pesoActual = pesoActual; }

    public String getEstadoSalud() { return estadoSalud; }
    public void setEstadoSalud(String estadoSalud) { this.estadoSalud = estadoSalud; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo;  }

    @Override
    public String toString() {
        return numeroIdentificacion + " - " + raza + " (" + sexo + ")";
    }
}