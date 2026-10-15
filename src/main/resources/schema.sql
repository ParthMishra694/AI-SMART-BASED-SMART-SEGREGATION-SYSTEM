-- ====================================================================
-- WasteWise AI — Database Schema (PostgreSQL / Supabase compatible)
-- Aligned with UN Sustainable Development Goal 12
-- ====================================================================

-- 1. Users Table (Citizen Profiles and Authentication)
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    ward VARCHAR(100),
    total_eco_points INT NOT NULL DEFAULT 0,
    streak_days INT NOT NULL DEFAULT 0,
    last_active_date DATE,
    role VARCHAR(30) NOT NULL DEFAULT 'ROLE_USER',
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_users_points ON users(total_eco_points DESC);
CREATE INDEX IF NOT EXISTS idx_users_ward ON users(ward);

-- 2. Waste Analyses Table
CREATE TABLE IF NOT EXISTS waste_analyses (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    item_name VARCHAR(255) NOT NULL,
    predicted_category VARCHAR(100) NOT NULL,
    ai_explanation TEXT NOT NULL,
    disposal_recommendation TEXT NOT NULL,
    reuse_recycle_tips TEXT,
    safety_precautions TEXT,
    confidence_level VARCHAR(50) NOT NULL DEFAULT 'High',
    is_uncertain BOOLEAN NOT NULL DEFAULT FALSE,
    image_path VARCHAR(500),
    agent_execution_trace TEXT,
    demo_fallback_used BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_waste_analyses_user ON waste_analyses(user_id);
CREATE INDEX IF NOT EXISTS idx_waste_analyses_category ON waste_analyses(predicted_category);
CREATE INDEX IF NOT EXISTS idx_waste_analyses_created ON waste_analyses(created_at DESC);

-- 3. Reward Transactions Table (Immutable Audit Ledger)
CREATE TABLE IF NOT EXISTS reward_transactions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    points INT NOT NULL,
    transaction_type VARCHAR(50) NOT NULL,
    reason VARCHAR(255) NOT NULL,
    activity_reference VARCHAR(150) NOT NULL UNIQUE,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_rewards_user ON reward_transactions(user_id);
CREATE INDEX IF NOT EXISTS idx_rewards_activity_ref ON reward_transactions(activity_reference);
CREATE INDEX IF NOT EXISTS idx_rewards_created ON reward_transactions(created_at DESC);

-- 4. Challenges Table
CREATE TABLE IF NOT EXISTS challenges (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(150) NOT NULL,
    description TEXT NOT NULL,
    category VARCHAR(50) NOT NULL,
    target_count INT NOT NULL DEFAULT 1,
    point_reward INT NOT NULL DEFAULT 25,
    start_date DATE,
    end_date DATE,
    icon VARCHAR(50) DEFAULT 'bi-trophy',
    active BOOLEAN NOT NULL DEFAULT TRUE
);

-- 5. User Challenges Progress Table
CREATE TABLE IF NOT EXISTS user_challenges (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    challenge_id BIGINT NOT NULL REFERENCES challenges(id) ON DELETE CASCADE,
    current_progress INT NOT NULL DEFAULT 0,
    completed BOOLEAN NOT NULL DEFAULT FALSE,
    completed_at TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT uq_user_challenge UNIQUE (user_id, challenge_id)
);

CREATE INDEX IF NOT EXISTS idx_user_challenges_user ON user_challenges(user_id);

-- 6. Recycling Submissions Table (Drop-off Verification)
CREATE TABLE IF NOT EXISTS recycling_submissions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title VARCHAR(150) NOT NULL,
    material_type VARCHAR(100) NOT NULL,
    estimated_weight_kg DOUBLE PRECISION,
    description TEXT NOT NULL,
    photo_path VARCHAR(500),
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    admin_notes TEXT,
    points_awarded INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW(),
    reviewed_at TIMESTAMP WITHOUT TIME ZONE
);

CREATE INDEX IF NOT EXISTS idx_recycling_status ON recycling_submissions(status);
CREATE INDEX IF NOT EXISTS idx_recycling_user ON recycling_submissions(user_id);

-- 7. Quiz Attempts Table
CREATE TABLE IF NOT EXISTS quiz_attempts (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    quiz_title VARCHAR(150),
    score INT NOT NULL,
    total_questions INT NOT NULL,
    passed BOOLEAN NOT NULL,
    points_awarded INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_quiz_user ON quiz_attempts(user_id);
