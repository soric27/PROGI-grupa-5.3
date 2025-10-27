import express from "express";
import { getAllVehicles, addVehicle } from "../controllers/vehicleController.js";
import { checkAuthentication } from "../middlewares/authMiddleware.js";
import { authorizeRoles } from "../middlewares/roleMiddleware.js";

const router = express.Router();

router.get("/", getAllVehicles);   // dohvati sva vozila
//router.get('/', (req, res) => {
//  res.json([
//    { id: 1, marka: 'Toyota', model: 'Yaris', godina: 2020 },
//    { id: 2, marka: 'Volkswagen', model: 'Golf', godina: 2019 }
//  ]);
//});

router.post("/", checkAuthentication, addVehicle);      // dodaj novo vozilo

// router.post("/reserve", checkAuthentication, ---);   --> napraviti middleware za rezervaciju zamjesnog vozila

router.delete("/:id", checkAuthentication, authorizeRoles("admin"), async (req, res) => {
  try {
    const { id } = req.params;
    await pool.query("DELETE FROM vozilo WHERE id_vozilo=$1", [id]);
    res.json({ message: `Vozilo ${id} je obrisano.` });
  } catch (err) {
    res.status(500).json({ message: "Greška pri brisanju vozila." });
  }
});

export default router;