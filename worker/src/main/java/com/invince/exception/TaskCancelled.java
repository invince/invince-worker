package com.invince.exception;

import lombok.Getter;

public class TaskCancelled extends RuntimeException {

    @Getter
    private String key;

    public TaskCancelled(String key) {
        this.key = key;
    }
}
