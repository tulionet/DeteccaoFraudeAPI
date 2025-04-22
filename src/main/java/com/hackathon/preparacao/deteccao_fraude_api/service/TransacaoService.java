package com.hackathon.preparacao.deteccao_fraude_api.service;

import com.hackathon.preparacao.deteccao_fraude_api.domain.*;
import com.hackathon.preparacao.deteccao_fraude_api.exceptions.TransacaoBloqueadaException;
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

    private static final int LIMITE_SUSPEITO = 10;
    private static final int LIMITE_BLOQUEIO = 20;

    public tiposValidados validarJSON(Transacao transacao) {
        try {
            if (logicaValidacoes.validarMesmaTitularidade(transacao)) {
                return tiposValidados.LIBERADO;
            }

            int pontuacaoRisco = 0;

            pontuacaoRisco += validarPontuacao(logicaValidacoes.validarValorMedioGastoDiario(transacao), 5, pontuacaoRisco);
            pontuacaoRisco += validarPontuacao(logicaValidacoes.validarQuantidadeTransferencias(transacao), 20, pontuacaoRisco);
            pontuacaoRisco += validarPontuacao(logicaValidacoes.validarGrandeGasto(transacao), 10, pontuacaoRisco);
            pontuacaoRisco += validarPontuacao(logicaValidacoes.validarFraudeRecebedor(transacao), 15, pontuacaoRisco);
            pontuacaoRisco += validarPontuacao(logicaValidacoes.validarLocalidadeGeografica(transacao), 20, pontuacaoRisco);
            pontuacaoRisco += validarPontuacao(logicaValidacoes.validarHorarioIncomumTransacao(transacao), 7, pontuacaoRisco);
            pontuacaoRisco += validarPontuacao(logicaValidacoes.validarDispositvo(transacao), 8, pontuacaoRisco);
            pontuacaoRisco += validarPontuacao(logicaValidacoes.validarPadraoTriangulo(transacao), 12, pontuacaoRisco);

            return determinarTipoValidacao(pontuacaoRisco);

        } catch (TransacaoBloqueadaException e) {
            return tiposValidados.BLOQUEADO;
        }
    }

    private int validarPontuacao(boolean condicao, int pontuacao, int totalPontuacao) throws TransacaoBloqueadaException {
        if (condicao) {
            if (totalPontuacao >= LIMITE_BLOQUEIO) {
                throw new TransacaoBloqueadaException("Transação com risco alto detectado!");
            }
            return pontuacao;
        }
        return 0;
    }

    private tiposValidados determinarTipoValidacao(int pontuacaoRisco) {
        if (pontuacaoRisco > LIMITE_BLOQUEIO) {
            return tiposValidados.BLOQUEADO;
        } else if (pontuacaoRisco > LIMITE_SUSPEITO) {
            return tiposValidados.SUSPEITO;
        } else {
            return tiposValidados.LIBERADO;
        }
    }


    public List<Transacao> buscarListaTransacoesPorCliente(@PathVariable String id){
        /*
        TODO Lógica para pegar lista de gastos no banco de dados ou API.
        */

        List<Transacao> transacoes = new ArrayList<>();

        //hardcode pra gerar transacoes
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
        }
        return transacoes;
    }

    public Boolean validarFraudeRecebedor(String idMercadorDestino) {
        /*
        TODO lógica pra pegar os dados do mercador
        */
        return true;
    }

    public boolean verificaDistanciaLocalizacao(Transacao transacao, Transacao ultimaTransacao) {
        double distanciaMetros = calcularDistanciaGeografica(transacao, ultimaTransacao);

        double distanciaKilometros = distanciaMetros / 1000;
        long intervaloSegundos = java.time.Duration.between(ultimaTransacao.getDataTransacao(), transacao.getDataTransacao()).getSeconds();

        double velocidadeKmH = (distanciaKilometros / intervaloSegundos) * 3600;
        return velocidadeKmH > 1000;
    }

    private static double calcularDistanciaGeografica(Transacao transacao, Transacao ultimaTransacao) {
        double raioDaTerra = 6371000; // em metros
        double lat1 = transacao.getLocalizacao().getLatidute();
        double lng1 = transacao.getLocalizacao().getLongitude();
        double lat2 = ultimaTransacao.getLocalizacao().getLatidute();
        double lng2 = ultimaTransacao.getLocalizacao().getLongitude();

        double dLat = Math.toRadians(lat2-lat1);
        double dLng = Math.toRadians(lng2-lng1);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLng/2) * Math.sin(dLng/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        return (raioDaTerra * c);
    }
}
