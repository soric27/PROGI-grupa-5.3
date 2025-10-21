import { pool } from "../config/db.js";

export const getVehiclesFromDB = async () => {
  const [rows] = await pool.query("SELECT * FROM vozilo");
  return rows;
};

export const insertVehicleToDB = async (vehicleData) => {
  const { id_vozilo, id_osoba, marka, model, registracija, godina_proizvodnje } = vehicleData;

  const [result] = await pool.query(
//    "INSERT INTO vozilo (id_vozilo, id_osoba, marka, model, registracija, godina_proizvodnje) VALUES (?, ?, ?, ?, ?, ?)",
    [id_vozilo, id_osoba, marka, model, registracija, godina_proizvodnje]
  );

  return { id: result.insertId, ...vehicleData };
};