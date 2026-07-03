# Add SQL Migration

## When to use
When adding or changing a database schema: new table, new column, new index, or constraint change.

## Steps

1. **Determine the next version number**
   ```bash
   ls src/main/resources/db/migration/
   # Current highest: V4__ → next is V5__
   ```

2. **Create the migration file**
   - Path: `src/main/resources/db/migration/V{n}__{snake_case_description}.sql`
   - Example: `V5__create_widget.sql`

3. **Write the SQL** — rules:
   - Always qualify tables with `general.` schema
   - UUID primary keys: `UUID NOT NULL DEFAULT gen_random_uuid()`
   - Timestamps: `TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()`
   - Use `CREATE TABLE IF NOT EXISTS` / `ALTER TABLE IF EXISTS`
   - Add `CREATE INDEX IF NOT EXISTS idx_{table}_{column}` for every FK and UUID lookup column
   - Never use `SERIAL` / `BIGSERIAL` — UUIDs only
   - Append-only tables: add a comment `-- append-only: no UPDATE or DELETE`

4. **Migration template**
   ```sql
   -- V5__create_widget.sql
   CREATE TABLE IF NOT EXISTS general.widget (
       id              UUID                        NOT NULL DEFAULT gen_random_uuid(),
       application_id  UUID                        NOT NULL,
       name            VARCHAR(128)                NOT NULL,
       active          BOOLEAN                     NOT NULL DEFAULT TRUE,
       created_at      TIMESTAMP WITH TIME ZONE    NOT NULL DEFAULT NOW(),
       updated_at      TIMESTAMP WITH TIME ZONE    NOT NULL DEFAULT NOW(),
       CONSTRAINT pk_widget PRIMARY KEY (id),
       CONSTRAINT fk_widget_application FOREIGN KEY (application_id)
           REFERENCES general.application(id)
   );

   CREATE INDEX IF NOT EXISTS idx_widget_application_id ON general.widget (application_id);
   ```

5. **Create or update the JPA entity** in `com.umdc.backoffice.jpa.domain`
   - `@Entity`, `@Table(schema = "general", name = "widget")`
   - `@Id UUID id` — caller-generated
   - `@PrePersist` sets `createdAt` / `id` if null
   - `updatable = false` on immutable columns

6. **Create the Spring Data repository** in `com.umdc.backoffice.jpa.repository`
   - Extends `JpaRepository<WidgetEntity, UUID>`
   - Scope all queries by `applicationId`

7. **Verify against PostgreSQL** (not H2 — H2 doesn't support `jsonb` or `gen_random_uuid()` the same way)
   ```bash
   # Apply via Flyway CLI or psql
   psql "${DB_URL}" -f src/main/resources/db/migration/V5__create_widget.sql
   ```

## Checklist
- [ ] Version number is sequential (no gaps, no duplicates)
- [ ] File name is `V{n}__{description}.sql` with double underscore
- [ ] All tables use `general.` schema prefix
- [ ] UUID PK with `DEFAULT gen_random_uuid()`
- [ ] `IF NOT EXISTS` on all DDL statements
- [ ] Index created for every FK column
- [ ] JPA entity created / updated in `com.umdc.backoffice.jpa.domain`
- [ ] Repository created / updated in `com.umdc.backoffice.jpa.repository`
- [ ] Migration tested against PostgreSQL (not just H2)
- [ ] Never edited a previously merged migration — created a new one instead
