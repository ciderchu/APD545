package com.minifin.model;

public class Category {
    private final long id;
    private final String name;
    private final String type;
    private final long userId;

    public Category(long id, String name, String type, long userId) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.userId = userId;
    }
    
    public Category(String name, String type, long userId) {
        this(-1, name, type, userId);
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public long getUserId() {
        return userId;
    }
    
    @Override
    public String toString() {
        return name;
    }
}