import express from "express";
import { getAllVehicles, addVehicle } from "../controllers/vehicleController.js";

const router = express.Router();

// router.get("/", getAllVehicles);   // dohvati sva vozila
router.get('/', (req, res) => {
  res.json([
    { id: 1, marka: 'Toyota', model: 'Yaris', godina: 2020 },
    { id: 2, marka: 'Volkswagen', model: 'Golf', godina: 2019 }
  ]);
});

router.post("/", addVehicle);      // dodaj novo vozilo

export default router;