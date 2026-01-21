import { useEffect, useState } from "react";
import axios from "axios";

function formatDate(d) {
  return d.toISOString().slice(0, 10);
}

function Statistika({ user }) {
  const [from, setFrom] = useState(() => {
    const d = new Date();
    d.setDate(d.getDate() - 30);
    return formatDate(d);
  });
  const [to, setTo] = useState(() => formatDate(new Date()));
  const [stats, setStats] = useState(null);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  const fetchStats = async () => {
    setError("");
    setLoading(true);
    try {
      const resp = await axios.get("/api/stats", {
        params: { from, to },
      });
      setStats(resp.data);
    } catch (err) {
      const msg = err?.response?.data?.message || err?.message || "Greska pri dohvatu statistike.";
      setError(msg);
    } finally {
      setLoading(false);
    }
  };

  const download = async (format) => {
    setError("");
    try {
      const resp = await axios.get("/api/stats", {
        params: { from, to, format },
        responseType: "blob",
      });
      const blob = new Blob([resp.data]);
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement("a");
      a.href = url;
      a.download = `stats.${format}`;
      document.body.appendChild(a);
      a.click();
      a.remove();
      window.URL.revokeObjectURL(url);
    } catch (err) {
      const msg = err?.response?.data?.message || err?.message || "Greska pri izvozu.";
      setError(msg);
    }
  };

  useEffect(() => {
    if (user && (user.uloga === "serviser" || user.uloga === "administrator")) {
      fetchStats();
    }
  }, [from, to, user]);

  if (!user || (user.uloga !== "serviser" && user.uloga !== "administrator")) {
    return <div className="alert alert-warning">Nemate pristup statistici.</div>;
  }

  return (
    <div className="container">
      <h2>Statistika servisa</h2>

      <div className="card p-3 mb-3">
        <div className="row g-2 align-items-end">
          <div className="col-md-4">
            <label className="form-label">Od</label>
            <input type="date" className="form-control" value={from} onChange={(e) => setFrom(e.target.value)} />
          </div>
          <div className="col-md-4">
            <label className="form-label">Do</label>
            <input type="date" className="form-control" value={to} onChange={(e) => setTo(e.target.value)} />
          </div>
          <div className="col-md-4 d-flex gap-2">
            <button className="btn btn-primary" onClick={fetchStats} disabled={loading}>
              {loading ? "Ucitavanje..." : "Osvjezi"}
            </button>
            <button className="btn btn-outline-secondary" onClick={() => download("pdf")}>PDF</button>
            <button className="btn btn-outline-secondary" onClick={() => download("xml")}>XML</button>
            <button className="btn btn-outline-secondary" onClick={() => download("xlsx")}>XLSX</button>
          </div>
        </div>
        {error && <div className="text-danger mt-2">{error}</div>}
      </div>

      {stats && (
        <div className="card p-3">
          <div><strong>Broj zaprimljenih vozila:</strong> {stats.prijaveCount}</div>
          <div><strong>Zavrseni popravci:</strong> {stats.completedRepairsCount}</div>
          <div><strong>Prosjecno trajanje popravka (dani):</strong> {Number(stats.averageRepairDays).toFixed(2)}</div>
          <div><strong>Zauzece zamjenskih vozila (%):</strong> {Number(stats.replacementOccupancyPercent).toFixed(2)}</div>
          <div><strong>Dostupni termini (broj):</strong> {stats.availableSlotsCount}</div>
        </div>
      )}
    </div>
  );
}

export default Statistika;
