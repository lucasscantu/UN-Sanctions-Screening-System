-- Initial schema creation for UN Sanctions Screening System
-- This migration creates all the necessary tables for the application

CREATE TYPE record_type_enum AS ENUM ('INDIVIDUAL', 'ENTITY');
CREATE TYPE alias_quality_enum AS ENUM ('GOOD', 'LOW', 'UNKNOWN');
CREATE TYPE date_precision_enum AS ENUM ('EXACT', 'APPROXIMATE', 'YEAR_ONLY', 'MONTH_AND_YEAR', 'UNKNOWN');

-- Main sanctioned records table
CREATE TABLE sanctioned_records (
    id BIGSERIAL PRIMARY KEY,
    un_reference_number VARCHAR(200) UNIQUE NOT NULL,
    record_type record_type_enum NOT NULL,
    primary_name VARCHAR(1000) NOT NULL,
    primary_name_normalized VARCHAR(1000),
    submitted_name VARCHAR(1000),
    sort_key VARCHAR(1000),
    designation VARCHAR(2000),
    listed_on DATE,
    source_url VARCHAR(2000),
    first_imported_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    last_synced_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    is_active BOOLEAN DEFAULT TRUE NOT NULL,
    source_status VARCHAR(100),
    raw_xml_data TEXT
);

-- Indexes for sanctioned_records
CREATE INDEX idx_sanctioned_records_un_reference ON sanctioned_records(un_reference_number);
CREATE INDEX idx_sanctioned_records_record_type ON sanctioned_records(record_type);
CREATE INDEX idx_sanctioned_records_is_active ON sanctioned_records(is_active);
CREATE INDEX idx_sanctioned_records_primary_name ON sanctioned_records(primary_name);
CREATE INDEX idx_sanctioned_records_primary_name_normalized ON sanctioned_records(primary_name_normalized);
CREATE INDEX idx_sanctioned_records_listed_on ON sanctioned_records(listed_on);
CREATE INDEX idx_sanctioned_records_last_synced ON sanctioned_records(last_synced_at);

-- Name aliases table
CREATE TABLE name_aliases (
    id BIGSERIAL PRIMARY KEY,
    sanctioned_record_id BIGINT NOT NULL,
    alias_name VARCHAR(1000) NOT NULL,
    alias_name_normalized VARCHAR(1000),
    quality alias_quality_enum,
    name_origin_desc VARCHAR(500),
    is_primary_alias BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (sanctioned_record_id) REFERENCES sanctioned_records(id) ON DELETE CASCADE
);

-- Indexes for name_aliases
CREATE INDEX idx_name_aliases_record_id ON name_aliases(sanctioned_record_id);
CREATE INDEX idx_name_aliases_name ON name_aliases(alias_name);
CREATE INDEX idx_name_aliases_name_normalized ON name_aliases(alias_name_normalized);
CREATE INDEX idx_name_aliases_quality ON name_aliases(quality);

-- Biographical details table
CREATE TABLE biographical_details (
    id BIGSERIAL PRIMARY KEY,
    sanctioned_record_id BIGINT NOT NULL,
    date_of_birth DATE,
    date_precision date_precision_enum,
    place_of_birth VARCHAR(1000),
    city_of_birth VARCHAR(500),
    country_of_birth VARCHAR(200),
    nationality VARCHAR(500),
    gender VARCHAR(50),
    title VARCHAR(100),
    position VARCHAR(500),
    function VARCHAR(500),
    FOREIGN KEY (sanctioned_record_id) REFERENCES sanctioned_records(id) ON DELETE CASCADE
);

-- Indexes for biographical_details
CREATE INDEX idx_biographical_details_record_id ON biographical_details(sanctioned_record_id);
CREATE INDEX idx_biographical_details_dob ON biographical_details(date_of_birth);
CREATE INDEX idx_biographical_details_nationality ON biographical_details(nationality);
CREATE INDEX idx_biographical_details_country_of_birth ON biographical_details(country_of_birth);

-- Sanctions programs table
CREATE TABLE sanctions_programs (
    id BIGSERIAL PRIMARY KEY,
    sanctioned_record_id BIGINT NOT NULL,
    committee VARCHAR(100) NOT NULL,
    program_name VARCHAR(500),
    committee_url VARCHAR(2000),
    listing_type VARCHAR(200),
    other_information TEXT,
    FOREIGN KEY (sanctioned_record_id) REFERENCES sanctioned_records(id) ON DELETE CASCADE
);

-- Indexes for sanctions_programs
CREATE INDEX idx_sanctions_programs_record_id ON sanctions_programs(sanctioned_record_id);
CREATE INDEX idx_sanctions_programs_committee ON sanctions_programs(committee);

-- Addresses table
CREATE TABLE addresses (
    id BIGSERIAL PRIMARY KEY,
    sanctioned_record_id BIGINT NOT NULL,
    street VARCHAR(1000),
    city VARCHAR(500),
    state_or_province VARCHAR(500),
    postal_code VARCHAR(50),
    country VARCHAR(200),
    address_type VARCHAR(100),
    is_primary BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (sanctioned_record_id) REFERENCES sanctioned_records(id) ON DELETE CASCADE
);

-- Indexes for addresses
CREATE INDEX idx_addresses_record_id ON addresses(sanctioned_record_id);
CREATE INDEX idx_addresses_country ON addresses(country);
CREATE INDEX idx_addresses_city ON addresses(city);

-- Identifiers table
CREATE TABLE identifiers (
    id BIGSERIAL PRIMARY KEY,
    sanctioned_record_id BIGINT NOT NULL,
    identifier_type VARCHAR(100),
    identifier_number VARCHAR(200),
    issuing_country VARCHAR(200),
    issuing_authority VARCHAR(500),
    expiration_date VARCHAR(50),
    notes VARCHAR(1000),
    FOREIGN KEY (sanctioned_record_id) REFERENCES sanctioned_records(id) ON DELETE CASCADE
);

-- Indexes for identifiers
CREATE INDEX idx_identifiers_record_id ON identifiers(sanctioned_record_id);
CREATE INDEX idx_identifiers_type ON identifiers(identifier_type);
CREATE INDEX idx_identifiers_number ON identifiers(identifier_number);

-- Import jobs table
CREATE TABLE import_jobs (
    id BIGSERIAL PRIMARY KEY,
    job_uuid VARCHAR(50) UNIQUE NOT NULL,
    source_url VARCHAR(2000) NOT NULL,
    status VARCHAR(50) NOT NULL,
    started_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    completed_at TIMESTAMP WITH TIME ZONE,
    records_created INTEGER DEFAULT 0,
    records_updated INTEGER DEFAULT 0,
    records_deactivated INTEGER DEFAULT 0,
    records_failed INTEGER DEFAULT 0,
    total_records_processed INTEGER DEFAULT 0,
    error_summary TEXT,
    file_size_bytes BIGINT,
    file_checksum VARCHAR(100),
    is_manual_trigger BOOLEAN DEFAULT FALSE NOT NULL,
    triggered_by VARCHAR(100)
);

-- Indexes for import_jobs
CREATE INDEX idx_import_jobs_uuid ON import_jobs(job_uuid);
CREATE INDEX idx_import_jobs_status ON import_jobs(status);
CREATE INDEX idx_import_jobs_started_at ON import_jobs(started_at);
CREATE INDEX idx_import_jobs_completed_at ON import_jobs(completed_at);

-- Search history table
CREATE TABLE search_history (
    id BIGSERIAL PRIMARY KEY,
    search_query VARCHAR(1000) NOT NULL,
    search_query_normalized VARCHAR(1000),
    record_type_filter VARCHAR(20),
    results_count INTEGER DEFAULT 0,
    min_score_filter DOUBLE PRECISION,
    search_duration_ms BIGINT,
    ai_analysis_enabled BOOLEAN DEFAULT FALSE,
    ai_analysis_duration_ms BIGINT,
    user_agent VARCHAR(500),
    ip_address VARCHAR(50),
    session_id VARCHAR(100),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    user_id VARCHAR(100)
);

-- Indexes for search_history
CREATE INDEX idx_search_history_user_id ON search_history(user_id);
CREATE INDEX idx_search_history_created_at ON search_history(created_at);
CREATE INDEX idx_search_history_query ON search_history(search_query);
CREATE INDEX idx_search_history_session ON search_history(session_id);

-- Full-text search index for better name matching
CREATE INDEX idx_sanctioned_records_fts ON sanctioned_records USING GIN (
    to_tsvector('english', COALESCE(primary_name, '') || ' ' || COALESCE(submitted_name, '') || ' ' || COALESCE(sort_key, ''))
);

-- Full-text search index for aliases
CREATE INDEX idx_name_aliases_fts ON name_aliases USING GIN (
    to_tsvector('english', COALESCE(alias_name, ''))
);

-- Create a materialized view for fast similarity searches (optional, can be refreshed periodically)
CREATE MATERIALIZED VIEW IF NOT EXISTS searchable_names AS
SELECT 
    sr.id as record_id,
    sr.un_reference_number,
    sr.record_type,
    sr.primary_name,
    sr.primary_name_normalized,
    na.alias_name,
    na.alias_name_normalized,
    na.quality as alias_quality
FROM sanctioned_records sr
LEFT JOIN name_aliases na ON sr.id = na.sanctioned_record_id
WHERE sr.is_active = true;

-- Index for the materialized view
CREATE INDEX IF NOT EXISTS idx_searchable_names_record_id ON searchable_names(record_id);
CREATE INDEX IF NOT EXISTS idx_searchable_names_name ON searchable_names(alias_name);
CREATE INDEX IF NOT EXISTS idx_searchable_names_normalized ON searchable_names(alias_name_normalized);

-- Create a function to refresh the materialized view
CREATE OR REPLACE FUNCTION refresh_searchable_names()
RETURNS TRIGGER AS $$
BEGIN
    REFRESH MATERIALIZED VIEW searchable_names;
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

-- Create triggers to refresh the materialized view when data changes
DROP TRIGGER IF EXISTS refresh_searchable_names_trigger ON sanctioned_records;
CREATE TRIGGER refresh_searchable_names_trigger
    AFTER INSERT OR UPDATE OR DELETE ON sanctioned_records
    FOR EACH STATEMENT
    EXECUTE FUNCTION refresh_searchable_names();

DROP TRIGGER IF EXISTS refresh_searchable_names_alias_trigger ON name_aliases;
CREATE TRIGGER refresh_searchable_names_alias_trigger
    AFTER INSERT OR UPDATE OR DELETE ON name_aliases
    FOR EACH STATEMENT
    EXECUTE FUNCTION refresh_searchable_names();
