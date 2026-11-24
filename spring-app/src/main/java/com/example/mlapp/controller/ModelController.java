package com.example.mlapp.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.mlapp.client.ModelServiceClient;
import com.example.mlapp.client.ModelServiceClient.ModelServiceException;
import com.example.mlapp.dto.Dtos.ModelInfo;
import com.example.mlapp.dto.Dtos.PredictRequest;
import com.example.mlapp.dto.Dtos.PredictResponse;
import com.example.mlapp.dto.Dtos.TrainRequest;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/model")
public class ModelController {

    private final ModelServiceClient modelService;

    public ModelController(ModelServiceClient modelService) {
        this.modelService = modelService;
    }

    @PostMapping("/predict")
    public PredictResponse predict(@Valid @RequestBody PredictRequest request) {
        return modelService.predict(request);
    }

    @PostMapping("/train")
    public ModelInfo train(@Valid @RequestBody(required = false) TrainRequest request) {
        return modelService.train(request);
    }

    @GetMapping
    public ModelInfo info() {
        return modelService.modelInfo();
    }

    @ExceptionHandler(ModelServiceException.class)
    ProblemDetail handleModelServiceError(ModelServiceException e) {
        var pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_GATEWAY, e.getMessage());
        pd.setTitle("Model service unavailable");
        return pd;
    }
}
