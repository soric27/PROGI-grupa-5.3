import express from "express";
import passport from "passport";

const router = express.Router();

// pokreće Google OAuth2 login
router.get("/google", passport.authenticate("google", { scope: ["profile", "email"] }));

// callback nakon što Google potvrdi korisnika
router.get(
  "/google/callback",
  passport.authenticate("google", {
    failureRedirect: (`${process.env.FRONTEND_URL}/?login=fail`),
    session: true,
  }),
  (req, res) => {
    res.redirect(`${process.env.FRONTEND_URL}/?login=success`);
  }
);

router.get("/logout", (req, res) => {
  req.logout(() => {
    res.redirect(process.env.FRONTEND_URL);  // ✅
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