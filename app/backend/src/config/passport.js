import passport from "passport";
import { Strategy as GoogleStrategy } from "passport-google-oauth20";
import dotenv from "dotenv";
import { pool } from "./db.js";

dotenv.config();

passport.use(new GoogleStrategy({
    clientID: process.env.GOOGLE_CLIENT_ID,
    clientSecret: process.env.GOOGLE_CLIENT_SECRET,
    callbackURL: process.env.GOOGLE_CALLBACK_URL,
  },
  async (accessToken, refreshToken, profile, done) => {
    try {
      // provjeri postoji li korisnik u bazi
      const { rows } = await pool.query("SELECT * FROM osoba WHERE oauth_id=$1", [profile.id]);
      let user;
      if (rows.length === 0) {
        // ako ne postoji, dodaj novog korisnika
        const insertQuery = `
          INSERT INTO osoba (ime, prezime, email, uloga, oauth_id)
          VALUES ($1, $2, $3, $4, $5)
          RETURNING *;
        `;
        const values = [
          profile.name?.givenName || profile.displayName || "Korisnik",
          profile.name?.familyName || "",
          profile.emails[0].value,
          "korisnik",
          profile.id
        ];
        const { rows: newUser } = await pool.query(insertQuery, values);
        user = newUser[0];
      } else {
        user = rows[0];
      }
      return done(null, user);
    } catch (err) {
      return done(err, null);
    }
  }
));

passport.serializeUser((user, done) => {
  done(null, user.id_osoba);
});

passport.deserializeUser(async (id, done) => {
  try {
    const { rows } = await pool.query("SELECT * FROM osoba WHERE id_osoba=$1", [id]);
    done(null, rows[0]);
  } catch (err) {
    done(err, null);
  }
});

export default passport;