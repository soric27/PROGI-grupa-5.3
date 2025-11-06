import { getVozilaByOsoba, addVozilo } from "../services/vehicleService.js";

export const getVozilaOdOsobe = async (req, res) => {
  try {
    const { id_osoba } = req.user; // dobiveno iz middleware-a nakon logina
    const vehicles = await getVozilaByOsoba(id_osoba);
    res.json(vehicles);
  } catch (error) {
    console.error("Greška pri dohvaćanju vozila:", error);
    res.status(500).json({ message: "Greška pri dohvaćanju vozila." });
  }
};

export const createVozilo = async (req, res) => {
  try {
    const { id_model, registracija, godina_proizvodnje } = req.body;
    const { id_osoba } = req.user;
    const newVehicle = await addVozilo({
      id_osoba,
      id_model,
      registracija,
      godina_proizvodnje,
    });
    res.status(201).json(newVehicle);
  } catch (error) {
    console.error("Greška pri dodavanju vozila:", error);
    res.status(500).json({ message: "Greška pri dodavanju vozila." });
  }
};