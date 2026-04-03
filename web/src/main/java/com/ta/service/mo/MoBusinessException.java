package com.ta.service.mo;

/**
 * Runtime exception used for MO business rule violations.
 */
public class MoBusinessException extends RuntimeException {
    private final String code;
    private final int httpStatus;

    public MoBusinessException(String code, String message, int httpStatus) {
        super(message);
        this.code = code;
        this.httpStatus = httpStatus;
    }

    public String getCode() {
        return code;
    }

    public int getHttpStatus() {
        return httpStatus;
    }
}
