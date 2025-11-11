import ModelService from "../services/ModelService.js";

class ModelController {
  async getModeliByMarka(req, res) {
    try {
      const { id_marka } = req.params;
      const modeli = await ModelService.getModeliByMarka(id_marka);
      res.json(modeli);
    } catch (error) {
      console.error("Greška pri dohvaćanju modela:", error);
      res.status(500).json({ message: "Greška pri dohvaćanju modela." });
    }
  }
}

export default new ModelController();
