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

        List<Transacao> ultimosTrintaDias = transacoesPorPeriodo(trintaDiasAtras, hoje, listaTransacao);

        double maiorTransacaoTrintaDias = ultimosTrintaDias.stream()
                .mapToDouble(Transacao::getSaldoTransacao)
                .max()
                .orElse(0);

        return transacao.getSaldoTransacao() > maiorTransacaoTrintaDias;
    }

    public boolean validarFraudeRecebedor(Transacao transacao) {
        return transacaoService.validarFraudeRecebedor(transacao.getDestino().getIdDestino());
    }

    public boolean validarLocalidadeGeografica(Transacao transacao) {
        List<Transacao> listaTransacoes = getListaTransacao(transacao);
        List<Transacao> listaTransacoesPorPeriodo = transacoesPorPeriodo(transacao.getDataTransacao(), transacao.getDataTransacao().minusDays(30), listaTransacoes);

        Transacao ultimaTransacao = listaTransacoesPorPeriodo.stream()
                .max((t1, t2) -> t1.getDataTransacao().compareTo(t2.getDataTransacao()))
                .orElse(null);

        if (ultimaTransacao != null) {
            return transacaoService.verificaDistanciaLocalizacao(transacao.getLocalizacao(), ultimaTransacao.getLocalizacao());
        }
        return false;
    }

    public boolean validarHorarioIncomumTransacao(Transacao transacao) {
        //TODO validação horario media das transacoes
        return true;
    }

    public boolean validarDispositvo(Transacao transacao) {
        List<Transacao> listaTransacoes = getListaTransacao(transacao);
        List<Transacao> listaTransacoesPorPeriodo = transacoesPorPeriodo(transacao.getDataTransacao(), transacao.getDataTransacao().minusDays(30), listaTransacoes);

        Transacao ultimaTransacao = listaTransacoesPorPeriodo.stream()
                .max((t1, t2) -> t1.getDataTransacao().compareTo(t2.getDataTransacao()))
                .orElse(null);

        if (ultimaTransacao != null) {
            return !ultimaTransacao.equals(transacao);
        }

        return false;
    }

    public boolean validarPadraoTriangulo(Transacao transacao) {
        // TODO
        return true;
    }

    public List<Transacao> transacoesPorPeriodo(LocalDateTime dataInicio, LocalDateTime dataFim, List<Transacao> listaTransacao ) {
        List<Transacao> transacoesNoPeriodo = listaTransacao.stream()
                .filter(t -> t.getDataTransacao().isAfter(dataInicio) && t.getDataTransacao().isBefore(dataFim))
                .collect(Collectors.toList());
        return transacoesNoPeriodo;
    }

}
