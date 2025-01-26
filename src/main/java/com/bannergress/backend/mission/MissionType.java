package com.bannergress.backend.mission;

/**
 * Mission type.
 */
public enum MissionType {
    /**
     * Mission steps have to be followed in order.
     */
    sequential,
    /**
     * Mission steps can be performed in any order.
     */
    anyOrder,
    /**
     * Mission steps are hidden.
     */
    hidden
}
