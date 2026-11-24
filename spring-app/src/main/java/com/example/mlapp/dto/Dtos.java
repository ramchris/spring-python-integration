package com.example.mlapp.dto;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/** Request/response records mirroring the FastAPI schemas (snake_case on the wire). */
public final class Dtos {
    private Dtos() {}

    public record PredictRequest(
            @NotNull @Positive @JsonProperty("sepal_length") Double sepalLength,
            @NotNull @Positive @JsonProperty("sepal_width") Double sepalWidth,
            @NotNull @Positive @JsonProperty("petal_length") Double petalLength,
            @NotNull @Positive @JsonProperty("petal_width") Double petalWidth) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record PredictResponse(
            String label,
            @JsonProperty("class_index") int classIndex,
            Map<String, Double> probabilities) {}

    // NON_NULL: omitted fields must be absent (not null) so FastAPI applies its defaults.
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record TrainRequest(
            @Min(1) @Max(1000) @JsonProperty("n_estimators") Integer nEstimators,
            @Min(1) @Max(50) @JsonProperty("max_depth") Integer maxDepth) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ModelInfo(
            String model,
            String dataset,
            List<String> features,
            List<String> classes,
            @JsonProperty("n_estimators") Integer nEstimators,
            @JsonProperty("max_depth") Integer maxDepth,
            @JsonProperty("test_accuracy") Double testAccuracy,
            @JsonProperty("trained_at") String trainedAt) {}
}
