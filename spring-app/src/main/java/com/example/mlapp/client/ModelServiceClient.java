package com.example.mlapp.client;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.example.mlapp.dto.Dtos.ModelInfo;
import com.example.mlapp.dto.Dtos.PredictRequest;
import com.example.mlapp.dto.Dtos.PredictResponse;
import com.example.mlapp.dto.Dtos.TrainRequest;

/** Thin HTTP client for the Python FastAPI model service. */
@Component
public class ModelServiceClient {

    private final RestClient client;

    public ModelServiceClient(RestClient modelRestClient) {
        this.client = modelRestClient;
    }

    public PredictResponse predict(PredictRequest request) {
        return call(() -> client.post().uri("/predict")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(PredictResponse.class));
    }

    public ModelInfo train(TrainRequest request) {
        return call(() -> client.post().uri("/train")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request == null ? new TrainRequest(null, null) : request)
                .retrieve()
                .body(ModelInfo.class));
    }

    public ModelInfo modelInfo() {
        return call(() -> client.get().uri("/model").retrieve().body(ModelInfo.class));
    }

    private <T> T call(java.util.function.Supplier<T> s) {
        try {
            return s.get();
        } catch (RestClientException e) {
            throw new ModelServiceException("Model service call failed: " + e.getMessage(), e);
        }
    }

    public static class ModelServiceException extends RuntimeException {
        public ModelServiceException(String msg, Throwable cause) { super(msg, cause); }
    }
}
