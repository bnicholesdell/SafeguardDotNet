package com.oneidentity.safeguard.safeguardclient.exceptions;

public class RestClientException extends Exception {

    @Override
    public String getLocalizedMessage() {
        return getMessage();
    }

    @Override
    public String getMessage() {
        String retVal = "";
        if (null != super.getMessage()) {
            retVal = super.getMessage();
        }
        return retVal;
    }
    private static final long serialVersionUID = 1L;
    private int status = 500;

    public RestClientException() {
        super();
    }

    public RestClientException(Exception cause, int status) {
        super(cause);
        this.status = status;
    }

    public RestClientException(String msg, int status) {
        super(msg);
        this.status = status;
    }
    
    public RestClientException(String msg, Exception cause, int status) {
        super(msg, cause);
        this.status = status;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

}
