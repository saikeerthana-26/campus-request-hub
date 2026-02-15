import React, { useMemo, useState } from "react";
import { audit, createRequest, legacyEmployee, listRequests, login, updateStatus, type RequestItem } from "./api";

type Session = { username: string; roles: string[] };

function hasRole(session: Session | null, role: string) {
  return !!session?.roles?.includes(role);
}

export default function App() {
  const [session, setSession] = useState<Session | null>(() => {
    const username = localStorage.getItem("username");
    const roles = JSON.parse(localStorage.getItem("roles") || "[]");
    const token = localStorage.getItem("token");
    return token && username ? { username, roles } : null;
  });

  const [view, setView] = useState<"login" | "dashboard">(() => (session ? "dashboard" : "login"));
  const [err, setErr] = useState<string>("");

  async function doLogin(u: string, p: string) {
    setErr("");
    try {
      const res = await login(u, p);
      localStorage.setItem("token", res.token);
      localStorage.setItem("username", res.username);
      localStorage.setItem("roles", JSON.stringify(res.roles));
      setSession({ username: res.username, roles: res.roles });
      setView("dashboard");
    } catch (e: any) {
      setErr("Login failed. Use employee/approver/admin + Password123!");
    }
  }

  function logout() {
    localStorage.clear();
    setSession(null);
    setView("login");
  }

  return (
    <div className="container">
      {view === "login" ? (
        <Login onLogin={doLogin} error={err} />
      ) : (
        <Dashboard session={session!} onLogout={logout} />
      )}
    </div>
  );
}

function Login({ onLogin, error }: { onLogin: (u: string, p: string) => void; error: string }) {
  const [u, setU] = useState("employee");
  const [p, setP] = useState("Password123!");
  return (
    <div className="card" style={{ maxWidth: 520, margin: "70px auto" }}>
      <h2 className="h">Campus Request Hub</h2>
      <p className="small">Demo accounts: employee / approver / admin — password: Password123!</p>
      {error ? <p style={{ color: "crimson" }}>{error}</p> : null}
      <div style={{ display: "grid", gap: 10, marginTop: 10 }}>
        <input className="input" value={u} onChange={(e) => setU(e.target.value)} placeholder="username" />
        <input className="input" value={p} type="password" onChange={(e) => setP(e.target.value)} placeholder="password" />
        <button className="btn btnPrimary" onClick={() => onLogin(u, p)}>
          Sign in
        </button>
      </div>
    </div>
  );
}

function Dashboard({ session, onLogout }: { session: Session; onLogout: () => void }) {
  const [items, setItems] = useState<RequestItem[]>([]);
  const [selected, setSelected] = useState<RequestItem | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const canApprove = useMemo(() => hasRole(session, "ROLE_APPROVER") || hasRole(session, "ROLE_ADMIN"), [session]);
  const isAdmin = useMemo(() => hasRole(session, "ROLE_ADMIN"), [session]);

  async function refresh() {
    setError("");
    setLoading(true);
    try {
      const res = await listRequests();
      setItems(res);
      if (selected) {
        const updated = res.find((x) => x.id === selected.id) || null;
        setSelected(updated);
      }
    } catch (e: any) {
      setError("Failed to load requests.");
    } finally {
      setLoading(false);
    }
  }

  React.useEffect(() => { refresh(); }, []);

  async function onCreate(title: string, description: string, category: string) {
    await createRequest(title, description, category);
    await refresh();
  }

  async function onSetStatus(id: string, status: RequestItem["status"]) {
    await updateStatus(id, status);
    await refresh();
  }

  return (
    <>
      <div className="topbar">
        <div>
          <h2 className="h">Dashboard</h2>
          <div className="small">
            Signed in as <b>{session.username}</b> — {session.roles.join(", ")}
          </div>
        </div>
        <div className="row">
          <button className="btn btnGhost" onClick={refresh}>{loading ? "Refreshing..." : "Refresh"}</button>
          <button className="btn btnGhost" onClick={onLogout}>Logout</button>
        </div>
      </div>

      {error ? <p style={{ color: "crimson" }}>{error}</p> : null}

      <div className="row">
        <div className="card" style={{ flex: 2, minWidth: 520 }}>
          <h3 className="h">Requests</h3>
          <table className="table">
            <thead>
              <tr>
                <th>Title</th>
                <th>Status</th>
                <th>Category</th>
                <th>Owner</th>
              </tr>
            </thead>
            <tbody>
              {items.map((r) => (
                <tr key={r.id} onClick={() => setSelected(r)} style={{ cursor: "pointer" }}>
                  <td className="link">{r.title}</td>
                  <td><span className="badge">{r.status}</span></td>
                  <td>{r.category}</td>
                  <td>{r.createdBy}</td>
                </tr>
              ))}
            </tbody>
          </table>
          <p className="small" style={{ marginTop: 10 }}>
            Approver/Admin sees SUBMITTED queue + own requests.
          </p>
        </div>

        <div className="card" style={{ flex: 1, minWidth: 360 }}>
          <h3 className="h">New Request</h3>
          <NewRequest onCreate={onCreate} />
        </div>
      </div>

      <div style={{ height: 16 }} />

      <div className="row">
        <div className="card" style={{ flex: 2, minWidth: 520 }}>
          <h3 className="h">Details</h3>
          {!selected ? (
            <p className="small">Select a request to view details.</p>
          ) : (
            <RequestDetails
              session={session}
              item={selected}
              canApprove={canApprove}
              isAdmin={isAdmin}
              onSetStatus={onSetStatus}
            />
          )}
        </div>

        <div className="card" style={{ flex: 1, minWidth: 360 }}>
          <h3 className="h">Legacy Integration</h3>
          <LegacyPanel />
        </div>
      </div>
    </>
  );
}

function NewRequest({ onCreate }: { onCreate: (t: string, d: string, c: string) => Promise<void> }) {
  const [title, setTitle] = useState("");
  const [desc, setDesc] = useState("");
  const [cat, setCat] = useState("HR");
  const [msg, setMsg] = useState("");

  async function submit() {
    setMsg("");
    await onCreate(title || "Untitled request", desc || "No description", cat);
    setTitle(""); setDesc("");
    setMsg("Created in DRAFT. Open it and submit.");
  }

  return (
    <div style={{ display: "grid", gap: 10 }}>
      <input className="input" value={title} onChange={(e) => setTitle(e.target.value)} placeholder="Title" />
      <input className="input" value={cat} onChange={(e) => setCat(e.target.value)} placeholder="Category (HR/Finance/Payroll)" />
      <textarea className="input" value={desc} onChange={(e) => setDesc(e.target.value)} placeholder="Description" style={{ minHeight: 90 }} />
      <button className="btn btnPrimary" onClick={submit}>Create</button>
      {msg ? <div className="small">{msg}</div> : null}
    </div>
  );
}

function RequestDetails({
  session,
  item,
  canApprove,
  isAdmin,
  onSetStatus
}: {
  session: Session;
  item: RequestItem;
  canApprove: boolean;
  isAdmin: boolean;
  onSetStatus: (id: string, status: RequestItem["status"]) => Promise<void>;
}) {
  const [auditItems, setAuditItems] = useState<any[]>([]);
  const [auditErr, setAuditErr] = useState("");

  async function loadAudit() {
    setAuditErr("");
    try {
      const res = await audit(item.id);
      setAuditItems(res.items);
    } catch {
      setAuditErr("Failed to load audit.");
    }
  }

  React.useEffect(() => { loadAudit(); }, [item.id]);

  const isOwner = item.createdBy === session.username;

  return (
    <div style={{ display: "grid", gap: 10 }}>
      <div><b>{item.title}</b> <span className="badge">{item.status}</span></div>
      <div className="small">{item.description}</div>
      <div className="small">Category: <b>{item.category}</b> • Owner: <b>{item.createdBy}</b></div>

      <div className="row">
        {isOwner && item.status === "DRAFT" ? (
          <button className="btn btnPrimary" onClick={() => onSetStatus(item.id, "SUBMITTED")}>Submit</button>
        ) : null}

        {canApprove && item.status === "SUBMITTED" ? (
          <>
            <button className="btn btnPrimary" onClick={() => onSetStatus(item.id, "APPROVED")}>Approve</button>
            <button className="btn btnGhost" onClick={() => onSetStatus(item.id, "REJECTED")}>Reject</button>
          </>
        ) : null}

        {isOwner && item.status === "REJECTED" ? (
          <button className="btn btnPrimary" onClick={() => onSetStatus(item.id, "DRAFT")}>Revise (Back to Draft)</button>
        ) : null}

        {isAdmin && item.status === "APPROVED" ? (
          <button className="btn btnPrimary" onClick={() => onSetStatus(item.id, "COMPLETED")}>Mark Completed</button>
        ) : null}
      </div>

      <div>
        <div className="row" style={{ justifyContent: "space-between", alignItems: "center" }}>
          <h4 className="h" style={{ marginBottom: 0 }}>Audit Log</h4>
          <button className="btn btnGhost" onClick={loadAudit}>Reload</button>
        </div>
        {auditErr ? <div style={{ color: "crimson" }}>{auditErr}</div> : null}
        <ul className="small">
          {auditItems.map((a) => (
            <li key={a.id}>
              <b>{a.action}</b> by {a.actor} — {a.details} <span className="small">({a.createdAt})</span>
            </li>
          ))}
        </ul>
      </div>
    </div>
  );
}

function LegacyPanel() {
  const [id, setId] = useState("123");
  const [data, setData] = useState<any>(null);
  const [err, setErr] = useState("");

  async function fetchIt() {
    setErr("");
    try {
      const res = await legacyEmployee(id);
      setData(res);
    } catch {
      setErr("Failed to call legacy API (check backend/legacy containers).");
    }
  }

  return (
    <div style={{ display: "grid", gap: 10 }}>
      <div className="small">Fetch employee profile from “on-prem” legacy service via backend.</div>
      <input className="input" value={id} onChange={(e) => setId(e.target.value)} placeholder="Employee ID" />
      <button className="btn btnPrimary" onClick={fetchIt}>Fetch</button>
      {err ? <div style={{ color: "crimson" }}>{err}</div> : null}
      {data ? <pre className="card" style={{ background: "#0b1020", color: "white", overflow: "auto" }}>{JSON.stringify(data, null, 2)}</pre> : null}
    </div>
  );
}
