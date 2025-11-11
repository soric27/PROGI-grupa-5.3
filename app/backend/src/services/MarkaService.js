import { pool } from "../config/db.js";

class MarkaService {
  async getAllMarke() {
    const result = await pool.query("SELECT * FROM marka ORDER BY naziv ASC");
    return result.rows;
  }

  async getMarkaById(id_marka) {
    const result = await pool.query(
      "SELECT * FROM marka WHERE id_marka = $1",
      [id_marka]
    );
    return result.rows[0];
  }
}

export default new MarkaService();
