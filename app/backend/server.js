import express from "express";
import session from "express-session";
import cors from "cors";
import dotenv from "dotenv";
import vehicleRoutes from "./src/routes/vehicleRoutes.js";
import passport from "./src/config/passport.js";
import authRoutes from "./src/routes/authRoutes.js";
import markaRoutes from "./routes/markaRoutes.js";
import modelRoutes from "./routes/modelRoutes.js";

dotenv.config();
const app = express();

app.use(
  cors({
    origin: "http://localhost:3000",
    credentials: true, // omogućuje cookie-je
  })
);

app.use(express.json());

app.use(
  session({
    secret: process.env.SESSION_SECRET || "secret",
    resave: false,
    saveUninitialized: false,
  })
);

app.use(passport.initialize());
app.use(passport.session());

app.use("/auth", authRoutes);
app.use("/api/vehicles", vehicleRoutes);
app.use("/api/marke", markaRoutes);
app.use("/api/modeli", modelRoutes);

app.get("/", (req, res) => {
  res.send("Autoservis REST API radi");
});

const PORT = process.env.PORT || 5000;
app.listen(PORT, () => console.log(`Server pokrenut na vratima ${PORT}`));