package com.youou.aso.common.error;

public class BusinessException extends RuntimeException {
    private final ErrorCode errorCode;
    private final Object args;

    public BusinessException(ErrorCode errorCode) {
        this(errorCode, null);
    }

    public BusinessException(ErrorCode errorCode, Object args) {
        super(errorCode.name());
        this.errorCode = errorCode;
        this.args = args;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public Object getArgs() {
        return args;
    }
}
