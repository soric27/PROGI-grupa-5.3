import { pool } from "../config/db.js";

export const getVehiclesFromDB = async () => {
  const [rows] = await pool.query("SELECT * FROM vozilo");
  return rows;
};

export const insertVehicleToDB = async (vehicleData) => {
  const { id_vozilo, id_osoba, marka, model, registracija, godina_proizvodnje } = vehicleData;

  const query = `
    INSERT INTO vozilo (id_vozilo, id_osoba, marka, model, registracija, godina_proizvodnje)
    VALUES ($1, $2, $3, $4, $5)
    RETURNING *;
  `;

  const values = [id_vozilo, id_osoba, marka, model, registracija, godina_proizvodnje];

  const { rows } = await pool.query(query, values);
  return rows[0];
};