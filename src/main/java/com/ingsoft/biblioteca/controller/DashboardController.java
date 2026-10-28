package com.ingsoft.biblioteca.controller;

import com.ingsoft.biblioteca.dto.DashboardStats;
import com.ingsoft.biblioteca.service.DashboardService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/admin/dashboard")
    public String mostrarDashboard(Model model) {
        DashboardStats stats = dashboardService.obtenerEstadisticas();
        model.addAttribute("stats", stats);
        return "admin-dashboard";
    }
}
