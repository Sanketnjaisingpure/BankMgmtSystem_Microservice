package com.bank.controller;

import com.bank.service.LoanService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/loans")
public class LoanController {

    private final LoanService loanService;


    public LoanController(LoanService loanService){
        this.loanService = loanService;
    }


}
