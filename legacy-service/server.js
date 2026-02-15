const express = require("express");

const app = express();
app.use(express.json());

app.get("/legacy/employee/:id", (req, res) => {
  const { id } = req.params;
  res.json({
    id,
    name: "Employee " + id,
    department: "Finance Ops",
    location: "Ames, IA",
    workerType: "Staff",
    lastSync: new Date().toISOString()
  });
});

app.post("/legacy/sync", (req, res) => {
  console.log("LEGACY SYNC RECEIVED:", JSON.stringify(req.body, null, 2));
  res.json({ ok: true, receivedAt: new Date().toISOString() });
});

const port = process.env.PORT || 8081;
app.listen(port, () => console.log(`Legacy service running on :${port}`));
