import express from "express";
import VehicleController from "../controllers/VehicleController.js";
import { isAuthenticated } from "../middleware/authMiddleware.js";  // ← Promijeni u isAuthenticated

const router = express.Router();

router.get("/vozila", isAuthenticated, (req, res) => VehicleController.getVozilaOdOsobe(req, res));
router.post("/vozila", isAuthenticated, (req, res) => VehicleController.createVozilo(req, res));
router.delete("/vozila/:id_vozilo", isAuthenticated, (req, res) => VehicleController.deleteVozilo(req, res));

export default router;
