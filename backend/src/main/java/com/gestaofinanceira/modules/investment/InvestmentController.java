package com.gestaofinanceira.modules.investment;

import com.gestaofinanceira.modules.user.User;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/investments")
public class InvestmentController {

    private final InvestmentService investmentService;

    public InvestmentController(InvestmentService investmentService) {
        this.investmentService = investmentService;
    }

    @GetMapping("/positions")
    public ResponseEntity<List<InvestmentDTO.PositionResponse>> getPositions(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(investmentService.getPortfolioPositions(user));
    }

    @GetMapping("/transactions")
    public ResponseEntity<List<InvestmentDTO.OrderResponse>> listOrders(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(investmentService.listOrders(user));
    }

    @PostMapping("/transactions")
    public ResponseEntity<InvestmentDTO.OrderResponse> recordOrder(
            @Valid @RequestBody InvestmentDTO.OrderRequest request,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(investmentService.recordOrder(request, user));
    }
}
