/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DAO;

import Modelo.Oveja;
import Util.HibernateUtil;
import java.math.BigDecimal;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import java.util.List;
import java.util.stream.Collectors;

/**
 * DAO para operaciones CRUD de ovejas usando Hibernate con soft-delete.
 * Implementa patrón Repository con transacciones seguras, HQL optimizado y
 * métodos para gestión completa del censo (activas/inactivas, KPIs).
 * 
 * @author Elena González
 * @version 1.0
 */
public class OvejaDAO {
    
    /**
     * Obtiene Session Hibernate lazy (maneja Hibernate no inicializado).
     *
     * @return Session activa.
     * @throws IllegalStateException si HibernateUtil falló.
     */
    protected Session getSession() {
        try {
            return HibernateUtil.getSessionFactory().openSession();
        } catch (Exception ex) {
            throw new IllegalStateException("Hibernate no disponible: " + ex.getMessage(), ex);
        }
    }

    /**
     * Lista todas las ovejas activas (activo = true).
     * 
     * @return lista de ovejas activas, vacía si no hay resultados.
     */
    public List<Oveja> listarTodas() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM Oveja WHERE activo = true", Oveja.class).list();
        }
    }

    /**
     * Inserta nueva oveja en base de datos con validación.
     * 
     * @param oveja objeto Oveja a persistir (no null).
     * @throws IllegalArgumentException si oveja es null.
     */
    public void insertar(Oveja oveja) {
        if (oveja == null) throw new IllegalArgumentException("Oveja no puede ser null");
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.persist(oveja);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
            throw new RuntimeException("Error insertando oveja: " + e.getMessage(), e);
        }
    }

    /**
     * Da de baja oveja por ID (soft-delete: activo = false).
     * 
     * @param id ID de la oveja a eliminar lógicamente.
     */
    public void eliminar(int id) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            Oveja oveja = session.get(Oveja.class, id);
            if (oveja != null) {
                oveja.setActivo(false);
                session.merge(oveja);
                tx.commit();
            }
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        }
    }

    /**
     * Actualiza oveja existente sincronizando cambios con BD.
     * 
     * @param oveja objeto Oveja con ID y datos actualizados.
     */
    public void actualizar(Oveja oveja) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.merge(oveja);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        }
    }

    /**
     * Busca oveja activa por número de identificación único.
     * 
     * @param numero número como "OVE001".
     * @return Oveja encontrada o null.
     */
    public Oveja buscarPorNumero(String numero) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Oveja> query = session.createQuery(
                    "FROM Oveja WHERE numeroIdentificacion = :num AND activo = true", Oveja.class);
            query.setParameter("num", numero);
            return query.uniqueResult();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Lista ovejas inactivas (activo = false) para ver histórico.
     * 
     * @return lista de ovejas dadas de baja.
     */
    public List<Oveja> listarInactivas() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM Oveja WHERE activo = false", Oveja.class).list();
        }
    }

    /**
     * Lista según estado: true=activas, false=inactivas.
     * 
     * @param soloActivas filtro por estado activo.
     * @return lista filtrada.
     */
    public List<Oveja> listarSegunEstado(boolean soloActivas) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM Oveja WHERE activo = :estado";
            return session.createQuery(hql, Oveja.class)
                    .setParameter("estado", soloActivas)
                    .list();
        }
    }

    /**
     * Reincorpora oveja inactiva al censo (activo = true).
     * 
     * @param id ID de la oveja a reactivar.
     */
    public void reincorporar(int id) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            Oveja oveja = session.get(Oveja.class, id);
            if (oveja != null) {
                oveja.setActivo(true);
                session.merge(oveja);
                tx.commit();
            }
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        }
    }

    /**
     * Cuenta ovejas totales (activas + inactivas) para KPIs históricos.
     * 
     * @return número total de registros.
     */
    public long contarTotal() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Long resultado = session.createQuery("SELECT COUNT(o) FROM Oveja o", Long.class).uniqueResult();
            return resultado != null ? resultado : 0L;
        }
    }

    /**
     * Cuenta ovejas activas para KPIs del rebaño actual.
     * 
     * @return número de ovejas con activo = true.
     */
    public long contarActivas() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Long resultado = session.createQuery("SELECT COUNT(o) FROM Oveja o WHERE o.activo = true", Long.class).uniqueResult();
            return resultado != null ? resultado : 0L;
        }
    }

    /**
     *
     */
    public List<Oveja> listarHembrasAdultas() {
        // SQL: WHERE sexo='H' AND activo=true AND peso>30 ORDER BY numero
        return listarSegunEstado(true).stream()
                .filter(o -> "H".equals(o.getSexo()) && o.getPesoActual().compareTo(new BigDecimal(30)) > 0)
                .collect(Collectors.toList());
    }

    /**
     * Busca oveja por ID primaria.
     *
     * @param id ID único
     * @return Oveja o null
     */
    public Oveja buscarPorId(Integer id) {
        if (id == null) {
            return null;
        }
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(Oveja.class, id);
        } catch (Exception e) {
            return null;
        }
    }
}
