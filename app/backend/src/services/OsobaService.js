import { pool } from "../config/db.js";

class OsobaService {
  async getOsobaByEmail(email) {
    const result = await pool.query(
      "SELECT * FROM osoba WHERE email = $1",
      [email]
    );
    return result.rows[0];
  }

  async getOsobaById(id_osoba) {
    const result = await pool.query(
      "SELECT * FROM osoba WHERE id_osoba = $1",
      [id_osoba]
    );
    return result.rows[0];
  }

  async addOsoba({ ime, prezime, email, telefon, uloga, oauth_id }) {
    const result = await pool.query(
      `INSERT INTO osoba (ime, prezime, email, telefon, uloga, oauth_id)
       VALUES ($1, $2, $3, $4, $5, $6)
       RETURNING *`,
      [ime, prezime, email, telefon, uloga, oauth_id]
    );
    return result.rows[0];
  }
}

export default new OsobaService();
