package com.hackathon.preparacao.deteccao_fraude_api.utils;

import com.hackathon.preparacao.deteccao_fraude_api.domain.Transacao;
import com.hackathon.preparacao.deteccao_fraude_api.service.TransacaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.core.Local;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class LogicaValidacoes {

    @Autowired
    private TransacaoService transacaoService;

    private List<Transacao> getListaTransacao(Transacao transacao) {
        return transacaoService.buscarListaTransacoesPorCliente(transacao.getUserId());
    }

    public boolean validarMesmaTitularidade(Transacao transacao) {
        return transacao.getUserId().equals(transacao.getDestino().getIdDestino());
    }


    public boolean validarValorMedioGastoDiario(Transacao transacao) {
        List<Transacao> listaTransacao = getListaTransacao(transacao);
        LocalDateTime hoje = LocalDateTime.now();
        LocalDateTime trintaDiasAtras = hoje.minusDays(30);

        List<Transacao> ultimosTrintaDias = transacoesPorPeriodo(trintaDiasAtras, hoje, listaTransacao);

        double somaTotal = ultimosTrintaDias.stream()
                .mapToDouble(Transacao::getSaldoTransacao)
                .sum();

        double mediaDiaria = somaTotal / 30.0;

        return transacao.getSaldoTransacao() >= (mediaDiaria * 3);
    }

    public boolean validarQuantidadeTransferencias(Transacao transacao) {
        List<Transacao> listaTransacao = getListaTransacao(transacao);

        int qtdLimiteTransferencias = 5;
        int tempoEntreTransferencias = 5;

        long quantidadeTransacoes = listaTransacao.stream()
                .filter(t -> t.getDataTransacao().isAfter(transacao.getDataTransacao().minusMinutes(tempoEntreTransferencias)))
                .count();

        return quantidadeTransacoes > qtdLimiteTransferencias;

    }

    public boolean validarGrandeGasto(Transacao transacao) {
        List<Transacao> listaTransacao = getListaTransacao(transacao);

        LocalDateTime hoje = LocalDateTime.now();
        LocalDateTime trintaDiasAtras = hoje.minusDays(30);

        List<Transacao> ultimosTrintaDias = listaTransacao.stream()
                .filter(t -> t.getDataTransacao().isAfter(trintaDiasAtras) && t.getDataTransacao().isBefore(hoje))
                .collect(Collectors.toList());

        double maiorTransacaoTrintaDias = ultimosTrintaDias.stream()
                .mapToDouble(Transacao::getSaldoTransacao)
                .max()
                .orElse(0);

        return transacao.getSaldoTransacao() > maiorTransacaoTrintaDias;
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

    public List<Transacao> transacoesPorPeriodo(LocalDateTime dataInicio, LocalDateTime dataFim, List<Transacao> listaTransacao ) {
        List<Transacao> transacoesNoPeriodo = listaTransacao.stream()
                .filter(t -> t.getDataTransacao().isAfter(dataInicio) && t.getDataTransacao().isBefore(dataFim))
                .collect(Collectors.toList());
        return transacoesNoPeriodo;
    }
}
