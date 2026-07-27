--
-- PostgreSQL database dump
--

-- Dumped from database version 17.10
-- Dumped by pg_dump version 17.10

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: attendances; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.attendances (
    recorded_at timestamp(6) with time zone NOT NULL,
    id uuid NOT NULL,
    member_id uuid NOT NULL,
    occurrence_id uuid NOT NULL
);


--
-- Name: community_members; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.community_members (
    created_at timestamp(6) with time zone NOT NULL,
    updated_at timestamp(6) with time zone NOT NULL,
    id uuid NOT NULL,
    status character varying(20) NOT NULL,
    display_name character varying(255) NOT NULL,
    email character varying(255) NOT NULL,
    password_hash character varying(255) NOT NULL,
    CONSTRAINT community_members_status_check CHECK (((status)::text = ANY ((ARRAY['PENDING'::character varying, 'ACTIVE'::character varying, 'SUSPENDED'::character varying])::text[])))
);


--
-- Name: device_tokens; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.device_tokens (
    created_at timestamp(6) with time zone NOT NULL,
    id uuid NOT NULL,
    member_id uuid NOT NULL,
    token character varying(255) NOT NULL
);


--
-- Name: enrollments; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.enrollments (
    enrolled_at timestamp(6) with time zone NOT NULL,
    id uuid NOT NULL,
    member_id uuid NOT NULL,
    occurrence_id uuid NOT NULL,
    status character varying(20) NOT NULL,
    CONSTRAINT enrollments_status_check CHECK (((status)::text = ANY ((ARRAY['ENROLLED'::character varying, 'WITHDRAWN'::character varying, 'CANCELLED'::character varying])::text[])))
);


--
-- Name: initiatives; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.initiatives (
    default_points integer NOT NULL,
    id uuid NOT NULL,
    owner_unit_id uuid NOT NULL,
    description character varying(255),
    name character varying(255) NOT NULL
);


--
-- Name: leadership_assignments; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.leadership_assignments (
    end_date date,
    start_date date NOT NULL,
    id uuid NOT NULL,
    member_id uuid NOT NULL,
    unit_id uuid NOT NULL,
    role character varying(20) NOT NULL,
    CONSTRAINT leadership_assignments_role_check CHECK (((role)::text = ANY ((ARRAY['KADRA'::character varying, 'PRZESLOWY'::character varying, 'PODPRZESLOWY'::character varying])::text[])))
);


--
-- Name: notifications; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.notifications (
    is_read boolean NOT NULL,
    created_at timestamp(6) with time zone NOT NULL,
    id uuid NOT NULL,
    member_id uuid NOT NULL,
    content character varying(1000) NOT NULL,
    title character varying(255) NOT NULL
);


--
-- Name: occurrence_reschedule_log; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.occurrence_reschedule_log (
    new_start timestamp(6) without time zone NOT NULL,
    old_start timestamp(6) without time zone NOT NULL,
    rescheduled_at timestamp(6) with time zone NOT NULL,
    occurrence_id uuid NOT NULL,
    reason character varying(255) NOT NULL
);


--
-- Name: occurrences; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.occurrences (
    capacity integer,
    scheduled_end timestamp(6) without time zone NOT NULL,
    scheduled_start timestamp(6) without time zone NOT NULL,
    id uuid NOT NULL,
    initiative_id uuid NOT NULL,
    status character varying(20) NOT NULL,
    CONSTRAINT occurrences_status_check CHECK (((status)::text = ANY ((ARRAY['PLANNED'::character varying, 'PUBLISHED'::character varying, 'COMPLETED'::character varying, 'CANCELLED'::character varying])::text[])))
);


--
-- Name: organization_units; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.organization_units (
    monthly_points_cap integer,
    id uuid NOT NULL,
    parent_unit_id uuid,
    name character varying(255) NOT NULL
);


--
-- Name: points_transactions; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.points_transactions (
    points integer NOT NULL,
    "timestamp" timestamp(6) with time zone NOT NULL,
    id uuid NOT NULL,
    member_id uuid NOT NULL,
    occurrence_id uuid,
    unit_id uuid NOT NULL,
    reason character varying(255) NOT NULL
);


--
-- Name: schedule_rules; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.schedule_rules (
    duration_minutes integer NOT NULL,
    effective_from date NOT NULL,
    effective_until date,
    start_time time(0) without time zone NOT NULL,
    day_of_week character varying(10) NOT NULL,
    id uuid NOT NULL,
    initiative_id uuid NOT NULL,
    recurrence_type character varying(20) NOT NULL,
    CONSTRAINT schedule_rules_day_of_week_check CHECK (((day_of_week)::text = ANY ((ARRAY['MONDAY'::character varying, 'TUESDAY'::character varying, 'WEDNESDAY'::character varying, 'THURSDAY'::character varying, 'FRIDAY'::character varying, 'SATURDAY'::character varying, 'SUNDAY'::character varying])::text[]))),
    CONSTRAINT schedule_rules_recurrence_type_check CHECK (((recurrence_type)::text = ANY ((ARRAY['WEEKLY'::character varying, 'BIWEEKLY'::character varying])::text[])))
);


--
-- Name: subscriptions; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.subscriptions (
    created_at timestamp(6) with time zone NOT NULL,
    id uuid NOT NULL,
    initiative_id uuid NOT NULL,
    member_id uuid NOT NULL
);


--
-- Name: attendances attendances_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.attendances
    ADD CONSTRAINT attendances_pkey PRIMARY KEY (id);


--
-- Name: community_members community_members_email_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.community_members
    ADD CONSTRAINT community_members_email_key UNIQUE (email);


--
-- Name: community_members community_members_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.community_members
    ADD CONSTRAINT community_members_pkey PRIMARY KEY (id);


--
-- Name: device_tokens device_tokens_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.device_tokens
    ADD CONSTRAINT device_tokens_pkey PRIMARY KEY (id);


--
-- Name: device_tokens device_tokens_token_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.device_tokens
    ADD CONSTRAINT device_tokens_token_key UNIQUE (token);


--
-- Name: enrollments enrollments_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.enrollments
    ADD CONSTRAINT enrollments_pkey PRIMARY KEY (id);


--
-- Name: initiatives initiatives_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.initiatives
    ADD CONSTRAINT initiatives_pkey PRIMARY KEY (id);


--
-- Name: leadership_assignments leadership_assignments_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.leadership_assignments
    ADD CONSTRAINT leadership_assignments_pkey PRIMARY KEY (id);


--
-- Name: notifications notifications_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.notifications
    ADD CONSTRAINT notifications_pkey PRIMARY KEY (id);


--
-- Name: occurrences occurrences_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.occurrences
    ADD CONSTRAINT occurrences_pkey PRIMARY KEY (id);


--
-- Name: organization_units organization_units_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.organization_units
    ADD CONSTRAINT organization_units_pkey PRIMARY KEY (id);


--
-- Name: points_transactions points_transactions_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.points_transactions
    ADD CONSTRAINT points_transactions_pkey PRIMARY KEY (id);


--
-- Name: schedule_rules schedule_rules_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.schedule_rules
    ADD CONSTRAINT schedule_rules_pkey PRIMARY KEY (id);


--
-- Name: subscriptions subscriptions_member_id_initiative_id_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.subscriptions
    ADD CONSTRAINT subscriptions_member_id_initiative_id_key UNIQUE (member_id, initiative_id);


--
-- Name: subscriptions subscriptions_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.subscriptions
    ADD CONSTRAINT subscriptions_pkey PRIMARY KEY (id);


--
-- Name: subscriptions ukoggadjh4hy70l3dma8uxi1mwq; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.subscriptions
    ADD CONSTRAINT ukoggadjh4hy70l3dma8uxi1mwq UNIQUE (member_id, initiative_id);


--
-- Name: occurrence_reschedule_log fk80owj8ruax5gvrs44fidjfrr6; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.occurrence_reschedule_log
    ADD CONSTRAINT fk80owj8ruax5gvrs44fidjfrr6 FOREIGN KEY (occurrence_id) REFERENCES public.occurrences(id);


--
-- Name: schedule_rules fkdcpgfrhpxmt24sr766e8l08mc; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.schedule_rules
    ADD CONSTRAINT fkdcpgfrhpxmt24sr766e8l08mc FOREIGN KEY (initiative_id) REFERENCES public.initiatives(id);


--
-- Name: leadership_assignments fkddelos6t9sircn612jfro2s44; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.leadership_assignments
    ADD CONSTRAINT fkddelos6t9sircn612jfro2s44 FOREIGN KEY (unit_id) REFERENCES public.organization_units(id);


--
-- PostgreSQL database dump complete
--

