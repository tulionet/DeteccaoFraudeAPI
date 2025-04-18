package com.hackathon.preparacao.deteccao_fraude_api.domain;

import lombok.*;
import org.springframework.data.annotation.Id;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class Transacao {

    @Id
    private String id;

    private String userId;
    private Integer numeroConta;
    private Double saldoTransacao;
    private String tipoTransacao;
    private LocalDateTime dataTransacao;

    private Localizacao localizacao;

    private InfoDispositivo infoDispositivo;

    private Destino destino;
}
