import VehicleService from "../services/VehicleService.js";

class VehicleController {
  async getVozilaOdOsobe(req, res) {
    try {
      const { id_osoba } = req.user;
      const vehicles = await VehicleService.getVozilaByOsoba(id_osoba);
      res.json(vehicles);
    } catch (error) {
      console.error("Greška pri dohvaćanju vozila:", error);
      res.status(500).json({ message: "Greška pri dohvaćanju vozila." });
    }
  }

  async createVozilo(req, res) {
    try {
      const { id_model, registracija, godina_proizvodnje } = req.body;
      const { id_osoba } = req.user;
      
      const newVehicle = await VehicleService.addVozilo({
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
  }

  async deleteVozilo(req, res) {
    try {
      const { id_vozilo } = req.params;
      const { id_osoba } = req.user;
      
      const deletedVehicle = await VehicleService.deleteVozilo(id_vozilo, id_osoba);
      
      if (!deletedVehicle) {
        return res.status(404).json({ message: "Vozilo nije pronađeno." });
      }
      
      res.json({ message: "Vozilo obrisano.", vehicle: deletedVehicle });
    } catch (error) {
      console.error("Greška pri brisanju vozila:", error);
      res.status(500).json({ message: "Greška pri brisanju vozila." });
    }
  }
}

export default new VehicleController();
