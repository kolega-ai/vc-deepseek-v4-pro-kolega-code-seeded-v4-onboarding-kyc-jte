-- Postgres-only guard: the audit log is append-only. No role, endpoint or export may ever
-- update or delete an audit row; this trigger makes that a database-enforced invariant.

CREATE OR REPLACE FUNCTION prevent_audit_event_mutation() RETURNS trigger AS $$
BEGIN
    RAISE EXCEPTION 'audit_event is append-only; % operations are forbidden', TG_OP;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER audit_event_append_only
    BEFORE UPDATE OR DELETE ON audit_event
    FOR EACH ROW
    EXECUTE FUNCTION prevent_audit_event_mutation();
