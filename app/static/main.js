const importResult = document.getElementById("import-result");
const borrowTableBody = document.querySelector("#borrow-table tbody");
const selectAllCheckbox = document.getElementById("select-all");

async function uploadFile(endpoint, fileInputId) {
  const input = document.getElementById(fileInputId);
  if (!input.files.length) {
    importResult.textContent = "Please choose a file before importing.";
    return;
  }
  const formData = new FormData();
  formData.append("file", input.files[0]);

  const response = await fetch(endpoint, { method: "POST", body: formData });
  if (!response.ok) {
    const error = await response.json();
    importResult.textContent = error.detail || "Import failed";
    return;
  }
  const data = await response.json();
  importResult.textContent = `Imported ${data.success_count} rows. Failed rows: ${data.failed_rows.length}`;
  if (data.failed_rows.length) {
    importResult.textContent += ` | Details: ${data.failed_rows
      .map((row) => `#${row.row_number}: ${row.errors.join(", ")}`)
      .join("; ")}`;
  }
  await loadBorrows();
}

function buildQueryParams() {
  const params = new URLSearchParams();
  const start = document.getElementById("start-date").value;
  const end = document.getElementById("end-date").value;
  const category = document.getElementById("category").value;
  const status = document.getElementById("status").value;
  if (start) params.append("start_date", new Date(start).toISOString());
  if (end) params.append("end_date", new Date(end).toISOString());
  if (category) params.append("category", category);
  if (status) params.append("status", status);
  return params;
}

function formatDate(value) {
  if (!value) return "";
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return value;
  return date.toLocaleString();
}

async function loadBorrows() {
  const params = buildQueryParams();
  const response = await fetch(`/borrows?${params.toString()}`);
  const data = await response.json();

  borrowTableBody.innerHTML = "";
  data.forEach((item) => {
    const tr = document.createElement("tr");
    tr.innerHTML = `
      <td><input type="checkbox" class="row-select" value="${item.id}"></td>
      <td>${item.id}</td>
      <td>${item.reader_name} <span class="muted">(${item.reader_email})</span></td>
      <td>${item.book_title}</td>
      <td>${item.category || ""}</td>
      <td><span class="tag ${item.status}">${item.status}</span></td>
      <td>${formatDate(item.borrowed_at)}</td>
      <td>${formatDate(item.due_at)}</td>
      <td>${formatDate(item.returned_at)}</td>`;
    borrowTableBody.appendChild(tr);
  });
  selectAllCheckbox.checked = false;
}

function getSelectedIds() {
  return Array.from(document.querySelectorAll(".row-select:checked")).map((el) => Number(el.value));
}

async function applyBatch(endpoint) {
  const ids = getSelectedIds();
  if (!ids.length) {
    alert("Please select at least one record.");
    return;
  }
  const response = await fetch(endpoint, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ ids }),
  });
  if (!response.ok) {
    const error = await response.json();
    alert(error.detail || "Batch action failed");
    return;
  }
  const result = await response.json();
  alert(`Updated: ${result.updated}, Skipped: ${result.skipped}`);
  await loadBorrows();
}

function exportBorrows(format) {
  const params = buildQueryParams();
  params.append("file_format", format);
  const url = `/export/borrows?${params.toString()}`;
  window.open(url, "_blank");
}

selectAllCheckbox.addEventListener("change", (event) => {
  const checked = event.target.checked;
  document.querySelectorAll(".row-select").forEach((checkbox) => {
    checkbox.checked = checked;
  });
});

document.getElementById("import-readers").addEventListener("click", () => uploadFile("/import/readers", "reader-file"));
document.getElementById("import-books").addEventListener("click", () => uploadFile("/import/books", "book-file"));
document.getElementById("import-borrows").addEventListener("click", () => uploadFile("/import/borrows", "borrow-file"));
document.getElementById("refresh").addEventListener("click", loadBorrows);
document.getElementById("export-csv").addEventListener("click", () => exportBorrows("csv"));
document.getElementById("export-xlsx").addEventListener("click", () => exportBorrows("xlsx"));
document.getElementById("batch-return").addEventListener("click", () => applyBatch("/borrows/batch-return"));
document.getElementById("batch-delete").addEventListener("click", () => applyBatch("/borrows/batch-delete"));

document.addEventListener("DOMContentLoaded", loadBorrows);
