import MarkaService from "../services/MarkaService.js";

class MarkaController {
  async getAllMarke(req, res) {
    try {
      const marke = await MarkaService.getAllMarke();
      res.json(marke);
    } catch (error) {
      res.status(500).json({ message: "Greška pri dohvaćanju marki." });
    }
  }
}

export default new MarkaController();
