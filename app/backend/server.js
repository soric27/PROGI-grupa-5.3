import express from "express";
import session from "express-session";
import cors from "cors";
import dotenv from "dotenv";
import pgSession from "connect-pg-simple";
import vehicleRoutes from "./src/routes/vehicleRoutes.js";
import passport from "./src/config/passport.js";
import authRoutes from "./src/routes/authRoutes.js";
import markaRoutes from "./src/routes/markaRoutes.js";
import modelRoutes from "./src/routes/modelRoutes.js";
import { pool } from "./src/config/db.js";

dotenv.config();

const app = express();
const PostgresStore = pgSession(session);

app.set('trust proxy', 1); // ← DODAJ OVO za Render

app.use(
  cors({
    origin: process.env.FRONTEND_URL,
    credentials: true,
  })
);

app.use(express.json());

app.use(
  session({
    store: new PostgresStore({
      pool: pool,
      tableName: "session", // ← Postgres će kreirati session tablicu
    }),
    secret: process.env.SESSION_SECRET || "secret",
    resave: false,
    saveUninitialized: false,
    cookie: {
      secure: process.env.NODE_ENV === 'production',
      sameSite: process.env.NODE_ENV === 'production' ? 'none' : 'lax',
      httpOnly: true,
      maxAge: 24 * 60 * 60 * 1000
    }
  })
);

app.use(passport.initialize());
app.use(passport.session());

// Routes
app.use("/auth", authRoutes);
app.use("/api", vehicleRoutes);
app.use("/api", markaRoutes);
app.use("/api", modelRoutes);

app.get("/", (req, res) => {
  res.send("Autoservis REST API radi");
});

const PORT = process.env.PORT || 5000;
app.listen(PORT, '0.0.0.0', () => console.log(`Server pokrenut na vratima ${PORT}`));
