import express from "express";
import MarkaController from "../controllers/MarkaController.js";

const router = express.Router();

router.get("/marke", (req, res) => MarkaController.getAllMarke(req, res));

export default router;
