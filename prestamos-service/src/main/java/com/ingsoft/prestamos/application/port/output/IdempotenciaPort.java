/*
 * Decompiled with CFR 0.152.
 */
package com.ingsoft.prestamos.application.port.output;

import java.util.Optional;

public interface IdempotenciaPort {
    public Optional<Resultado> buscar(String var1, String var2, String var3);

    public void registrarEnProceso(String var1, String var2, String var3);

    public void completar(String var1, String var2, String var3, String var4);

    public record Resultado(String hashSolicitud, String respuesta, Estado estado) {

        public static enum Estado {
            EN_PROCESO,
            COMPLETADA,
            RECHAZADA;

        }
    }
}

