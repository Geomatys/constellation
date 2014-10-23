package org.constellation.api;

/**
 * Different states that a task can take.
 *
 * @author Quentin Boileau (Geomatys)
 */
public enum TaskState {
    PENDING,
    RUNNING,
    SUCCEED,
    FAILED,
    CANCELLED,
    PAUSED
}
