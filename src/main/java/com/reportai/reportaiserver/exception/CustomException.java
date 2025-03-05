package com.reportai.reportaiserver.exception;

public class CustomException extends RuntimeException {

    private final ErrorDictionary errorDictionary;

    public CustomException(ErrorDictionary errorDictionary) {
        // A mensagem da superclasse pode ser a descrição do enum
        super(errorDictionary.getDescricao());
        this.errorDictionary = errorDictionary;
    }

    public ErrorDictionary getErrorDictionary() {
        return errorDictionary;
    }
}
