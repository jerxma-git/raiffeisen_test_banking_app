package com.raiffeisen.bank;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

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
    Client sampleClient = Client.builder()
        .id(1L)
        .lastName("Zhmyshenko")
        .firstName("Valery")
        .email("valzhmysh@mail.ru")
        .build();

    @Autowired
    public AccountServiceTest(AccountService accountService, AccountRepository accountRepository, ClientService clientService) {
        this.accountService = accountService;
        this.accountRepository = accountRepository;
        this.clientService = clientService;
    }

    private Long assignNewAccountId() {
        // TODO: assign unique id
        return 1L;
    }

    @Test
    void testOpenNewAccount() {
        Mockito.when(clientService.getClientById(sampleClient.getId()))
            .thenReturn(sampleClient);
        Mockito.when(accountRepository.save(Mockito.any(Account.class)))
            .thenAnswer(invocation -> {
                Account acc = invocation.getArgument(0);
                acc.setId(assignNewAccountId());

                Mockito.when(accountRepository.findById(acc.getId()))
                    .thenReturn(Optional.of(acc));
                return acc;
            });

        Account acc = accountService.openNewAccount(sampleClient.getId());

        assertNotNull(acc);
        assertNotNull(acc.getId());
        Client client = acc.getClient();
        assertNotNull(client);
        assertNotNull(client.getId());
        assertEquals(client.getId(), sampleClient.getId());
    }

    @Test
    void testCloseAccountByAccountNumber() {
        List<Account> accounts = Stream.of(1L, 2L, 3L, 4L)
            .map((Long id) -> Account.builder()
                    .id(id)
                    .client(sampleClient) 
                    .accountNumber("num" + id.toString())
                    .balance(0.0)
                    .status(AccountStatus.ACTIVE)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build())
            .toList();
        accounts.forEach(account -> {
            Mockito.when(accountRepository.findById(account.getId()))
                .thenReturn(Optional.of(account));
            Mockito.when(accountRepository.findByAccountNumber(account.getAccountNumber()))
                .thenReturn(Optional.of(account));
        });

        List<String> accNumsToClose = List.of(
            accounts.get(1).getAccountNumber(),
            accounts.get(3).getAccountNumber());
            
        accNumsToClose.forEach(num -> accountService.closeAccountByAccountNumber(num));

        for (Account acc : accounts) {
            boolean shouldBeClosed = accNumsToClose.contains(acc.getAccountNumber());
            assertEquals(acc.getStatus() == AccountStatus.CLOSED, shouldBeClosed);
        }
    }


}
