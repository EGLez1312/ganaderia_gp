/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DAO;

import Modelo.Oveja;
import Util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import java.util.List;

/**
 * DAO para operaciones CRUD de ovejas usando Hibernate + soft-delete.
 * Implementa patrón Repository con transacciones seguras y HQL optimizado.
 * Soporta filtrado activo/inactivo y reincorporación al censo.
 * 
 * @author Sistema de Gestión Ganadera
 */
public class OvejaDAO {

    /**
     * Lista todas las ovejas activas (activo=true).
     * 
     * @return Lista de ovejas activas, vacía si no hay resultados
     */
    public List<Oveja> listarTodas() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM Oveja WHERE activo = true", Oveja.class).list();
        }
    }

    /**
     * Inserta nueva oveja en base de datos.
     * Validación null + transacción con rollback automático en errores.
     * 
     * @param oveja objeto Oveja a persistir (no null)
     * @throws IllegalArgumentException si oveja es null
     * @throws RuntimeException si falla persistencia
     */
    public void insertar(Oveja oveja) {
        if (oveja == null) throw new IllegalArgumentException("Oveja no puede ser null");
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            if (!session.getTransaction().isActive()) {
                Transaction tx = session.beginTransaction();
                try {
                    session.persist(oveja);
                    tx.commit();
                } catch (Exception e) {
                    if (tx != null) tx.rollback();
                    throw e;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error insertando oveja: " + e.getMessage(), e);
        }
    }

    /**
     * Da de baja oveja por ID (soft-delete: activo=false).
     * No elimina físicamente, solo marca como inactiva para auditoría.
     * 
     * @param id ID primario de la oveja a dar de baja
     * @throws RuntimeException si falla la operación
     */
    public void eliminar(int id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            try {
                Oveja oveja = session.get(Oveja.class, id);
                if (oveja != null) {
                    oveja.setActivo(false); 
                    session.merge(oveja);  
                    session.flush();
                    tx.commit();      
                    System.out.println("DEBUG: Commit realizado para ID " + id);
                }
            } catch (Exception e) {
                if (tx != null) tx.rollback();
                throw e;
            }
        }
    }

    /**
     * Actualiza oveja existente usando merge().
     * Sincroniza cambios con base de datos en transacción.
     * 
     * @param oveja objeto Oveja con ID y campos actualizados
     */
    public void actualizar(Oveja oveja) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            session.merge(oveja);
            tx.commit();
        }
    }

    /**
     * Busca oveja activa por número de identificación único.
     * 
     * @param numero número de identificación (ej: "OVE001")
     * @return Oveja encontrada o null si no existe/activa
     */
    public Oveja buscarPorNumero(String numero) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                    "FROM Oveja WHERE numeroIdentificacion = :num AND activo = true",
                    Oveja.class)
                    .setParameter("num", numero)
                    .uniqueResult();
        } catch (Exception e) {
            System.err.println("Error al buscar oveja por número: " + e.getMessage());
            return null;
        }
    }
  
    /**
     * Lista todas las ovejas inactivas (activo=false).
     * Útil para reincorporación o reportes históricos.
     * 
     * @return Lista de ovejas dadas de baja
     */
    public List<Oveja> listarInactivas() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM Oveja WHERE activo = false", Oveja.class).list();
        }
    }

    /**
     * Lista ovejas según estado activo/inactivo.
     * Flexible para OvejaPanel: true=activas, false=bajas.
     * 
     * @param mostrarSoloActivas true para activas, false para inactivas
     * @return Lista filtrada por estado activo
     */
    public List<Oveja> listarSegunEstado(boolean mostrarSoloActivas) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM Oveja WHERE activo = :estado";
            return session.createQuery(hql, Oveja.class)
                    .setParameter("estado", mostrarSoloActivas)
                    .list();
        }
    }

    /**
     * Reincorpora oveja inactiva al censo (activo=true).
     * Útil desde OvejaPanel cuando chkMostrarBajas=true.
     * 
     * @param id ID de la oveja a reactivar
     * @throws RuntimeException si falla la operación o ID no existe
     */
    public void reincorporar(int id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            try {
                Oveja oveja = session.get(Oveja.class, id);
                if (oveja != null) {
                    oveja.setActivo(true); // Cambiamos de 0 a 1
                    session.merge(oveja);
                    tx.commit();
                    System.out.println("DEBUG: Oveja ID " + id + " reincorporada al censo activo.");
                }
            } catch (Exception e) {
                if (tx != null) {
                    tx.rollback();
                }
                throw e;
            }
        }
    }

    /**
     * Cuenta TOTAL ovejas en BD (activas + inactivas). Para KPIs de
     * estadísticas completas del rebaño histórico.
     *
     * @return número total de registros Oveja
     */
    public long contarTotal() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Long resultado = session.createQuery("SELECT COUNT(o) FROM Oveja o", Long.class)
                    .uniqueResult();
            return resultado != null ? resultado : 0L;
        }
    }

    /**
     * Cuenta ovejas ACTIVAS (activo=true) en BD. Optimizado para KPIs - directo
     * COUNT sin cargar entidades.
     *
     * @return número de ovejas con activo=true
     */
    public long contarActivas() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Long resultado = session.createQuery("SELECT COUNT(o) FROM Oveja o WHERE o.activo = true", Long.class)
                    .uniqueResult();
            return resultado != null ? resultado : 0L;
        }
    }

}
