// Handle file selection and drop zone
document.addEventListener('DOMContentLoaded', function() {
	const dropZone = document.getElementById('dropZone');
	const fileInput = document.getElementById('archivoCsv');
	const fileName = document.getElementById('fileName');

	// Click on drop zone triggers file input
	dropZone.addEventListener('click', () => fileInput.click());

	// File input change event
	fileInput.addEventListener('change', function() {
		if (this.files.length) {
			fileName.textContent = this.files[0].name;
			dropZone.classList.add('hover');
		}
	});

	// Drag and drop events
	['dragenter', 'dragover', 'dragleave', 'drop'].forEach(eventName => {
		dropZone.addEventListener(eventName, preventDefaults, false);
	});

	function preventDefaults(e) {
		e.preventDefault();
		e.stopPropagation();
	}

	['dragenter', 'dragover'].forEach(eventName => {
		dropZone.addEventListener(eventName, highlight, false);
	});

	['dragleave', 'drop'].forEach(eventName => {
		dropZone.addEventListener(eventName, unhighlight, false);
	});

	function highlight() {
		dropZone.classList.add('hover');
	}

	function unhighlight() {
		dropZone.classList.remove('hover');
	}

	// Handle dropped files
	dropZone.addEventListener('drop', handleDrop, false);

	function handleDrop(e) {
		const dt = e.dataTransfer;
		const files = dt.files;

		if (files.length) {
			fileInput.files = files;
			fileName.textContent = files[0].name;
		}
	}
});

// Select algorithm when card is clicked
function selectAlgorithm(algorithm) {
	document.getElementById(algorithm + 'Option').checked = true;
}