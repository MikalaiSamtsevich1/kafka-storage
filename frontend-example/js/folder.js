document.getElementById('folderForm').addEventListener('submit', async (event) => {
    event.preventDefault();

    const folderPath = document.getElementById('folderPath').value;
    const folderContentDiv = document.getElementById('folderContent');
    const ownerName = document.getElementById('ownerName').value;

    const params = new URLSearchParams({
        owner: ownerName,
        path: folderPath,
    });

    try {
        const response = await fetch(`http://localhost:80/folder?${params.toString()}`);
        if (!response.ok) {
            throw new Error(`Failed to get folder content: ${response.statusText}`);
        }

        const folderData = await response.json();
        folderContentDiv.innerHTML = `
            <h3>Content of ${folderPath}</h3>
            <table>
                <thead>
                    <tr>
                        <th>Type</th>
                        <th>Name</th>
                        <th>UUID</th>
                        <th>Size (Bytes)</th>
                        <th>MIME Type</th>
                    </tr>
                </thead>
                <tbody>
                    ${folderData.folders.map(folder => `
                        <tr>
                            <td class="icon folder">📁</td>
                            <td class="folder">${folder}</td>
                            <td colspan="2"></td>
                        </tr>
                    `).join('')}
                    ${folderData.files.map(file => `
                        <tr>
                            <td class="icon file">🗂</td>
                            <td class="file">${file.fileName}</td>
                            <td class="UUID">${file.fileUUID}</td>
                            <td>${file.fileSize}</td>
                            <td>${file.mimeType}</td>
                        </tr>
                    `).join('')}
                </tbody>
            </table>
        `;
    } catch (error) {
        folderContentDiv.innerHTML = `<p class="error">Error: ${error.message}</p>`;
    }
});
