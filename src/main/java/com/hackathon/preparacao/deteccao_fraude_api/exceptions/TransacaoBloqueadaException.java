package com.hackathon.preparacao.deteccao_fraude_api.exceptions;

public class TransacaoBloqueadaException extends RuntimeException {
    public TransacaoBloqueadaException(String message) {
        super(message);
    }
}
