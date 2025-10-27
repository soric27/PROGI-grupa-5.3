import express from "express";
import session from "express-session";
import cors from "cors";
import dotenv from "dotenv";
import vehicleRoutes from "./src/routes/vehicleRoutes.js";
import passport from "./src/config/passport.js";
import authRoutes from "./src/routes/authRoutes.js";

dotenv.config();
const app = express();

app.use(
  cors({
    origin: "http://localhost:3000",
    credentials: true,     // omogućuje cookie-je
  })
);

app.use("/auth", authRoutes);

app.get("/api/auth/google", passport.authenticate("google", { scope: ["profile", "email"] }));

app.get("/api/auth/google/callback", 
  passport.authenticate("google", { failureRedirect: "/login" }),
  (req, res) => {
    // frontend pohranjuje cookie
    res.redirect(`${process.env.FRONTEND_URL}/dashboard`);
  }
);

app.get("/api/auth/logout", (req, res) => {
  req.logout(() => {
    res.redirect(process.env.FRONTEND_URL);
  });
});

app.get("/api/auth/me", (req, res) => {
  if (req.isAuthenticated()) {
    res.json(req.user);
  } else {
    res.status(401).json({ message: "Korisnik nije prijavljen." });
  }
});

app.use(cors());
app.use(express.json());

app.use(session({
  secret: process.env.SESSION_SECRET || "secret",
  resave: false,
  saveUninitialized: false
}));

app.use(passport.initialize());
app.use(passport.session());

app.get("/", (req, res) => {
  res.send("Autoservis REST API radi");
});

app.use("/api/vehicles", vehicleRoutes);

const PORT = process.env.PORT || 5000;
app.listen(PORT, () => console.log(`Server pokrenut na vratima ${PORT}`));