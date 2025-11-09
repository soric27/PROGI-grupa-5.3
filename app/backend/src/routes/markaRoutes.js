import express from "express";
import { getAllMarke } from "../services/markaService.js";

const router = express.Router();

router.get("/", async (req, res) => {
  try {
    const marke = await getAllMarke();
    res.json(marke);
  } catch (err) {
    console.error("Greška pri dohvaćanju marki:", err);
    res.status(500).json({ message: "Greška pri dohvaćanju marki" });
  }
});

export default router;
