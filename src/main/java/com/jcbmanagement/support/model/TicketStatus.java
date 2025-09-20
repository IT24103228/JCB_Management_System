package com.jcbmanagement.support.model;

public enum TicketStatus {
    OPEN("open"),
    CLOSED("closed"),
    IN_PROGRESS("in progress"),
    SOLVED("solved");
    
    private final String value;
    
    TicketStatus(String value) {
        this.value = value;
    }
    
    public String getValue() {
        return value;
    }
    
    @Override
    public String toString() {
        return value;
    }
}
