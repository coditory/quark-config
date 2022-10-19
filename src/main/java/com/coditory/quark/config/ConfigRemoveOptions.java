package com.coditory.quark.config;

public final class ConfigRemoveOptions {
    private final boolean removeEmptyObjects;
    private final boolean removeEmptyLists;

    public static ConfigRemoveOptions removeEmptyParents() {
        return new ConfigRemoveOptions(true, true);
    }

    public static ConfigRemoveOptions removeEmptyParentObjects() {
        return new ConfigRemoveOptions(true, false);
    }

    public static ConfigRemoveOptions removeEmptyParentLists() {
        return new ConfigRemoveOptions(false, true);
    }

    public static ConfigRemoveOptions leaveEmptyParents() {
        return new ConfigRemoveOptions(false, false);
    }

    public ConfigRemoveOptions(boolean removeEmptyObjects, boolean removeEmptyLists) {
        this.removeEmptyObjects = removeEmptyObjects;
        this.removeEmptyLists = removeEmptyLists;
    }

    public boolean isRemoveEmptyObjects() {
        return removeEmptyObjects;
    }

    public boolean isRemoveEmptyLists() {
        return removeEmptyLists;
    }

    @Override
    public String toString() {
        return "ConfigRemoveOptions{" +
                "removeEmptyObjects=" + removeEmptyObjects +
                ", removeEmptyLists=" + removeEmptyLists +
                '}';
    }
}