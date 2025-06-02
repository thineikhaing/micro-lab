package org.tek.microlab.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse<T> {
	
    private String code;
    private String message;
    private T data;

    public ErrorResponse(String code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

}
