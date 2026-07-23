package pl.salezjanie.most.participation.domain;

/**
 * Lifecycle status of an {@link Enrollment}.
 */
public enum EnrollmentStatus {

    /** Member is actively enrolled for the occurrence. */
    ENROLLED,

    /** Member voluntarily withdrew before the occurrence started. */
    WITHDRAWN,

    /** Enrollment was cancelled (e.g. occurrence was cancelled). */
    CANCELLED
}
