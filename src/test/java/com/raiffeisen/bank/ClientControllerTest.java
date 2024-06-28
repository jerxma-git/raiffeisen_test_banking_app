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
import com.raiffeisen.bank.DTO.ClientDTO;
import com.raiffeisen.bank.controllers.ClientController;
import com.raiffeisen.bank.services.ClientService;

// bit weird to make this a bunch of *unit* test, but why not
@WebMvcTest(ClientController.class)
public class ClientControllerTest {

        MockMvc mockMvc;

        ObjectMapper objectMapper;

        @MockBean
        ClientService clientService;

        ClientDTO sampleClient = ClientDTO.builder()
                        .id(null)
                        .firstName("sample_client_firstName")
                        .lastName("sample_client_lastName")
                        .email("sample_client_email@mail.domain")
                        .build();

        Map<String, ClientDTO> validClients = Map.of(
                        "sample_client", sampleClient,
                        "no_email_client", ClientDTO.builder().id(null)
                                        .firstName("no_email_client_firstName")
                                        .lastName("no_email_client_lastName")
                                        .email(null)
                                        .build());

        Map<String, ClientDTO> invalidClients = Map.of(
                        "no_lastName_client", ClientDTO.builder()
                                        .id(null)
                                        .firstName("no_last_name_client_firstName")
                                        .lastName(null)
                                        .email("no_lastName_client_email@mail.domain")
                                        .build(),
                        "no_firstName_client", ClientDTO.builder()
                                        .id(null)
                                        .firstName(null)
                                        .lastName("no_firstName_client_lastName")
                                        .email("no_firstName_client_email@mail.domain")
                                        .build(),
                        "nothing_client", ClientDTO.builder()
                                        .id(null)
                                        .firstName(null)
                                        .lastName(null)
                                        .email(null)
                                        .build());

        @Autowired
        public ClientControllerTest(ObjectMapper objectMapper, MockMvc mockMvc) {
                this.mockMvc = mockMvc;
                this.objectMapper = objectMapper;
                this.objectMapper.setSerializationInclusion(Include.NON_NULL);
        }

        @Test
        void testCreateValidClients() throws Exception {
                for (ClientDTO client : validClients.values()) {
                        ClientDTO returnClient = ClientDTO.builder()
                                        .firstName(client.getFirstName())
                                        .lastName(client.getLastName())
                                        .email(client.getEmail())
                                        .id(1L)
                                        .build();

                        Mockito.when(clientService.createClient(Mockito.any(ClientDTO.class)))
                                        .thenReturn(returnClient);

                        ResultActions result = mockMvc.perform(
                                        MockMvcRequestBuilders.post("/api/clients/create")
                                                        .contentType(MediaType.APPLICATION_JSON)
                                                        .content(objectMapper.writeValueAsString(client)))
                                        .andExpect(MockMvcResultMatchers.status().isCreated())
                                        .andExpect(MockMvcResultMatchers.jsonPath("$.firstName")
                                                        .value(client.getFirstName()))
                                        .andExpect(MockMvcResultMatchers.jsonPath("$.lastName")
                                                        .value(client.getLastName()))
                                        .andExpect(MockMvcResultMatchers.jsonPath("$.id").exists());

                        // optional fields
                        if (client.getEmail() != null) {
                                result.andExpect(MockMvcResultMatchers.jsonPath("$.email").value(client.getEmail()));
                        }
                }
        }

        @Test
        void testCreateInvalidClients() throws Exception {
                for (ClientDTO client : invalidClients.values()) {
                        ClientDTO returnClient = ClientDTO.builder()
                                        .firstName(client.getFirstName())
                                        .lastName(client.getLastName())
                                        .email(client.getEmail())
                                        .id(1L)
                                        .build();

                        Mockito.when(clientService.createClient(Mockito.any(ClientDTO.class)))
                                        .thenReturn(returnClient);

                        mockMvc.perform(
                                        MockMvcRequestBuilders.post("/api/clients/create")
                                                        .contentType(MediaType.APPLICATION_JSON)
                                                        .content(objectMapper.writeValueAsString(client)))
                                        .andExpect(MockMvcResultMatchers.status().isBadRequest());
                }
        }

        @Test
        void testGetClientDTOById() throws Exception {
                ClientDTO[] clients = validClients.values().toArray(ClientDTO[]::new);
                Map<Long, ClientDTO> clientsById = IntStream.range(0, clients.length)
                                .boxed()
                                .collect(Collectors.toMap(index -> Long.valueOf(index.longValue()),
                                                index -> clients[index]));

                for (Entry<Long, ClientDTO> entry : clientsById.entrySet()) {
                        final ClientDTO returnClient = ClientDTO.builder()
                                        .firstName(entry.getValue().getFirstName())
                                        .lastName(entry.getValue().getLastName())
                                        .email(entry.getValue().getEmail())
                                        .id(entry.getKey())
                                        .build();

                        Mockito.when(clientService.getClientDTOById(entry.getKey()))
                                        .thenReturn(returnClient);
                }

                for (Map.Entry<Long, ClientDTO> entry : clientsById.entrySet()) {
                        ResultActions result = mockMvc
                                        .perform(MockMvcRequestBuilders.get("/api/clients/{id}", entry.getKey()))
                                        .andExpect(MockMvcResultMatchers.status().isOk())
                                        .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(entry.getKey()))
                                        .andExpect(MockMvcResultMatchers.jsonPath("$.firstName")
                                                        .value(entry.getValue().getFirstName()))
                                        .andExpect(MockMvcResultMatchers.jsonPath("$.lastName")
                                                        .value(entry.getValue().getLastName()));
                        if (entry.getValue().getEmail() != null) {
                                result.andExpect(MockMvcResultMatchers.jsonPath("$.email")
                                                .value(entry.getValue().getEmail()));
                        }
                }

        }

}
