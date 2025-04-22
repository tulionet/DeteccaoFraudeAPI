package com.hackathon.preparacao.deteccao_fraude_api.utils;

import com.hackathon.preparacao.deteccao_fraude_api.domain.Transacao;
import com.hackathon.preparacao.deteccao_fraude_api.service.TransacaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
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
                .filter(t -> t.getDataTransacao().isAfter(LocalDateTime.now().minusMinutes(tempoEntreTransferencias)))
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
        List<Transacao> listaTransacoesPorPeriodo = transacoesPorPeriodo(transacao.getDataTransacao().minusDays(30), transacao.getDataTransacao(), listaTransacoes);

        Transacao ultimaTransacao = listaTransacoesPorPeriodo.stream()
                .max(Comparator.comparing(Transacao::getDataTransacao))
                .orElse(null);

        if (ultimaTransacao != null) {
            return transacaoService.verificaDistanciaLocalizacao(transacao, ultimaTransacao);
        }
        return false;
    }

    public boolean validarHorarioIncomumTransacao(Transacao transacao) {
        List<Transacao> listaTransacoes = getListaTransacao(transacao);
        List<Transacao> historicoTransacoes = transacoesPorPeriodo(transacao.getDataTransacao().minusDays(30), transacao.getDataTransacao(), listaTransacoes);

        if (historicoTransacoes.isEmpty()) return false;

        List<LocalTime> horarios = historicoTransacoes.stream()
                .map(t -> t.getDataTransacao().toLocalTime())
                .toList();

        LocalTime horarioMedio = calcularHorarioMedio(horarios);
        Double desvioPadrao = calcularDesvioPadrao(horarios, horarioMedio);

        LocalTime limiteInferior = horarioMedio.minusMinutes(desvioPadrao.longValue());
        LocalTime limiteSuperior = horarioMedio.plusMinutes(desvioPadrao.longValue());

        LocalTime horarioTransacao = transacao.getDataTransacao().toLocalTime();
        return !(horarioTransacao.isBefore(limiteInferior) || horarioTransacao.isAfter(limiteSuperior));
    }

    public boolean validarDispositvo(Transacao transacao) {
        List<Transacao> listaTransacoes = getListaTransacao(transacao);
        List<Transacao> listaTransacoesPorPeriodo = transacoesPorPeriodo(transacao.getDataTransacao().minusDays(30), transacao.getDataTransacao(), listaTransacoes);

        Transacao ultimaTransacao = listaTransacoesPorPeriodo.stream()
                .max(Comparator.comparing(Transacao::getDataTransacao))
                .orElse(null);

        if (ultimaTransacao != null) {
            return !ultimaTransacao.equals(transacao);
        }

        return false;
    }

    public boolean validarPadraoTriangulo(Transacao transacao) {
        // TODO
        return false;
    }

    private List<Transacao> transacoesPorPeriodo(LocalDateTime dataInicio, LocalDateTime dataFim, List<Transacao> listaTransacao ) {
        return listaTransacao.stream()
                .filter(t -> t.getDataTransacao().isAfter(dataInicio) && t.getDataTransacao().isBefore(dataFim))
                .collect(Collectors.toList());
    }

    private LocalTime calcularHorarioMedio(List<LocalTime> horarios) {
        int totalMinutos = horarios.stream()
                .mapToInt(horario -> horario.getHour() * 60 + horario.getMinute())
                .sum();
        int mediaMinutos = totalMinutos / horarios.size();
        return LocalTime.of(mediaMinutos / 60, mediaMinutos % 60);
    }

    private Double calcularDesvioPadrao(List<LocalTime> horarios, LocalTime horarioMedio) {
        int mediaMinutos = horarioMedio.getHour() * 60 + horarioMedio.getMinute();
        double somaDiferencasQuadradas = horarios.stream()
                .mapToInt(horario -> {
                    int minutos = horario.getHour() * 60 + horario.getMinute();
                    return minutos - mediaMinutos;
                })
                .map(diferenca -> diferenca * diferenca)
                .sum();
        return Math.sqrt(somaDiferencasQuadradas / horarios.size());
    }
}
