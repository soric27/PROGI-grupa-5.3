export const authorizeRoles = (...allowedRoles) => {
  return (req, res, next) => {
    if (!req.isAuthenticated || !req.isAuthenticated()) {
      return res.status(401).json({ message: "Niste prijavljeni." });
    }

    const user = req.user;  // passport sprema korisnika u req.user
    if (!user) {
      return res.status(403).json({ message: "Podaci o korisniku nisu pronađeni." });
    }

    // Provjera ima li korisnik dopuštenu ulogu
    if (!allowedRoles.includes(user.uloga)) {
      return res.status(403).json({ message: "Nemate dopuštenje za ovu akciju." });
    }

    next();
  };
};