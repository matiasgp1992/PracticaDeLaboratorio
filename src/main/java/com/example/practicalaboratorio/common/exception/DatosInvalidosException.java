package com.example.practicalaboratorio.common.exception;

import java.util.Map;

public class DatosInvalidosException extends RuntimeException {

    private final Map<String, String> errores;

    public DatosInvalidosException(Map<String, String> errores) {
        super("Hay datos inválidos");
        this.errores = errores;
    }

    public Map<String, String> getErrores() {
        return errores;
    }
}