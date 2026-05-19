package com.qataskmanager.automation_sut.exception;

import java.util.Map;

public class TestDataImportException extends IllegalArgumentException {
    private final Map<String, String> validationErrors;

    public TestDataImportException(String message, Map<String, String> validationErrors) {
        super(message);
        this.validationErrors = validationErrors;
    }

    public Map<String, String> getValidationErrors() {
        return validationErrors;
    }
}
