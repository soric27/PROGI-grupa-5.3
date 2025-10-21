import express from "express";
import cors from "cors";
import dotenv from "dotenv";
import vehicleRoutes from "./src/routes/vehicleRoutes.js";

dotenv.config();
const app = express();

app.use(cors());
app.use(express.json());

app.get("/", (req, res) => {
  res.send("Autoservis REST API radi");
});

app.use("/api/vehicles", vehicleRoutes);

const PORT = process.env.PORT || 5000;
app.listen(PORT, () => console.log(`Server pokrenut na vratima ${PORT}`));