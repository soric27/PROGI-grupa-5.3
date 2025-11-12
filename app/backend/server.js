import express from "express";
import session from "express-session";
import cors from "cors";
import dotenv from "dotenv";
import vehicleRoutes from "./src/routes/vehicleRoutes.js";
import passport from "./src/config/passport.js";
import authRoutes from "./src/routes/authRoutes.js";
import markaRoutes from "./src/routes/markaRoutes.js";
import modelRoutes from "./src/routes/modelRoutes.js";

dotenv.config();
const app = express();

app.use(
  cors({
    origin: "http://localhost:3000",
    credentials: true,
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

// Routes
app.use("/auth", authRoutes);
app.use("/api", vehicleRoutes);  // ✅ Samo "/api"
app.use("/api", markaRoutes);    // ✅ Samo "/api"
app.use("/api", modelRoutes);    // ✅ Samo "/api"

app.get("/", (req, res) => {
  res.send("Autoservis REST API radi");
});

const PORT = process.env.PORT || 5000;
app.listen(PORT, '0.0.0.0', () => console.log(`Server pokrenut na vratima ${PORT}`));
