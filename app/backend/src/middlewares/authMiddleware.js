export const checkAuthentication = (req, res, next) => {
  if (req.isAuthenticated && req.isAuthenticated()) {
    // korisnik je prijavljen
    return next();
  } else {
    // korisnik nije prijavljen
    return res.status(401).json({ message: "Pristup odbijen. Prijavite se za nastavak." });
  }
};