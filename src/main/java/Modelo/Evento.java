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
@Table(name = "eventos")
public class Evento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "id_oveja", nullable = false)
    private Oveja oveja;

    @Column(name = "tipo_evento", length = 30)
    private String tipoEvento;

    @Column(length = 10)
    private LocalDate fecha;

    @Column(length = 200)
    private String detalles;

    // Constructores
    public Evento() {}

    /**
     * Constructor para nuevo evento (sin ID).
     */
    public Evento(Oveja oveja, String tipoEvento, LocalDate fecha, String detalles) {
        this.oveja = oveja;
        this.tipoEvento = tipoEvento;
        this.fecha = fecha;
        this.detalles = detalles;
    }

    // Getters y Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Oveja getOveja() { return oveja; }
    public void setOveja(Oveja oveja) { this.oveja = oveja; }

    public String getTipoEvento() { return tipoEvento; }
    public void setTipoEvento(String tipoEvento) { this.tipoEvento = tipoEvento; }

    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }

    public String getDetalles() { return detalles; }
    public void setDetalles(String detalles) { this.detalles = detalles; }

    @Override
    public String toString() {
        return tipoEvento + " (" + fecha + ")";
    }
}