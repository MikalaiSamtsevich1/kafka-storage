async function deleteFile() {
    const owner = document.getElementById('ownerName').value.trim();
    const filePath = document.getElementById('filePathDelete').value.trim();
    const fileUUID = document.getElementById('fileUUIDDelete').value.trim();
    const fileName = document.getElementById('fileNameDelete').value.trim();
    const progressElement = document.getElementById('deleteProgress');

    if (!owner || !filePath || !fileUUID) {
        alert('Please fill in all fields for file deletion.');
        return;
    }

    const params = new URLSearchParams({
        owner: owner,
        path: filePath,
        fileName: fileName,
        fileUUID: fileUUID
    });

    const deleteUrl = `http://localhost:80/file?${params.toString()}`;

    try {
        const response = await fetch(deleteUrl, {method: 'DELETE'});
        if (response.ok) {
            progressElement.innerHTML = `File deleted successfully!`;
        } else {
            throw new Error('Failed to delete file');
        }
    } catch (error) {
        progressElement.innerHTML = `Error: ${error.message}`;
    }
}

async function deleteFolder() {
    const owner = document.getElementById('ownerName').value.trim();
    const folderPath = document.getElementById('folderPathDelete').value.trim();
    const progressElement = document.getElementById('deleteProgress');

    if (!owner || !folderPath) {
        alert('Please fill in all fields for folder deletion.');
        return;
    }

    const params = new URLSearchParams({
        owner: owner,
        path: folderPath
    });

    const deleteUrl = `http://localhost:80/folder?${params.toString()}`;

    try {
        const response = await fetch(deleteUrl, {method: 'DELETE'});
        if (response.ok) {
            progressElement.innerHTML = `Folder deleted successfully!`;
        } else {
            throw new Error('Failed to delete folder');
        }
    } catch (error) {
        progressElement.innerHTML = `Error: ${error.message}`;
    }
}

document.addEventListener('DOMContentLoaded', () => {
    const deleteFileButton = document.getElementById('deleteFileButton');
    if (deleteFileButton) {
        deleteFileButton.addEventListener('click', deleteFile);
    }

    const deleteFolderButton = document.getElementById('deleteFolderButton');
    if (deleteFolderButton) {
        deleteFolderButton.addEventListener('click', deleteFolder);
    }
});
