package com.hackathon.preparacao.deteccao_fraude_api.service;

import com.hackathon.preparacao.deteccao_fraude_api.domain.Destino;
import com.hackathon.preparacao.deteccao_fraude_api.domain.InfoDispositivo;
import com.hackathon.preparacao.deteccao_fraude_api.domain.Localizacao;
import com.hackathon.preparacao.deteccao_fraude_api.domain.Transacao;
import com.hackathon.preparacao.deteccao_fraude_api.utils.LogicaValidacoes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class TransacaoService {

    @Autowired
    LogicaValidacoes logicaValidacoes;

    public void validarJSON(Transacao transacao) {
        logicaValidacoes.validarMesmaTitularidade(transacao);
    }

    public void validarValorMedioGastoDiario(Transacao transacao){
        logicaValidacoes.validarValorMedioGastoDiario(transacao);
    }

    public List<Transacao> buscarListaDeGastosPorCliente(@PathVariable String id){
        /*
        Lógica para pegar lista de gastos no banco de dados ou API.
        */

        List<Transacao> transacoes = new ArrayList<>();

        for (int i = 1; i <= 10; i++) {
            Localizacao localizacao = new Localizacao();
            localizacao.setLatidute(-23.55000 + (i * 0.0001));
            localizacao.setLongitude(-46.63000 + (i * 0.0001));

            InfoDispositivo info = new InfoDispositivo();
            info.setTipoDispositivo("MOBILE");
            info.setSoDispotivo("iOS 17");
            info.setEnderecoIp("189.45.23." + i);

            Destino destino = new Destino();
            destino.setIdDestino("mercado_" + i);
            destino.setNameDestino("Loja XPTO " + i);
            destino.setCategoriaDestino("Retail");

            Transacao transacao = new Transacao(
                    "TX20250417123" + i,
                    "cliente_00" + i,
                    12345670 + i,
                    8000.00 + (i * 100),
                    "TRANSFER",
                    LocalDateTime.now().minusMinutes(i),
                    localizacao,
                    info,
                    destino
            );

            transacoes.add(transacao);
        return transacoes;
    }
}
