ALTER TABLE public.schedule_rules DROP CONSTRAINT schedule_rules_recurrence_type_check;

ALTER TABLE public.schedule_rules ADD CONSTRAINT schedule_rules_recurrence_type_check 
CHECK (((recurrence_type)::text = ANY ((ARRAY['NONE', 'DAILY_WEEKDAYS', 'WEEKLY', 'BIWEEKLY', 'TRIWEEKLY'])::text[])));
