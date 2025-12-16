package com.example.library;

import com.example.library.dto.BorrowRequest;
import com.example.library.dto.LoginRequest;
import com.example.library.repository.BookRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class BorrowFlowIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    BookRepository bookRepository;

    private String token;

    @BeforeEach
    void setUp() throws Exception {
        LoginRequest login = new LoginRequest();
        login.setUsername("analyst");
        login.setPassword("password");
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andReturn();
        token = objectMapper.readTree(loginResult.getResponse().getContentAsString()).get("token").asText();
    }

    @Test
    void borrowAndReturnFlowUpdatesStats() throws Exception {
        UUID bookId = fetchFirstBookId();
        BorrowRequest request = new BorrowRequest();
        request.setBookId(bookId);
        request.setMemberUsername("analyst");

        MvcResult borrowResult = mockMvc.perform(post("/api/borrow")
                        .header("X-Auth-Token", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        UUID recordId = UUID.fromString(objectMapper.readTree(borrowResult.getResponse().getContentAsString()).get("recordId").asText());

        String summaryAfterBorrow = mockMvc.perform(get("/api/stats/summary").header("X-Auth-Token", token))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        assertThat(summaryAfterBorrow).contains("\"activeBorrowings\":1");

        mockMvc.perform(post("/api/borrow/" + recordId + "/return")
                        .header("X-Auth-Token", token))
                .andExpect(status().isOk());

        String summaryAfterReturn = mockMvc.perform(get("/api/stats/summary").header("X-Auth-Token", token))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        assertThat(summaryAfterReturn).contains("\"completedBorrowings\":1");
    }

    private UUID fetchFirstBookId() throws Exception {
        return bookRepository.findAll().stream()
                .findFirst()
                .map(book -> book.getId())
                .orElseThrow();
    }
}
