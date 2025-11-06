import { pool } from "../config/db.js";

// ovo pri login-u
export const getOsobaByEmail = async (email) => {
  const result = await pool.query(
    "SELECT * FROM osoba WHERE email = $1",
    [email]
  );
  return result.rows[0];
};

// dodaje novu osobu
export const addOsoba = async ({ ime, prezime, email, telefon, uloga, oauth_id }) => {
  const result = await pool.query(
    `INSERT INTO osoba (ime, prezime, email, telefon, uloga, oauth_id)
     VALUES ($1, $2, $3, $4, $5, $6)
     RETURNING *`,
    [ime, prezime, email, telefon, uloga, oauth_id]
  );
  return result.rows[0];
};
