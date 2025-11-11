import express from "express";
import ModelController from "../controllers/ModelController.js";

const router = express.Router();

router.get("/modeli/:id_marka", (req, res) => ModelController.getModeliByMarka(req, res));  // Bez auth

export default router;
