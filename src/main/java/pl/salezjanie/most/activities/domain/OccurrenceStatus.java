package pl.salezjanie.most.activities.domain;

/**
 * Lifecycle status of an {@link Occurrence}.
 *
 * <p>Flow: {@code PLANNED → PUBLISHED → COMPLETED/CANCELLED}.
 */
public enum OccurrenceStatus {

    /** Created but not yet visible to participants. */
    PLANNED,

    /** Visible and open for enrollment. */
    PUBLISHED,

    /** Successfully held. */
    COMPLETED,

    /** Will not take place. */
    CANCELLED
}
