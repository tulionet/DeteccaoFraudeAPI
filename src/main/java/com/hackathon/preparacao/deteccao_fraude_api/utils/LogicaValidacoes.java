package com.hackathon.preparacao.deteccao_fraude_api.utils;

import com.hackathon.preparacao.deteccao_fraude_api.domain.Transacao;
import com.hackathon.preparacao.deteccao_fraude_api.service.TransacaoService;
import org.springframework.cglib.core.Local;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class LogicaValidacoes {

    TransacaoService transacaoService;

    public boolean validarMesmaTitularidade(Transacao transacao) {
        return true ? transacao.getUserId().equals(transacao.getDestino().getIdDestino()) : false;
    }

    public boolean validarValorMedioGastoDiario(Transacao transacao) {
        List<Transacao> listaTransacao = transacaoService.buscarListaDeGastosPorCliente(transacao.getUserId());

        LocalDateTime hoje = LocalDateTime.now();
        LocalDateTime trintaDiasAtras = hoje.minusDays(30);

        List<Transacao> ultimosTrintaDias = listaTransacao.stream()
                .filter(t -> t.getDataTransacao().isAfter(trintaDiasAtras) && t.getDataTransacao().isBefore(hoje))
                .collect(Collectors.toList());

        double somaTotal = ultimosTrintaDias.stream()
                .mapToDouble(Transacao::getSaldoTransacao)
                .sum();

        double mediaDiaria = somaTotal / 30.0;

        return transacao.getSaldoTransacao() <= (mediaDiaria * 3);
    }

    public boolean validarQuantidadeTransferencias(Transacao transacao) {
        return true;
    }
    public boolean validarGrandeGasto(Transacao transacao) {
        return true;
    }
    public boolean validarFraudeRecebedor(Transacao transacao) {
        return true;
    }
    public boolean validarLocalidadeGeografica(Transacao transacao) {
        return true;
    }
    public boolean validarHorarioTransacoes(Transacao transacao) {
        return true;
    }
    public boolean validarDispositvo(Transacao transacao) {
        return true;
    }
    public boolean validarPadraoTriangulo(Transacao transacao) {
        return true;
    }
}
