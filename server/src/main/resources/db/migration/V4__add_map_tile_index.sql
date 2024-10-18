DELETE
FROM map_tile
WHERE TRUE;

CREATE UNIQUE INDEX map_tile_index ON map_tile (x, y)
