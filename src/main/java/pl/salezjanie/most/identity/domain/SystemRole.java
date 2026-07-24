package pl.salezjanie.most.identity.domain;

/**
 * System-level roles defining global application permissions,
 * independent of the domain organizational structure.
 */
public enum SystemRole {
    /** Global super administrator, bypasses structure checks. */
    ADMIN,

    /** Regular community member. */
    USER
}
