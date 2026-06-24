package com.gabriel.assistentefinanceiro.controller;

import com.gabriel.assistentefinanceiro.dto.DashboardMensalResponse;
import com.gabriel.assistentefinanceiro.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/mensal")
    public DashboardMensalResponse mensal(
            @RequestParam Long usuarioId,
            @RequestParam Integer ano,
            @RequestParam Integer mes
    ) {
        return dashboardService.mensal(usuarioId, ano, mes);
    }
}
