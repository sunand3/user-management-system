document.getElementById('uploadForm').addEventListener('submit', async (e) => {
    e.preventDefault();

    const fileInput = document.getElementById('fileInput');
    const statusDiv = document.getElementById('uploadStatus');
    const progressDiv = document.getElementById('uploadProgress');
    const progressFill = progressDiv.querySelector('.progress-fill');

    if (!fileInput.files || fileInput.files.length === 0) {
        showStatus('Please select a file', 'error');
        return;
    }

    const file = fileInput.files[0];

    if (!file.name.endsWith('.xlsx')) {
        showStatus('Please select a valid Excel file (.xlsx)', 'error');
        return;
    }

    const formData = new FormData();
    formData.append('file', file);

    try {
        // Show progress
        progressDiv.style.display = 'block';
        progressFill.style.width = '30%';
        statusDiv.style.display = 'none';

        const response = await fetch('/api/upload', {
            method: 'POST',
            body: formData
        });

        progressFill.style.width = '70%';

        const result = await response.json();

        progressFill.style.width = '100%';

        if (result.success) {
            showStatus(
                `Upload successful! Total: ${result.totalRecords}, ` +
                `Imported: ${result.successCount}, ` +
                `Skipped: ${result.failCount}`,
                'success'
            );
            fileInput.value = '';

            setTimeout(() => {
                progressDiv.style.display = 'none';
                progressFill.style.width = '0%';
            }, 2000);
        } else {
            showStatus(result.message || 'Upload failed', 'error');
            progressDiv.style.display = 'none';
            progressFill.style.width = '0%';
        }
    } catch (error) {
        showStatus('Error uploading file: ' + error.message, 'error');
        progressDiv.style.display = 'none';
        progressFill.style.width = '0%';
    }
});

function showStatus(message, type) {
    const statusDiv = document.getElementById('uploadStatus');
    statusDiv.textContent = message;
    statusDiv.className = `status-message ${type}`;
    statusDiv.style.display = 'block';
}