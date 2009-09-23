ALTER TABLE postgrid."Layers" ADD COLUMN minimum double precision;
ALTER TABLE postgrid."Layers" ADD COLUMN maximum double precision;

COMMENT ON COLUMN postgrid."Layers".minimum IS 'Minimum value of the layer.';
COMMENT ON COLUMN postgrid."Layers".maximum IS 'Maximum value of the layer.';
