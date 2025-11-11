import { pool } from "../config/db.js";

class VehicleService {
  async getVozilaByOsoba(id_osoba) {
    const result = await pool.query(
      `SELECT v.*, m.naziv AS model_naziv, ma.naziv AS marka_naziv
       FROM vozilo v
       JOIN model m ON v.id_model = m.id_model
       JOIN marka ma ON m.id_marka = ma.id_marka
       WHERE v.id_osoba = $1
       ORDER BY v.id_vozilo DESC`,
      [id_osoba]
    );
    return result.rows;
  }

  async addVozilo({ id_osoba, id_model, registracija, godina_proizvodnje }) {
    const result = await pool.query(
      `INSERT INTO vozilo (id_osoba, id_model, registracija, godina_proizvodnje)
       VALUES ($1, $2, $3, $4)
       RETURNING *`,
      [id_osoba, id_model, registracija, godina_proizvodnje]
    );
    return result.rows[0];
  }

  async deleteVozilo(id_vozilo, id_osoba) {
    const result = await pool.query(
      `DELETE FROM vozilo WHERE id_vozilo = $1 AND id_osoba = $2 RETURNING *`,
      [id_vozilo, id_osoba]
    );
    return result.rows[0];
  }
}

export default new VehicleService(); // Singleton pattern
