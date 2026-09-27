-- ============================================================================
-- V4: Agregar campos de bono de bienvenida y recarga de prueba
-- ============================================================================
-- allow_test_recharge: Permiso para recarga ficticia (solo activable desde BD por admin)
-- welcome_bonus_claimed: Indica si el usuario ya recibió su bono de $1 USDC
-- ============================================================================

ALTER TABLE iam_users ADD COLUMN IF NOT EXISTS allow_test_recharge BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE iam_users ADD COLUMN IF NOT EXISTS welcome_bonus_claimed BOOLEAN NOT NULL DEFAULT FALSE;

COMMENT ON COLUMN iam_users.allow_test_recharge IS 'Permiso para recarga ficticia. Solo activable desde BD por admin.';
COMMENT ON COLUMN iam_users.welcome_bonus_claimed IS 'Indica si el usuario ya recibió su bono de bienvenida de $1 USDC.';
