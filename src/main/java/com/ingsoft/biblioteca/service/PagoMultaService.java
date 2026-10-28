package com.ingsoft.biblioteca.service;

import com.ingsoft.biblioteca.entity.*;
import com.ingsoft.biblioteca.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class PagoMultaService {

    private final MultaRepository multaRepository;
    private final PagoMultaRepository pagoMultaRepository;

    public PagoMultaService(
            MultaRepository multaRepository,
            PagoMultaRepository pagoMultaRepository
    ) {
        this.multaRepository = multaRepository;
        this.pagoMultaRepository = pagoMultaRepository;
    }

    @Transactional
    public PagoMulta registrarPago(
            Long multaId,
            BigDecimal valor,
            String metodo,
            String referencia
    ) {
        if (valor == null || valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "El pago debe ser mayor que cero."
            );
        }

        if (metodo == null || metodo.isBlank()) {
            throw new IllegalArgumentException(
                    "El método de pago es obligatorio."
            );
        }

        Multa multa = multaRepository.buscarPorIdConBloqueo(multaId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "La multa no existe."
                        )
                );

        BigDecimal totalPagado = pagoMultaRepository
                .sumarPagosPorMulta(multaId);

        if (totalPagado == null) {
            totalPagado = BigDecimal.ZERO;
        }

        BigDecimal saldo = multa.getImporteTotal()
                .subtract(totalPagado);

        if (saldo.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException(
                    "Esta multa ya está pagada."
            );
        }

        if (valor.compareTo(saldo) > 0) {
            throw new IllegalArgumentException(
                    "El pago supera el saldo pendiente: " + saldo
            );
        }

        String referenciaNormalizada = referencia == null
                ? null
                : referencia.trim();

        if (referenciaNormalizada != null
                && !referenciaNormalizada.isEmpty()
                && pagoMultaRepository.existsByMultaIdAndReferencia(
                multaId,
                referenciaNormalizada
        )) {
            throw new IllegalArgumentException(
                    "La referencia ya fue registrada para esta multa."
            );
        }

        PagoMulta pago = new PagoMulta();
        pago.setMulta(multa);
        pago.setValorPagado(valor);
        pago.setMetodo(metodo.trim());
        pago.setReferencia(
                referenciaNormalizada == null || referenciaNormalizada.isEmpty()
                        ? null
                        : referenciaNormalizada
        );

        PagoMulta pagoGuardado = pagoMultaRepository.save(pago);

        BigDecimal nuevoTotalPagado = totalPagado.add(valor);

        if (nuevoTotalPagado.compareTo(multa.getImporteTotal()) == 0) {
            multa.setEstado(EstadoMulta.PAGADA);
        } else {
            multa.setEstado(EstadoMulta.PARCIAL);
        }

        multaRepository.save(multa);

        return pagoGuardado;
    }

    @Transactional(readOnly = true)
    public BigDecimal consultarSaldoPendiente(Long multaId) {
        Multa multa = multaRepository.findById(multaId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "La multa no existe."
                        )
                );

        BigDecimal totalPagado = pagoMultaRepository
                .sumarPagosPorMulta(multaId);

        if (totalPagado == null) {
            totalPagado = BigDecimal.ZERO;
        }

        return multa.getImporteTotal().subtract(totalPagado);
    }

    @Transactional(readOnly = true)
    public List<PagoMulta> listarPagos(Long multaId) {
        if (!multaRepository.existsById(multaId)) {
            throw new IllegalArgumentException("La multa no existe.");
        }

        return pagoMultaRepository.findByMultaId(multaId);
    }
}
