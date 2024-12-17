function getOwnerName() {
    const ownerNameInput = document.getElementById('ownerName');
    const ownerName = ownerNameInput.value.trim();

    if (!ownerName) {
        alert("Please enter the Owner Name.");
        throw new Error("Owner Name is required.");
    }

    return ownerName;
}

export async function uploadFiles() {
    const files = document.querySelector('input[name="folder"]').files;
    const chunkSize = 500 * 1024;
    const progressElement = document.getElementById("progress");

    progressElement.innerHTML = "";

    const ownerName = getOwnerName();

    for (const file of files) {
        let uploadedBytes = 0;

        const path = file.webkitRelativePath;
        const fileName = file.name;
        const dirPath = path.substring(0, path.lastIndexOf('/'));

        const uuid = crypto.randomUUID();
        const params = new URLSearchParams({
            fileName: fileName,
            owner: ownerName,
            dirPath: dirPath,
            uuid,
        });

        const uploadUrl = `http://localhost:80/stream/upload?${params.toString()}`;

        while (uploadedBytes < file.size) {
            const chunk = file.slice(uploadedBytes, uploadedBytes + chunkSize);

            const response = await fetch(uploadUrl, {
                method: 'POST',
                headers: {
                    'Content-Range': `bytes ${uploadedBytes}-${uploadedBytes + chunk.size - 1}/${file.size}`,
                    'Content-Type': 'application/octet-stream',
                },
                body: chunk
            });

            if (!response.ok) {
                throw new Error(`Failed to upload chunk for file ${file.name}`);
            }

            uploadedBytes += chunk.size;
            progressElement.innerHTML = `Uploading ${file.name}: ${(uploadedBytes / file.size * 100).toFixed(2)}%`;
        }

        progressElement.innerHTML += `<br>Uploaded ${file.name} successfully!<br>`;
    }
}

export async function upload() {
    const fileInput = document.getElementById('singleFileInput');
    const file = fileInput.files[0];
    const chunkSize = 500 * 1024;
    const progressElement = document.getElementById("progress");

    if (!file) {
        progressElement.innerHTML = "No file selected!";
        return;
    }

    progressElement.innerHTML = "";

    const ownerName = getOwnerName();

    let uploadedBytes = 0;
    const fileName = file.name;

    const uuid = crypto.randomUUID();
    const params = new URLSearchParams({
        fileName: fileName,
        owner: ownerName,
        dirPath: '/',
        uuid,
    });

    const uploadUrl = `http://localhost:80/stream/upload?${params.toString()}`;

    while (uploadedBytes < file.size) {
        const chunk = file.slice(uploadedBytes, uploadedBytes + chunkSize);

        const response = await fetch(uploadUrl, {
            method: 'POST',
            headers: {
                'Content-Range': `bytes ${uploadedBytes}-${uploadedBytes + chunk.size - 1}/${file.size}`,
                'Content-Type': 'application/octet-stream',
            },
            body: chunk
        });

        if (!response.ok) {
            throw new Error(`Failed to upload chunk for file ${file.name}`);
        }

        uploadedBytes += chunk.size;
        progressElement.innerHTML = `Uploading ${file.name}: ${(uploadedBytes / file.size * 100).toFixed(2)}%`;
    }

    progressElement.innerHTML += `<br>Uploaded ${file.name} successfully!<br>`;
}

document.addEventListener('DOMContentLoaded', () => {
    const uploadButton = document.getElementById('uploadButton');
    if (uploadButton) {
        uploadButton.addEventListener('click', uploadFiles);
    }

    const uploadSingleButton = document.getElementById('uploadSingleButton');
    if (uploadSingleButton) {
        uploadSingleButton.addEventListener('click', upload);
    }
});
