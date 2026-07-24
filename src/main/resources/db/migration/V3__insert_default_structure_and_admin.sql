DO $$
DECLARE
    root_unit_id uuid := '00000000-0000-0000-0000-000000000001';
BEGIN
    IF NOT EXISTS (SELECT 1 FROM organization_units WHERE id = root_unit_id) THEN
        INSERT INTO organization_units (id, name, monthly_points_cap)
        VALUES (root_unit_id, 'Zarząd Główny', 4);
    END IF;

    -- Make a@a.pl an ADMIN if it exists, to simplify testing on dev environment
    UPDATE community_members SET system_role = 'ADMIN' WHERE email = 'a@a.pl';

END $$;
