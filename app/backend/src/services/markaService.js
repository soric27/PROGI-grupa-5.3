import { pool } from "../config/db.js";

// dohvaca marke
export const getAllMarke = async () => {
  const result = await pool.query("SELECT * FROM marka ORDER BY naziv ASC");
  return result.rows;
};