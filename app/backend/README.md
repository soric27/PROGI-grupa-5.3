# Backend - PDFs and Email reminders

This project now supports automatic PDF generation for service requests and email notifications when an appointment is rescheduled by at least 3 days.

## New environment variables
- MAIL_HOST - SMTP host
- MAIL_PORT - SMTP port (default 587)
- MAIL_USERNAME - SMTP username
- MAIL_PASSWORD - SMTP password
- PDF_STORAGE - directory where generated PDFs will be saved (default `./data/pdfs`)

## API highlights
- POST `/api/prijave` - create service request
  - body: { idVozilo: number, idServiser?: number, napomenaVlasnika?: string, terminDatum?: ISO_DATE_TIME }
  - generates a PDF and emails confirmation to the vehicle owner (if email present)

- PUT `/api/prijave/{id}` - update a service request
  - body: { newTerminDatum?: ISO_DATE_TIME, status?: string }
  - if the appointment is changed and delayed by >= 3 days, an email is sent to the owner with an updated PDF attached

- GET `/api/zamjene` - list available replacement vehicles
  - query params: `from` and `to` (ISO dates) to filter availability for a date range
- POST `/api/zamjene/rezervacije` - reserve a replacement vehicle
  - body: { idPrijava: number, idZamjena: number, datumOd: YYYY-MM-DD, datumDo: YYYY-MM-DD }
  - **Note:** the `dostupno` flag on the reserved vehicle will be set to `false` immediately when reservation is created
- POST `/api/zamjene/rezervacije/{id}/vrati` - mark reservation returned / vehicle back in service
  - sets the replacement vehicle `dostupno = true` and deletes the reservation
- GET `/api/zamjene/prijava/{id}/rezervacije` - list reservations for a service request

---

New feature: statistics endpoint
- GET `/api/stats` — returns statistics (JSON) or exported files (set `format=pdf|xml|xlsx`)
  - query: `from`, `to` (ISO dates) optional (defaults to last 30 days)
  - **Access:** only for users with role `serviser` (ROLE_SERVISER)
  - includes: number of received vehicles, completed repairs and average repair duration, replacement vehicle occupancy percentage, available appointment slots count and list

## Notes
- The backend uses Apache PDFBox to render simple PDFs and Spring Mail (JavaMailSender) to send emails.
- Make sure mail settings are provided as environment variables or in `application.yml`.
