package pl.salezjanie.most.structure.domain;

/**
 * Role a {@link pl.salezjanie.most.identity.domain.CommunityMember} can hold
 * within an {@link OrganizationUnit}.
 */
public enum LeadershipRole {

    /** Senior leadership / board member. */
    KADRA,

    /** Leader of a Przęsło (main branch). */
    PRZESLOWY,

    /** Leader of a Podprzęsło (sub-branch / initiative). */
    PODPRZESLOWY
}
