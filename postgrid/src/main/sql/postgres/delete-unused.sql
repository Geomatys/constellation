--
-- Delete unused entries from the GridGeometries table.
--

DELETE FROM "GridGeometries" WHERE "identifier" IN
  (SELECT "identifier" FROM "GridGeometries" EXCEPT SELECT "extent" FROM "GridCoverages");
