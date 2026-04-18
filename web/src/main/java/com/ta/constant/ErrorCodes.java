package com.ta.constant;

/**
 * MO Iteration 1 unified error codes for backend integration.
 */
public final class ErrorCodes {
    private ErrorCodes() {
    }

    public static final String OK = "OK";
    public static final String UNAUTHORIZED = "UNAUTHORIZED";
    public static final String FORBIDDEN_NOT_OWNER = "FORBIDDEN_NOT_OWNER";
    public static final String VALIDATION_ERROR = "VALIDATION_ERROR";

    public static final String JOB_NOT_FOUND = "JOB_NOT_FOUND";
    public static final String APPLICATION_NOT_FOUND = "APPLICATION_NOT_FOUND";

    public static final String HAS_PENDING_SAME_COURSE = "HAS_PENDING_SAME_COURSE";
    public static final String JOB_NOT_APPROVED = "JOB_NOT_APPROVED";
    public static final String JOB_ALREADY_PUBLISHED = "JOB_ALREADY_PUBLISHED";
    public static final String DEADLINE_LOCKED = "DEADLINE_LOCKED";
    public static final String HAS_APPLICATIONS_CANNOT_WITHDRAW = "HAS_APPLICATIONS_CANNOT_WITHDRAW";
    public static final String JOB_RECRUITMENT_CLOSED = "JOB_RECRUITMENT_CLOSED";
    public static final String FORBIDDEN_ADMIN_ONLY = "FORBIDDEN_ADMIN_ONLY";
}
