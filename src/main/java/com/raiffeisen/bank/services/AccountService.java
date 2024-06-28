package com.raiffeisen.bank.services;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.raiffeisen.bank.models.Account;
import com.raiffeisen.bank.models.AccountStatus;
import com.raiffeisen.bank.models.Client;
import com.raiffeisen.bank.repositories.AccountRepository;

@Service
public class AccountService {
    
    private static final int ACCOUNT_NUMBER_LENGTH = 20; 

    private final AccountRepository accountRepository;
    private final ClientService clientService;

    @Autowired
    public AccountService(AccountRepository accountRepository, ClientService clientService) {
        this.accountRepository = accountRepository;
        this.clientService = clientService;
    }


    public Account openNewAccount(Long clientID) {
        Client client = clientService.getClientById(clientID); 
        if (client == null) {
            return null;
        }
        Account account = Account.builder()
            .client(client)
            .accountNumber(generateUniqueAccountNumber())
            .balance(Double.valueOf(0))
            .status(AccountStatus.ACTIVE)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

        accountRepository.save(account);
        return account;
    }

    private String generateUniqueAccountNumber() {
        Random random = new Random();
        String accountNumber;
        do {
            accountNumber = random.ints()
                    .limit(ACCOUNT_NUMBER_LENGTH)
                    .mapToObj(i -> String.valueOf(Math.abs(i % 10)))
                    .collect(Collectors.joining());
        } while (getAccountByAccountNumber(accountNumber) != null);
        return accountNumber;
    }

    public Account getAccountByAccountNumber(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber).orElse(null);
    }

    public Account getAccountById(Long id) {
        return accountRepository.findById(id).orElse(null);
    }


    public boolean closeAccountByAccountNumber(String accountNumber) {
        Account account = getAccountByAccountNumber(accountNumber);
        if (account == null || account.getStatus() == AccountStatus.CLOSED) {
            return false;
        }
        account.setStatus(AccountStatus.CLOSED);
        account.setUpdatedAt(LocalDateTime.now());
        accountRepository.save(account);
        return true;        
    }

    public boolean applyAccountBalanceDelta(String accountNumber, Double delta) {
        Account account = getAccountByAccountNumber(accountNumber);
        if (account == null) {
            return false;
        }
        // TODO: rework into exception
        Double balance = account.getBalance();
        if (balance + delta < 0) {
            return false;
        }

        account.setBalance(balance + delta);
        account.setUpdatedAt(LocalDateTime.now());
        accountRepository.save(account);
        return true;
    }


    public List<Account> getRecentAccounts(Long clientID, int limit) {
        return accountRepository.findByClient_Id(clientID).stream()
                .sorted(Comparator.comparing(Account::getUpdatedAt).reversed())
                .limit(limit)
                .toList();
    }



}
