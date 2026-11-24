package com.example.mlapp;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.example.mlapp.client.ModelServiceClient;
import com.example.mlapp.controller.ModelController;
import com.example.mlapp.dto.Dtos.PredictResponse;

@WebMvcTest(ModelController.class)
class ModelControllerTest {

    @Autowired MockMvc mvc;
    @MockitoBean ModelServiceClient client;

    @Test
    void predictReturnsModelResponse() throws Exception {
        when(client.predict(any())).thenReturn(
                new PredictResponse("setosa", 0, Map.of("setosa", 1.0)));

        mvc.perform(post("/api/model/predict").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"sepal_length":5.1,"sepal_width":3.5,"petal_length":1.4,"petal_width":0.2}
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.label").value("setosa"));
    }

    @Test
    void invalidInputIsRejected() throws Exception {
        mvc.perform(post("/api/model/predict").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sepal_length\":-1}"))
                .andExpect(status().isBadRequest());
    }
}
