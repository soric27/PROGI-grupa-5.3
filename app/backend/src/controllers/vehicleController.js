import { getVehiclesFromDB, insertVehicleToDB } from "../services/vehicleService.js";

export const getAllVehicles = async (req, res) => {
  try {
    const vehicles = await getVehiclesFromDB();
    res.json(vehicles);
  } catch (error) {
    console.error("Greška pri dohvaćanju vozila:", error);
    res.status(500).json({ message: "Greška pri dohvaćanju vozila." });
  }
};

export const addVehicle = async (req, res) => {
  try {
    const vehicle = req.body;
    const newVehicle = await insertVehicleToDB(vehicle);
    res.status(201).json(newVehicle);
  } catch (error) {
    console.error("Greška pri dodavanju vozila:", error);
    res.status(500).json({ message: "Greška pri dodavanju vozila." });
  }
};