// Check authentication on page load
window.addEventListener('DOMContentLoaded', async () => {
    try {
        const response = await fetch('/api/auth/check');
        const result = await response.json();

        if (!result.authenticated) {
            window.location.href = 'login.html';
            return;
        }

        loadMigrationStatus();
        loadMigratedRecords();
    } catch (error) {
        console.error('Authentication check failed:', error);
        window.location.href = 'login.html';
    }
});

// Logout handler
document.getElementById('logoutBtn').addEventListener('click', async (e) => {
    e.preventDefault();

    try {
        await fetch('/api/auth/logout', { method: 'POST' });
        window.location.href = 'login.html';
    } catch (error) {
        console.error('Logout failed:', error);
        window.location.href = 'login.html';
    }
});

async function loadMigrationStatus() {
    try {
        const response = await fetch('/api/migration/status');
        const result = await response.json();

        if (result.success) {
            document.getElementById('totalUsers').textContent = result.totalUsers;
            document.getElementById('migratedUsers').textContent = result.migratedUsers;
            document.getElementById('pendingUsers').textContent = result.pendingUsers;
        }
    } catch (error) {
        console.error('Error loading migration status:', error);
    }
}

async function loadMigratedRecords() {
    try {
        const response = await fetch('/api/migration/records?limit=100');
        const result = await response.json();

        if (result.success) {
            displayMigratedRecords(result.records);
        }
    } catch (error) {
        console.error('Error loading migrated records:', error);
    }
}

function displayMigratedRecords(records) {
    const tbody = document.getElementById('migratedTableBody');

    if (records.length === 0) {
        tbody.innerHTML = '<tr><td colspan="4" class="loading">No migrated records found</td></tr>';
        return;
    }

    tbody.innerHTML = records.map(record => `
        <tr>
            <td>${escapeHtml(record.id)}</td>
            <td>${escapeHtml(record.name)}</td>
            <td>${escapeHtml(record.email)}</td>
            <td>${escapeHtml(record.phone)}</td>
        </tr>
    `).join('');
}

async function startBulkMigration() {
    const btn = document.getElementById('migrateBtn');
    const statusDiv = document.getElementById('migrationStatus');
    const progressDiv = document.getElementById('migrationProgress');
    const progressBar = document.getElementById('progressBar');
    const progressText = document.getElementById('progressText');

    // Disable button
    btn.disabled = true;
    btn.textContent = 'Migrating...';

    // Show progress
    progressDiv.style.display = 'block';
    progressBar.style.width = '0%';
    progressText.textContent = 'Starting migration...';
    statusDiv.style.display = 'none';

    try {
        // Animate progress
        let progress = 0;
        const progressInterval = setInterval(() => {
            if (progress < 90) {
                progress += 10;
                progressBar.style.width = progress + '%';
                progressText.textContent = `Migrating users... ${progress}%`;
            }
        }, 500);

        const response = await fetch('/api/migration/bulk', {
            method: 'POST'
        });

        clearInterval(progressInterval);

        const result = await response.json();

        progressBar.style.width = '100%';
        progressText.textContent = 'Migration complete!';

        if (result.success) {
            statusDiv.textContent =
                `Migration completed! Total: ${result.total}, ` +
                `Success: ${result.success}, ` +
                `Failed: ${result.failed}`;
            statusDiv.className = 'status-message success';
            statusDiv.style.display = 'block';

            // Reload data
            setTimeout(() => {
                loadMigrationStatus();
                loadMigratedRecords();
                progressDiv.style.display = 'none';
            }, 2000);
        } else {
            statusDiv.textContent = 'Migration failed: ' + result.message;
            statusDiv.className = 'status-message error';
            statusDiv.style.display = 'block';
            progressDiv.style.display = 'none';
        }
    } catch (error) {
        statusDiv.textContent = 'Error during migration: ' + error.message;
        statusDiv.className = 'status-message error';
        statusDiv.style.display = 'block';
        progressDiv.style.display = 'none';
    } finally {
        btn.disabled = false;
        btn.textContent = 'Start Bulk Migration';
    }
}

function escapeHtml(text) {
    if (!text) return '';
    const map = {
        '&': '&amp;',
        '<': '&lt;',
        '>': '&gt;',
        '"': '&quot;',
        "'": '&#039;'
    };
    return String(text).replace(/[&<>"']/g, m => map[m]);
}