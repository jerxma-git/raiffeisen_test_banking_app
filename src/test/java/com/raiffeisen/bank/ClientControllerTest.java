package com.raiffeisen.bank;

import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.raiffeisen.bank.controllers.ClientController;
import com.raiffeisen.bank.models.Client;
import com.raiffeisen.bank.services.ClientService;

// bit weird to make this a bunch of *unit* test, but why not
@WebMvcTest(ClientController.class)
public class ClientControllerTest {

    MockMvc mockMvc;

    ObjectMapper objectMapper;

    @MockBean
    ClientService clientService;

    Client sampleClient = new Client(null, "sample_client_firstName", "sample_client_lastName",
            "sample_client_email@mail.domain");

    Map<String, Client> validClients = Map.of(
            "sample_client", sampleClient,
            "no_email_client", new Client(null, "no_email_client_firstName", "no_email_client_lastName", null));
    Map<String, Client> invalidClients = Map.of(
            "no_lastName_client",
            new Client(null, "no_last_name_client_firstName", null, "no_lastName_client_email@mail.domain"),
            "no_firstName_client",
            new Client(null, null, "no_firstName_client_name", "no_firstName_client_email@mail.domain"),
            "nothing_client", new Client(null, null, null, null));

    @Autowired
    public ClientControllerTest(ObjectMapper objectMapper, MockMvc mockMvc) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
        this.objectMapper.setSerializationInclusion(Include.NON_NULL);
    }

    @Test
    void testCreateValidClients() throws Exception {
        for (Client client : validClients.values()) {
            Client returnClient = Client.builder()
                    .firstName(client.getFirstName())
                    .lastName(client.getLastName())
                    .email(client.getEmail())
                    .id(1L)
                    .build();

            Mockito.when(clientService.createClient(Mockito.any(Client.class)))
                    .thenReturn(returnClient);

            ResultActions result = mockMvc.perform(
                    MockMvcRequestBuilders.post("/api/clients/create")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(client)))
                    .andExpect(MockMvcResultMatchers.status().isCreated())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.firstName").value(client.getFirstName()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.lastName").value(client.getLastName()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.id").exists());

            // optional fields
            if (client.getEmail() != null) {
                result.andExpect(MockMvcResultMatchers.jsonPath("$.email").value(client.getEmail()));
            }
        }
    }

    // TODO: implement validation and uncomment
    // @Test
    void testCreateInvalidClients() throws Exception {
        for (Client client : invalidClients.values()) {
            Client returnClient = Client.builder()
                    .firstName(client.getFirstName())
                    .lastName(client.getLastName())
                    .email(client.getEmail())
                    .id(1L)
                    .build();

            Mockito.when(clientService.createClient(Mockito.any(Client.class)))
                    .thenReturn(returnClient);

            mockMvc.perform(
                    MockMvcRequestBuilders.post("/api/clients/create")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(client)))
                    .andExpect(MockMvcResultMatchers.status().isBadRequest());
        }
    }

    @Test
    void testGetClientById() throws Exception {
        Client[] clients = validClients.values().toArray(Client[]::new);
        Map<Long, Client> clientsById = IntStream.range(0, clients.length)
                .boxed()
                .collect(Collectors.toMap(index -> Long.valueOf(index.longValue()), index -> clients[index]));

        for (Entry<Long, Client> entry : clientsById.entrySet()) {
            final Client returnClient = Client.builder()
                    .firstName(entry.getValue().getFirstName())
                    .lastName(entry.getValue().getLastName())
                    .email(entry.getValue().getEmail())
                    .id(entry.getKey())
                    .build();

            Mockito.when(clientService.getClientById(entry.getKey()))
                    .thenReturn(returnClient);
        }

        for (Map.Entry<Long, Client> entry : clientsById.entrySet()) {
            ResultActions result = mockMvc.perform(MockMvcRequestBuilders.get("/api/clients/{id}", entry.getKey()))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(entry.getKey()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.firstName").value(entry.getValue().getFirstName()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.lastName").value(entry.getValue().getLastName()));
            if (entry.getValue().getEmail() != null) {
                result.andExpect(MockMvcResultMatchers.jsonPath("$.email").value(entry.getValue().getEmail()));
            }
        }

    }

}
