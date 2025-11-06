import express from "express";
import { getVozilaOdOsobe, createVozilo } from "../controllers/vehicleController.js";
import { checkAuthentication } from "../middlewares/authMiddleware.js";
import { authorizeRoles } from "../middlewares/roleMiddleware.js";

const router = express.Router();

router.get("/", checkAuthentication, getVozilaOdOsobe);   // dohvati sva vozila

router.post("/", checkAuthentication, createVozilo);      // dodaj novo vozilo

// router.post("/reserve", checkAuthentication, ---);   --> napraviti middleware za rezervaciju zamjesnog vozila

router.delete("/:id", checkAuthentication, authorizeRoles("administrator"), async (req, res) => {
  try {
    const { id } = req.params;
    await pool.query("DELETE FROM vozilo WHERE id_vozilo=$1", [id]);
    res.json({ message: `Vozilo ${id} je obrisano.` });
  } catch (err) {
    res.status(500).json({ message: "Greška pri brisanju vozila." });
  }
});

export default router;