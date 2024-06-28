package com.raiffeisen.bank;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import com.raiffeisen.bank.DTO.AccountDTO;
import com.raiffeisen.bank.models.Account;
import com.raiffeisen.bank.models.AccountStatus;
import com.raiffeisen.bank.models.Client;
import com.raiffeisen.bank.repositories.AccountRepository;
import com.raiffeisen.bank.services.AccountService;
import com.raiffeisen.bank.services.ClientService;

@SpringBootTest
public class AccountServiceTest {

    AccountService accountService;

    @MockBean
    AccountRepository accountRepository;

    @MockBean
    ClientService clientService;

    Client sampleClient;
    List<Account> sampleAccounts;

    Set<Long> savedAccountIDs;

    @Autowired
    public AccountServiceTest(AccountService accountService, AccountRepository accountRepository,
            ClientService clientService) {
        this.accountService = accountService;
        this.accountRepository = accountRepository;
        this.clientService = clientService;
    }

    @BeforeEach
    void setUp() {
        sampleClient = Client.builder()
                .id(1L)
                .lastName("Zhmyshenko")
                .firstName("Valery")
                .email("valzhmysh@mail.ru")
                .build();

        sampleAccounts = Stream.of(1L, 2L, 3L, 4L)
                .map((Long id) -> Account.builder()
                        // .id(id)
                        .client(sampleClient)
                        .accountNumber("num" + id)
                        .balance(0.0)
                        .status(AccountStatus.ACTIVE)
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build())
                .collect(Collectors.toCollection(ArrayList::new));

        savedAccountIDs = new HashSet<>();

        Mockito.when(accountRepository.save(Mockito.any(Account.class)))
                .thenAnswer(invocation -> {
                    Account acc = invocation.getArgument(0);
                    acc.setId(assignNewAccountId());
                    savedAccountIDs.add(acc.getId());

                    Mockito.when(accountRepository.findById(acc.getId()))
                            .thenReturn(Optional.of(acc));
                    Mockito.when(accountRepository.findByAccountNumber(acc.getAccountNumber()))
                            .thenReturn(Optional.of(acc));
                    return acc;
                });

        sampleAccounts.forEach(accountRepository::save);
        Mockito.when(clientService.getClientById(sampleClient.getId())).thenReturn(sampleClient);
        Mockito.when(accountRepository.findByClient_Id(Mockito.anyLong()))
                .thenAnswer(invocation -> {
                    Long clientID = invocation.getArgument(0);
                    return sampleAccounts.stream()
                            .filter(account -> account.getClient().getId() == clientID
                                    && savedAccountIDs.contains(account.getId()))
                            .toList();
                });
    }

    private Long assignNewAccountId() {
        return savedAccountIDs.size() + 1L;
    }

    @Test
    void testOpenNewAccount() {
        AccountDTO acc = accountService.openNewAccount(sampleClient.getId());

        assertNotNull(acc);
        assertNotNull(acc.getId());
        assertNotNull(acc.getClientID());
        assertEquals(sampleClient.getId(), acc.getClientID());
    }

    @Test
    void testCloseAccountByAccountNumber() {
        List<String> accNumsToClose = List.of(
                sampleAccounts.get(1).getAccountNumber(),
                sampleAccounts.get(3).getAccountNumber());

        accNumsToClose.forEach(num -> accountService.closeAccountByAccountNumber(num));

        for (Account acc : sampleAccounts) {
            boolean shouldBeClosed = accNumsToClose.contains(acc.getAccountNumber());
            assertEquals(shouldBeClosed ? AccountStatus.CLOSED : AccountStatus.ACTIVE, acc.getStatus());
        }
    }

    @Test
    void testApplyAccountBalanceDelta() {
        Account acc = sampleAccounts.get(0);
        Double initialBalance = 0.0;
        acc.setBalance(initialBalance);
        Double delta1 = 200.0;
        Double delta2 = -150.01;
        boolean result;

        result = accountService.applyAccountBalanceDelta(acc.getAccountNumber(), delta1);
        assertTrue(result);
        assertEquals(initialBalance + delta1, acc.getBalance(), 0.0001);

        result = accountService.applyAccountBalanceDelta(acc.getAccountNumber(), delta2);
        assertTrue(result);
        assertEquals(initialBalance + delta1 + delta2, acc.getBalance(), 0.0001);

        result = accountService.applyAccountBalanceDelta(acc.getAccountNumber(), delta2);
        assertFalse(result);
    }

    @Test
    void testGetRecentAccounts() {
        int limit = 2;
        sampleAccounts.get(0).setUpdatedAt(LocalDateTime.now().plusMinutes(1));
        sampleAccounts.get(1).setUpdatedAt(LocalDateTime.now().plusHours(1));

        List<AccountDTO> recentAccounts = accountService.getRecentAccounts(sampleClient.getId(), limit);
        assertEquals(2, recentAccounts.size());
        assertEquals(sampleAccounts.get(1).getId(), recentAccounts.get(0).getId());
        assertEquals(sampleAccounts.get(0).getId(), recentAccounts.get(1).getId());

        sampleAccounts.get(2).setUpdatedAt(LocalDateTime.now().plusMinutes(30));
        recentAccounts = accountService.getRecentAccounts(sampleClient.getId(), limit);
        assertEquals(2, recentAccounts.size());
        assertEquals(sampleAccounts.get(1).getId(), recentAccounts.get(0).getId());
        assertEquals(sampleAccounts.get(2).getId(), recentAccounts.get(1).getId());
    }

}
