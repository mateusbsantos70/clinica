package br.com.essenciaviva.clinic.util;

import java.time.LocalDateTime;
import java.time.ZoneId;

public final class Tempo {

    public static final ZoneId ZONA = ZoneId.of("America/Sao_Paulo");

    private Tempo() {
    }

    public static LocalDateTime agora() {
        return LocalDateTime.now(ZONA);
    }
}
