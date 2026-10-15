document.addEventListener("DOMContentLoaded", function () {
    // 1. File Upload Dropzone & Selection Handling
    const dropzone = document.getElementById("dropzone");
    const fileInput = document.getElementById("fileInput");
    const previewContainer = document.getElementById("previewContainer");
    const imagePreview = document.getElementById("imagePreview");
    const fileInfoText = document.getElementById("fileInfoText");
    const submitBtn = document.getElementById("analyzeSubmitBtn");
    const loadingState = document.getElementById("scanLoadingState");
    const scanForm = document.getElementById("scanForm");

    if (fileInput) {
        // Handle native change event
        fileInput.addEventListener("change", function (e) {
            if (e.target.files && e.target.files.length > 0) {
                handleFileSelection(e.target.files[0]);
            }
        });
    }

    if (dropzone && fileInput) {
        dropzone.addEventListener("dragover", function (e) {
            e.preventDefault();
            e.stopPropagation();
            dropzone.classList.add("dragover");
        });

        dropzone.addEventListener("dragleave", function (e) {
            e.preventDefault();
            e.stopPropagation();
            dropzone.classList.remove("dragover");
        });

        dropzone.addEventListener("drop", function (e) {
            e.preventDefault();
            e.stopPropagation();
            dropzone.classList.remove("dragover");

            if (e.dataTransfer && e.dataTransfer.files && e.dataTransfer.files.length > 0) {
                const droppedFile = e.dataTransfer.files[0];
                try {
                    const dt = new DataTransfer();
                    dt.items.add(droppedFile);
                    fileInput.files = dt.files;
                } catch (err) {
                    console.log("DataTransfer assignment fallback:", err);
                }
                handleFileSelection(droppedFile);
            }
        });
    }

    // Helper: Handle and preview selected image file
    function handleFileSelection(file) {
        if (!file) return;

        // Validation: Max 25MB
        if (file.size > 25 * 1024 * 1024) {
            alert("File is too large! Please select an image under 25MB.");
            fileInput.value = "";
            return;
        }

        // Friendly size format
        const sizeStr = file.size > 1024 * 1024
            ? (file.size / (1024 * 1024)).toFixed(1) + " MB"
            : Math.round(file.size / 1024) + " KB";

        const reader = new FileReader();
        reader.onload = function (e) {
            if (imagePreview) {
                imagePreview.src = e.target.result;
            }
            if (fileInfoText) {
                fileInfoText.innerHTML = `<strong>${file.name}</strong> (${sizeStr}) &bull; <span class="text-success">Ready to analyze</span>`;
            }
            if (previewContainer) {
                previewContainer.classList.remove("d-none");
            }
            if (submitBtn) {
                submitBtn.removeAttribute("disabled");
                submitBtn.classList.remove("btn-secondary");
                submitBtn.classList.add("btn-primary");
                submitBtn.innerHTML = `<i class="bi bi-stars me-1"></i> Analyze "${file.name}" (+2 EcoPoints)`;
            }
        };
        reader.readAsDataURL(file);
    }

    // 2. Global function for 1-Click Sample Test Buttons
    window.loadSampleImage = function (url, filename) {
        fetch(url)
            .then(res => res.blob())
            .then(blob => {
                const file = new File([blob], filename, { type: "image/png" });
                try {
                    const dt = new DataTransfer();
                    dt.items.add(file);
                    if (fileInput) {
                        fileInput.files = dt.files;
                    }
                } catch (e) {
                    console.log("Could not set fileInput.files directly:", e);
                }
                handleFileSelection(file);
            })
            .catch(err => {
                console.error("Error loading sample image:", err);
                alert("Could not load sample image: " + err.message);
            });
    };

    // 3. Form submission animation
    if (scanForm && submitBtn && loadingState) {
        scanForm.addEventListener("submit", function () {
            submitBtn.classList.add("d-none");
            loadingState.classList.remove("d-none");
        });
    }

    // 4. Chart.js Initialization for Category Breakdown
    const chartCanvas = document.getElementById("categoryChart");
    if (chartCanvas && window.Chart) {
        try {
            const rawLabels = chartCanvas.getAttribute("data-labels");
            const rawData = chartCanvas.getAttribute("data-values");

            const labels = rawLabels ? JSON.parse(rawLabels) : [];
            const data = rawData ? JSON.parse(rawData) : [];

            if (labels.length > 0 && data.length > 0) {
                const palette = [
                    "#10b981", // Emerald (Plastic)
                    "#3b82f6", // Blue (Paper)
                    "#f59e0b", // Amber (Organic)
                    "#64748b", // Slate (Metal)
                    "#ef4444", // Rose / Red (Batteries / Hazardous)
                    "#8b5cf6", // Purple (Glass)
                    "#06b6d4", // Cyan (E-waste)
                    "#9ca3af"  // Gray (General)
                ];

                new Chart(chartCanvas, {
                    type: "doughnut",
                    data: {
                        labels: labels,
                        datasets: [{
                            data: data,
                            backgroundColor: palette.slice(0, labels.length),
                            borderWidth: 2,
                            borderColor: "#ffffff",
                            hoverOffset: 6
                        }]
                    },
                    options: {
                        responsive: true,
                        maintainAspectRatio: false,
                        plugins: {
                            legend: {
                                position: "bottom",
                                labels: {
                                    boxWidth: 12,
                                    padding: 12,
                                    font: { family: "'Inter', sans-serif", size: 12 }
                                }
                            },
                            tooltip: {
                                callbacks: {
                                    label: function (ctx) {
                                        return ` ${ctx.label}: ${ctx.raw} item(s)`;
                                    }
                                }
                            }
                        },
                        cutout: "68%"
                    }
                });
            }
        } catch (e) {
            console.warn("Could not initialize Chart.js:", e);
        }
    }
});
