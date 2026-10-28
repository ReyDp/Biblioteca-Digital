
package com.ingsoft.biblioteca.repository;

import com.ingsoft.biblioteca.entity.PagoMulta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface PagoMultaRepository
        extends JpaRepository<PagoMulta, Long> {

    @Query("""
        SELECT COALESCE(SUM(p.valorPagado), 0)
        FROM PagoMulta p
        WHERE p.multa.id = :multaId
        """)
    BigDecimal sumarPagosPorMulta(
            @Param("multaId") Long multaId
    );

    List<PagoMulta> findByMultaId(Long multaId);

    boolean existsByMultaIdAndReferencia(Long multaId, String referencia);

    @Query("""
            SELECT COALESCE(SUM(p.valorPagado), 0)
            FROM PagoMulta p
            WHERE p.multa.estado != :estadoExcluido
            """)
    BigDecimal sumarPagosByMultaEstadoNot(@Param("estadoExcluido") com.ingsoft.biblioteca.entity.EstadoMulta estadoExcluido);
}
