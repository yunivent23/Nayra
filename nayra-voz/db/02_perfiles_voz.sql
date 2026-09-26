-- D-039: perfil de voz = centroide cifrado (AES-256-GCM). Sin audio. Ejecutar como nayra_voz.
-- La relación con usuarios es lógica (mismo usuario_id), sin FK entre esquemas.
CREATE TABLE IF NOT EXISTS biometria.perfiles_voz (
    id                  BIGSERIAL PRIMARY KEY,
    usuario_id          BIGINT       NOT NULL UNIQUE,
    embedding_cifrado   BYTEA        NOT NULL,
    iv                  BYTEA        NOT NULL,
    modelo              VARCHAR(100) NOT NULL,
    modelo_version      VARCHAR(100) NOT NULL,
    num_muestras        SMALLINT     NOT NULL,
    estado              VARCHAR(20)  NOT NULL CHECK (estado IN ('ACTIVO')),
    fecha_creacion      TIMESTAMP WITH TIME ZONE NOT NULL,
    fecha_actualizacion TIMESTAMP WITH TIME ZONE NOT NULL
);
