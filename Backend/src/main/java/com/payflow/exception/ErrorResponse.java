package com.payflow.exception;

import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {
	private LocalDateTime timestamp;
	private String message;
	private int status;
	private String error;
	private String path;
	
	private Map<String, String> fieldErrors;

}
