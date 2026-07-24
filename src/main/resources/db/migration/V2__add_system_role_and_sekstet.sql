ALTER TABLE public.community_members 
    ADD COLUMN system_role character varying(20) NOT NULL DEFAULT 'USER';
    
ALTER TABLE public.community_members 
    ADD CONSTRAINT community_members_system_role_check 
    CHECK (((system_role)::text = ANY ((ARRAY['ADMIN'::character varying, 'USER'::character varying])::text[])));

ALTER TABLE public.leadership_assignments DROP CONSTRAINT leadership_assignments_role_check;

-- Update existing KADRA to SEKSTET
UPDATE public.leadership_assignments SET role = 'SEKSTET' WHERE role = 'KADRA';

ALTER TABLE public.leadership_assignments 
    ADD CONSTRAINT leadership_assignments_role_check 
    CHECK (((role)::text = ANY ((ARRAY['SEKSTET'::character varying, 'PRZESLOWY'::character varying, 'PODPRZESLOWY'::character varying])::text[])));
