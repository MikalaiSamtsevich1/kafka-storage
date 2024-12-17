const streamSaver = window.streamSaver;

document.getElementById('downloadForm').addEventListener('submit', async (event) => {
    event.preventDefault();

    const ownerName = document.getElementById('ownerName').value;
    const path = document.getElementById('path').value;
    const fileName = document.getElementById('fileName').value;
    const fileUUID = document.getElementById('fileUUID').value;

    const params = new URLSearchParams({
        owner: ownerName,
        path: path,
        fileName: fileName,
        fileUUID: fileUUID
    });

    const downloadUrl = `http://localhost:80/stream/download?${params.toString()}`;

    try {
        const fileStream = streamSaver.createWriteStream(fileName);
        const response = await fetch(downloadUrl);
        if (!response.ok) {
            throw new Error(`Failed to download: ${response.statusText}`);
        }

        const reader = response.body.getReader();
        const writer = fileStream.getWriter();

        while (true) {
            const {done, value} = await reader.read();
            if (done) break;
            await writer.write(value);
        }

        writer.close();
        alert('Download completed successfully!');
    } catch (error) {
        console.error(error);
        alert("Failed to download the file. Please try again.");
    }
});
