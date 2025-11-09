import express from "express";
import { getModeliByMarka } from "../services/modelService.js";

const router = express.Router();

router.get("/:id_marka", async (req, res) => {
  try {
    const modeli = await getModeliByMarka(req.params.id_marka);
    res.json(modeli);
  } catch (err) {
    console.error("Greška pri dohvaćanju modela:", err);
    res.status(500).json({ message: "Greška pri dohvaćanju modela" });
  }
});

export default router;
