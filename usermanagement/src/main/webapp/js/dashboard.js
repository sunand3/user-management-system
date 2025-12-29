let users = [];
let userToDelete = null;

// Check authentication on page load
window.addEventListener('DOMContentLoaded', async () => {
    try {
        const response = await fetch('/api/auth/check');
        const result = await response.json();

        if (!result.authenticated) {
            window.location.href = 'login.html';
            return;
        }

        loadUsers();
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

async function loadUsers() {
    try {
        const response = await fetch('/api/users/');
        const result = await response.json();

        if (result.success) {
            users = result.users;
            displayUsers(users);
            updateStats(users.length);
        } else {
            showError('Failed to load users');
        }
    } catch (error) {
        showError('Error loading users: ' + error.message);
    }
}

function displayUsers(userList) {
    const tbody = document.getElementById('usersTableBody');

    if (userList.length === 0) {
        tbody.innerHTML = '<tr><td colspan="5" class="loading">No users found</td></tr>';
        return;
    }

    tbody.innerHTML = userList.map(user => `
        <tr>
            <td>${escapeHtml(user.name)}</td>
            <td>${escapeHtml(user.email)}</td>
            <td>${escapeHtml(user.phone)}</td>
            <td>${escapeHtml(user.gender)}</td>
            <td>
                <button class="action-btn delete" onclick="showDeleteModal('${user.id}')">Delete</button>
            </td>
        </tr>
    `).join('');
}

function updateStats(count) {
    document.getElementById('totalUsers').textContent = count;
}

function searchUsers() {
    const searchTerm = document.getElementById('searchInput').value.toLowerCase();

    if (!searchTerm) {
        displayUsers(users);
        return;
    }

    const filtered = users.filter(user =>
        user.name.toLowerCase().includes(searchTerm) ||
        user.email.toLowerCase().includes(searchTerm) ||
        user.phone.includes(searchTerm)
    );

    displayUsers(filtered);
}

// Allow search on Enter key
document.getElementById('searchInput').addEventListener('keypress', (e) => {
    if (e.key === 'Enter') {
        searchUsers();
    }
});

function showDeleteModal(userId) {
    userToDelete = userId;
    document.getElementById('deleteModal').style.display = 'block';
}

function closeDeleteModal() {
    userToDelete = null;
    document.getElementById('deleteModal').style.display = 'none';
}

async function confirmDelete() {
    if (!userToDelete) return;

    try {
        const response = await fetch(`/api/users/${userToDelete}`, {
            method: 'DELETE'
        });

        const result = await response.json();

        if (result.success) {
            closeDeleteModal();
            loadUsers();
        } else {
            alert('Failed to delete user: ' + result.message);
        }
    } catch (error) {
        alert('Error deleting user: ' + error.message);
    }
}

function showError(message) {
    const tbody = document.getElementById('usersTableBody');
    tbody.innerHTML = `<tr><td colspan="5" class="loading" style="color: red;">${escapeHtml(message)}</td></tr>`;
}

function escapeHtml(text) {
    const map = {
        '&': '&amp;',
        '<': '&lt;',
        '>': '&gt;',
        '"': '&quot;',
        "'": '&#039;'
    };
    return text.replace(/[&<>"']/g, m => map[m]);
}

// Close modal on outside click
window.onclick = function(event) {
    const modal = document.getElementById('deleteModal');
    if (event.target === modal) {
        closeDeleteModal();
    }
}