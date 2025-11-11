import { pool } from "../config/db.js";

class ModelService {
  async getModeliByMarka(id_marka) {
    const result = await pool.query(
      "SELECT * FROM model WHERE id_marka = $1 ORDER BY naziv ASC",
      [id_marka]
    );
    return result.rows;
  }

  async getModelById(id_model) {
    const result = await pool.query(
      "SELECT * FROM model WHERE id_model = $1",
      [id_model]
    );
    return result.rows[0];
  }
}

export default new ModelService();
