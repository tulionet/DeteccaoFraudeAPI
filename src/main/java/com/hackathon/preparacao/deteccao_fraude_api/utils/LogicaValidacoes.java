package com.hackathon.preparacao.deteccao_fraude_api.utils;

import com.hackathon.preparacao.deteccao_fraude_api.domain.Transacao;
import com.hackathon.preparacao.deteccao_fraude_api.service.TransacaoService;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class LogicaValidacoes {

    TransacaoService transacaoService;

    public boolean validarMesmaTitularidade(Transacao transacao) {
        return true ? transacao.getUserId().equals(transacao.getDestino().getIdDestino()) : false;
    }

    public boolean validarValorMedioGastoDiario(Transacao transacao) {
        List<Transacao> listaTransa = transacaoService.buscarListaDeGastosPorCliente(transacao.getUserId());
        // fazer o calculo de media de gasto diario pelas transações pegando o get dataTransação e o saldoTransacao
        // fazer o calculo 30 dias pra tras a partir de hoje
        return true;
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
