package com.raiffeisen.bank.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.raiffeisen.bank.models.Account;
import com.raiffeisen.bank.models.AccountStatus;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    Optional<Account> findByAccountNumber(String accountNumber);

    List<Account> findByClient_Id(Long clientId);

    List<Account> findByClient_IdAndStatusNot(Long clientID, AccountStatus status);

    List<Account> findByStatusNot(AccountStatus status);
}
