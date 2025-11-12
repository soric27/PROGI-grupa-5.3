import express from "express";
import passport from "passport";

const router = express.Router();

// pokreće Google OAuth2 login
router.get("/google", passport.authenticate("google", { scope: ["profile", "email"] }));

// callback nakon što Google potvrdi korisnika
router.get(
  "/google/callback",
  passport.authenticate("google", {
    failureRedirect: "https://progi-grupa-5-3-fyxj.onrender.com/?login=fail",
    session: true,
  }),
  (req, res) => {
    res.redirect("https://progi-grupa-5-3-fyxj.onrender.com/?login=success");
  }
);

router.get("/logout", (req, res) => {
  req.logout(() => {
    res.redirect("http://localhost:3000");
  });
});

router.get("/user", (req, res) => {
  if (req.isAuthenticated()) {
    res.json(req.user);
  } else {
    res.status(401).json({ message: "Niste prijavljeni" });
  }
});

export default router;