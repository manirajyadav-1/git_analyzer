-- Database initialization script for Code Archeologist Spring Boot
-- This script ensures the required PostgreSQL extensions are installed

-- Create extensions for vector operations
CREATE EXTENSION IF NOT EXISTS vector;

CREATE TABLE IF NOT EXISTS code_analysis (
    id BIGSERIAL PRIMARY KEY,
    session_id TEXT NOT NULL,
    repo_url TEXT NOT NULL,
    status TEXT NOT NULL,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    commits JSONB DEFAULT '{}'::jsonb,
    file_changes JSONB DEFAULT '{}'::jsonb,
    contributors JSONB DEFAULT '{}'::jsonb,
    commit_activity JSONB DEFAULT '{}'::jsonb,
    dependencies JSONB DEFAULT '{}'::jsonb,
    issues JSONB DEFAULT '{}'::jsonb,
    UNIQUE(session_id, repo_url)
);

CREATE TABLE IF NOT EXISTS commit_embeddings (
    id BIGSERIAL PRIMARY KEY,
    code_analysis_id BIGINT REFERENCES code_analysis(id),
    commit_hash TEXT NOT NULL,
    commit_message TEXT NOT NULL,
    embedding VECTOR(768),
    created_at TIMESTAMPTZ DEFAULT NOW(),
    UNIQUE(code_analysis_id, commit_hash)
);

-- Index for fast lookup on code_analysis_id
CREATE INDEX IF NOT EXISTS idx_commit_embeddings_analysis ON commit_embeddings (code_analysis_id);

-- Create vector similarity search index using diskann
DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM pg_indexes WHERE indexname = 'commit_embeddings_idx'
  ) THEN
    CREATE INDEX commit_embeddings_idx
    ON commit_embeddings USING diskann (embedding);
  END IF;
END$$;
