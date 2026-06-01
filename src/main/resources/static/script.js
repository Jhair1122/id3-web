// src/main/resources/static/script.js
const API_BASE = '';

async function fetchGains() {
    try {
        const res = await fetch(`${API_BASE}/api/gains`);
        const data = await res.json();
        displayGains(data);
    } catch (err) {
        console.error(err);
        document.getElementById('gain-table').innerHTML = '<p class="error">Error al cargar ganancias</p>';
    }
}

function displayGains(data) {
    const gains = data.gains;
    const totalEntropy = data.totalEntropy;
    const classDist = data.classDistribution;

    let html = '<table class="gain-table"><thead><tr><th>Atributo</th><th>Ganancia</th><th>Entropía Condicional</th></tr></thead><tbody>';
    gains.forEach(g => {
        const isHighest = g.gain === gains[0].gain;
        const rowClass = isHighest ? 'high-gain' : '';
        html += `<tr class="${rowClass}">
                    <td><strong>${g.attribute}</strong></td>
                    <td>${g.gain.toFixed(5)}</td>
                    <td>${g.conditionalEntropy.toFixed(5)}</td>
                 </tr>`;
    });
    html += '</tbody></table>';
    document.getElementById('gain-table').innerHTML = html;
    document.getElementById('entropy-info').innerHTML = `
        <strong>Entropía Total (S):</strong> ${totalEntropy.toFixed(6)}<br>
        <strong>Distribución de Clases:</strong> P=${classDist.counts.P || 0}, N=${classDist.counts.N || 0} (Total: ${classDist.total})
    `;
}

async function fetchDataset() {
    try {
        const res = await fetch(`${API_BASE}/api/dataset`);
        const data = await res.json();
        displayDataset(data);
    } catch (err) {
        console.error(err);
        document.getElementById('dataset-table').innerHTML = '<p>Error dataset</p>';
    }
}

function displayDataset(data) {
    const attrs = data.attributes;
    const rows = data.rows;
    if (!rows.length) {
        document.getElementById('dataset-table').innerHTML = '<p>No hay datos</p>';
        return;
    }
    let html = '<table class="dataset-table"><thead><tr>';
    attrs.forEach(attr => html += `<th>${attr}</th>`);
    html += '</tr></thead><tbody>';
    rows.forEach(row => {
        html += '<tr>';
        attrs.forEach(attr => html += `<td>${row[attr] || ''}</td>`);
        html += '</tr>';
    });
    html += '</tbody></table>';
    document.getElementById('dataset-table').innerHTML = html;
}

async function loadTreeGraph() {
    try {
        const res = await fetch(`${API_BASE}/api/tree/dot`);
        const dot = await res.text();
        if (!dot) throw new Error('No DOT content');
        const svg = Viz(dot, "svg");
        document.getElementById('tree-graph').innerHTML = svg;
    } catch (err) {
        console.error(err);
        document.getElementById('tree-graph').innerHTML = '<div class="error">Error generando árbol: ' + err.message + '</div>';
    }
}

async function loadAttributeForm() {
    try {
        const res = await fetch(`${API_BASE}/api/attributes`);
        const attrs = await res.json();
        const formDiv = document.getElementById('form-fields');
        let html = '';
        for (const [attr, values] of Object.entries(attrs)) {
            html += `<div class="form-group">
                        <label>${attr}</label>
                        <select name="${attr}" required>
                            <option value="">Seleccione</option>`;
            values.forEach(v => html += `<option value="${v}">${v}</option>`);
            html += `</select></div>`;
        }
        formDiv.innerHTML = html;
    } catch (err) {
        document.getElementById('form-fields').innerHTML = '<p>Error cargando atributos</p>';
    }
}

document.getElementById('prediction-form').addEventListener('submit', async (e) => {
    e.preventDefault();
    const formData = new FormData(e.target);
    const params = new URLSearchParams();
    for (let [key, value] of formData.entries()) {
        if (value) params.append(key, value);
    }
    try {
        const res = await fetch(`${API_BASE}/api/predict`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: params
        });
        const data = await res.json();
        const resultDiv = document.getElementById('prediction-result');
        if (data.prediction) {
            resultDiv.innerHTML = `🏷️ Predicción: <strong>${data.prediction}</strong>`;
            resultDiv.className = `prediction-result ${data.prediction.toLowerCase()}`;
        } else {
            resultDiv.innerHTML = `❌ Error: ${data.error}`;
            resultDiv.className = 'prediction-result';
        }
    } catch (err) {
        document.getElementById('prediction-result').innerHTML = 'Error en la solicitud';
    }
});

fetchGains();
fetchDataset();
loadTreeGraph();
loadAttributeForm();