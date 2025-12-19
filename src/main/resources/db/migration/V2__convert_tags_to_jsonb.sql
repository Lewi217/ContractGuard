ALTER TABLE public.contracts
    ALTER COLUMN tags TYPE jsonb
        USING (
        CASE
            WHEN tags IS NULL THEN NULL
            ELSE array_to_json(tags)::jsonb
            END
        );
