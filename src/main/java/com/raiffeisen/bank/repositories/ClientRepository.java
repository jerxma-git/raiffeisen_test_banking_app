package com.raiffeisen.bank.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.raiffeisen.bank.models.Client;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {
    
}
