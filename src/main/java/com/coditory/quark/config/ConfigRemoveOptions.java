package com.coditory.quark.config;

public record ConfigRemoveOptions(boolean removeEmptyObjects, boolean removeEmptyLists) {
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

    @Override
    public String toString() {
        return "ConfigRemoveOptions{" +
                "removeEmptyObjects=" + removeEmptyObjects +
                ", removeEmptyLists=" + removeEmptyLists +
                '}';
    }
}