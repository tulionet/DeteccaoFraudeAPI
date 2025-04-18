package com.hackathon.preparacao.deteccao_fraude_api.controller;

import com.hackathon.preparacao.deteccao_fraude_api.domain.Transacao;
import com.hackathon.preparacao.deteccao_fraude_api.service.TransacaoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/transacao")
public class TransacaoController {

    @Autowired
    private TransacaoService transacaoService;

    @PostMapping
    public void receberTransacao(@RequestBody @Valid Transacao transacao){
        transacaoService.validarJSON(transacao);
    }



}
