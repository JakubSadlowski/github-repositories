package org.js.githubrepos.api.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Error {
    @Schema(description = "Type of error, e.g., BAD_REQUEST or NOT_FOUND", example = "BAD_REQUEST, NOT_FOUND")
    private String code;

    @Schema(description = "Detailed error message", example = "The provided request is invalid.")
    private String description;

    public static Error of(String errorType, String message) {
        return new Error(errorType, message);
    }
}
