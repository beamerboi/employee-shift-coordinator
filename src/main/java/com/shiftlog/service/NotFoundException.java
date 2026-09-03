package com.shiftlog.service;

public class NotFoundException extends RuntimeException {

    public NotFoundException(String resource, String id) {
        super(resource + " " + id + " was not found");
    }
}
