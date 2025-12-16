import { useEffect, useMemo, useState } from "react";

function App() {
  const [apiHealth, setApiHealth] = useState({ status: "pending" });
  const [loading, setLoading] = useState(false);
  const apiBaseUrl = useMemo(() => import.meta.env.VITE_API_BASE_URL || "http://localhost:8000", []);

  const fetchHealth = async () => {
    setLoading(true);
    try {
      const response = await fetch(`${apiBaseUrl}/health`);
      if (!response.ok) throw new Error(`Status ${response.status}`);
      const payload = await response.json();
      setApiHealth({ status: "ok", payload });
    } catch (error) {
      setApiHealth({ status: "error", error: String(error) });
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchHealth();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  return (
    <main className="app">
      <section className="hero">
        <h1>Library Borrowing Analysis</h1>
        <p>
          Use this starter UI to explore borrowing metrics, wired to the FastAPI backend. Configure
          <code> VITE_API_BASE_URL </code>
          to point at the API service behind the reverse proxy.
        </p>
        <div className="actions">
          <button type="button" onClick={fetchHealth} disabled={loading}>
            {loading ? "Checking..." : "Check API health"}
          </button>
        </div>
        <div className="card">
          <h2>API health</h2>
          {apiHealth.status === "pending" && <p>Waiting for first check…</p>}
          {apiHealth.status === "ok" && (
            <pre aria-label="API health payload">{JSON.stringify(apiHealth.payload, null, 2)}</pre>
          )}
          {apiHealth.status === "error" && <p className="error">{apiHealth.error}</p>}
        </div>
        <div className="card">
          <h3>Borrowing summary (stub)</h3>
          <p>
            Connect your database and cache, then replace the placeholder endpoint at
            <code> /api/borrowings/summary</code> with real analytics.
          </p>
        </div>
      </section>
    </main>
  );
}

export default App;
