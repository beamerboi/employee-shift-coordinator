package com.shiftlog.service;

public class NotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public NotFoundException(String resource, String id) {
        super(resource + " " + id + " was not found");
    }
}
