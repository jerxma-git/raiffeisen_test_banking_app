package com.raiffeisen.bank.services;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.raiffeisen.bank.DTO.AccountDTO;
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

    public AccountDTO openNewAccount(Long clientID) {
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
        return mapToDTO(account);
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

    public AccountDTO getAccountDTOByAccountNumber(String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber).orElse(null);
        return account == null ? null : mapToDTO(account);
    }

    public Account getAccountById(Long id) {
        return accountRepository.findById(id).orElse(null);
    }

    public AccountDTO getAccountDTOById(Long id) {
        Account account = accountRepository.findById(id).orElse(null);
        return account == null ? null : mapToDTO(account);
    }

    public boolean closeAccountByAccountNumber(String accountNumber) {
        Account account = getAccountByAccountNumber(accountNumber);
        if (account == null || account.getStatus() == AccountStatus.CLOSED) {
            return false;
        }
        // TODO: rework into exception or DTO
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
        // TODO: rework into exception or DTO
        Double balance = account.getBalance();
        if (balance + delta < 0) {
            return false;
        }

        account.setBalance(balance + delta);
        account.setUpdatedAt(LocalDateTime.now());
        accountRepository.save(account);
        return true;
    }

    public List<AccountDTO> getRecentAccounts(Long clientID, int limit) {
        return accountRepository.findByClient_Id(clientID).stream()
                .sorted(Comparator.comparing(Account::getUpdatedAt).reversed())
                .limit(limit)
                .map(this::mapToDTO)
                .toList();
    }


    public AccountDTO mapToDTO(Account account) {
        return AccountDTO.builder()
            .id(account.getId())
            .clientID(account.getClient().getId())
            .accountNumber(account.getAccountNumber())
            .balance(account.getBalance())
            .status(account.getStatus())
            .createdAt(account.getCreatedAt())
            .updatedAt(account.getUpdatedAt())
            .build();
    }

}
