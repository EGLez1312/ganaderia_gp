/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Modelo;

import jakarta.persistence.*;
import java.time.LocalDate;

/**
 * Entidad que representa un evento asociado a una oveja del rebaño.
 * Se mapea a la tabla "eventos" con relación Many-to-One a Oveja.
 * 
 * @author Elena González
 * @version 1.0
 */
@Entity
@Table(name = "evento")
public class Evento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "id_oveja", nullable = false)
    private Oveja oveja;

    @Column(name = "tipo_evento", length = 30)
    private String tipoEvento;

    @Column(length = 10, name = "fecha_evento")
    private LocalDate fechaEvento;             

    @Column(length = 200, name = "observaciones")
    private String observaciones;              

    @Column(nullable = false)
    private boolean activo = true;

    // Constructores
    public Evento() {
    }

    /**
     * Constructor para nuevo evento (sin ID).
     */
    public Evento(Oveja oveja, String tipoEvento, LocalDate fechaEvento, String observaciones) {
        this.oveja = oveja;
        this.tipoEvento = tipoEvento;
        this.fechaEvento = fechaEvento;
        this.observaciones = observaciones;
        this.activo = true;
    }

    // Getters y Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Oveja getOveja() {
        return oveja;
    }

    public void setOveja(Oveja oveja) {
        this.oveja = oveja;
    }

    public String getTipoEvento() {
        return tipoEvento;
    }

    public void setTipoEvento(String tipoEvento) {
        this.tipoEvento = tipoEvento;
    }

    public LocalDate getFechaEvento() {
        return fechaEvento;
    } 

    public void setFechaEvento(LocalDate fechaEvento) {
        this.fechaEvento = fechaEvento;
    }

    public String getObservaciones() {
        return observaciones;
    } 

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    @Override
    public String toString() {
        return tipoEvento + " (" + fechaEvento + ")";
    }
}
