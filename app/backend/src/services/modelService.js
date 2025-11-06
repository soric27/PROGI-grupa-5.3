import { pool } from "../config/db.js";

// dohvaca modele za odredjenu marku
export const getModeliByMarka = async (id_marka) => {
  const result = await pool.query(
    "SELECT * FROM model WHERE id_marka = $1 ORDER BY naziv ASC",
    [id_marka]
  );
  return result.rows;
};
