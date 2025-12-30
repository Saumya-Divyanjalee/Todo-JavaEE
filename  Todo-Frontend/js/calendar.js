// DOM Elements
const calendarGrid = document.getElementById("calendarGrid");
const calendarTitle = document.getElementById("calendarTitle");
const prevMonthBtn = document.getElementById("prevMonth");
const nextMonthBtn = document.getElementById("nextMonth");
const todayBtn = document.getElementById("todayBtn");
const taskModal = document.getElementById("taskModal");
const taskInput = document.getElementById("taskInput");
const taskDescription = document.getElementById("taskDescription");
const saveTaskBtn = document.getElementById("saveTask");
const deleteTaskBtn = document.getElementById("deleteTask");
const cancelBtn = document.getElementById("cancelBtn");
const closeBtn = document.querySelector(".close-btn");
const modalTitle = document.getElementById("modalTitle");
const modalDate = document.getElementById("modalDate");
const taskList = document.getElementById("taskList");

// State
let currentDate = new Date();
let selectedDay = null;
let tasks = {};

// Load tasks from localStorage
function loadTasks() {
    const saved = localStorage.getItem('tasks');
    tasks = saved ? JSON.parse(saved) : {};
}

// Save tasks to localStorage
function saveTasks() {
    localStorage.setItem('tasks', JSON.stringify(tasks));
}

// Format date as YYYY-MM-DD
function formatDate(year, month, day) {
    return `${year}-${String(month + 1).padStart(2, '0')}-${String(day).padStart(2, '0')}`;
}

// Parse date string
function parseDate(dateStr) {
    const [year, month, day] = dateStr.split('-').map(Number);
    return { year, month: month - 1, day };
}

// Render calendar
function renderCalendar(date) {
    calendarGrid.innerHTML = "";
    const year = date.getFullYear();
    const month = date.getMonth();

    calendarTitle.textContent = date.toLocaleString('default', {
        month: 'long',
        year: 'numeric'
    });

    const firstDay = new Date(year, month, 1).getDay();
    const lastDate = new Date(year, month + 1, 0).getDate();
    const today = new Date();

    // Add empty cells for days before month starts
    for (let i = 0; i < firstDay; i++) {
        const emptyDiv = document.createElement("div");
        emptyDiv.setAttribute('role', 'gridcell');
        emptyDiv.setAttribute('aria-label', 'Empty day');
        emptyDiv.style.cursor = "default";
        emptyDiv.style.background = "transparent";
        calendarGrid.appendChild(emptyDiv);
    }

    // Add days
    for (let day = 1; day <= lastDate; day++) {
        const dayDiv = document.createElement("div");
        dayDiv.setAttribute('role', 'gridcell');
        dayDiv.setAttribute('aria-label', `${day} ${calendarTitle.textContent}`);
        dayDiv.textContent = day;

        const dateKey = formatDate(year, month, day);

        // Check if today
        if (
            day === today.getDate() &&
            month === today.getMonth() &&
            year === today.getFullYear()
        ) {
            dayDiv.classList.add("today");
        }

        // Check if has task
        if (tasks[dateKey]) {
            dayDiv.classList.add("has-task");
            dayDiv.title = `Event: ${tasks[dateKey].name}\nNotes: ${tasks[dateKey].description || 'None'}`;
        }

        // Add click event with ripple animation
        dayDiv.addEventListener("click", (e) => {
            // Ripple effect
            const ripple = document.createElement('span');
            ripple.classList.add('ripple');
            const rect = dayDiv.getBoundingClientRect();
            const size = Math.max(rect.width, rect.height);
            const x = e.clientX - rect.left - size / 2;
            const y = e.clientY - rect.top - size / 2;
            ripple.style.width = ripple.style.height = size + 'px';
            ripple.style.left = x + 'px';
            ripple.style.top = y + 'px';
            dayDiv.appendChild(ripple);
            setTimeout(() => ripple.remove(), 600);
            openModal(dateKey);
        });

        // Add animation with delay
        dayDiv.style.animation = `fadeInUp 0.4s ease-out ${day * 0.02}s both`;

        calendarGrid.appendChild(dayDiv);
    }

    renderTaskList();
}

// Open modal
function openModal(dateKey) {
    selectedDay = dateKey;
    const task = tasks[dateKey];
    taskModal.setAttribute('aria-hidden', 'false');

    if (task) {
        modalTitle.textContent = "Edit Task";
        taskInput.value = task.name;
        taskDescription.value = task.description || "";
        deleteTaskBtn.style.display = "inline-block";
    } else {
        modalTitle.textContent = "Add Task";
        taskInput.value = "";
        taskDescription.value = "";
        deleteTaskBtn.style.display = "none";
    }

    const parsed = parseDate(dateKey);
    const dateObj = new Date(parsed.year, parsed.month, parsed.day);
    modalDate.textContent = dateObj.toLocaleDateString('default', {
        weekday: 'long',
        year: 'numeric',
        month: 'long',
        day: 'numeric'
    });

    taskModal.classList.add("active");
    taskInput.focus();
}

// Close modal
function closeModal() {
    taskModal.classList.remove("active");
    taskModal.setAttribute('aria-hidden', 'true');
    selectedDay = null;
}

// Save task
function saveTask() {
    const taskName = taskInput.value.trim();
    const taskDesc = taskDescription.value.trim();

    if (taskName === "") {
        taskInput.style.borderColor = "#e53e3e";
        setTimeout(() => {
            taskInput.style.borderColor = "";
        }, 1000);
        return;
    }

    tasks[selectedDay] = {
        name: taskName,
        description: taskDesc,
        date: selectedDay
    };

    saveTasks();
    closeModal();
    renderCalendar(currentDate);
}

// Delete task
function deleteTask() {
    if (confirm("Are you sure you want to delete this task?")) {
        delete tasks[selectedDay];
        saveTasks();
        closeModal();
        renderCalendar(currentDate);
    }
}

// Render task list
function renderTaskList() {
    const sortedDates = Object.keys(tasks).sort();
    const today = new Date();
    const todayStr = formatDate(today.getFullYear(), today.getMonth(), today.getDate());

    // Filter upcoming tasks (today and future)
    const upcomingTasks = sortedDates.filter(date => date >= todayStr);

    if (upcomingTasks.length === 0) {
        taskList.innerHTML = '<p class="no-tasks" role="status">No tasks scheduled</p>';
        return;
    }

    taskList.innerHTML = upcomingTasks.map(dateKey => {
        const task = tasks[dateKey];
        const parsed = parseDate(dateKey);
        const dateObj = new Date(parsed.year, parsed.month, parsed.day);
        const dateStr = dateObj.toLocaleDateString('default', {
            month: 'short',
            day: 'numeric',
            year: 'numeric'
        });

        return `
            <div class="task-item" onclick="openModalFromList('${dateKey}')" role="listitem" tabindex="0">
                <div class="task-date">${dateStr}</div>
                <div class="task-name">${task.name}</div>
                ${task.description ? `<div class="task-description">${task.description}</div>` : ''}
            </div>
        `;
    }).join('');
}

// Open modal from task list
window.openModalFromList = function(dateKey) {
    openModal(dateKey);
};

// Navigate months
prevMonthBtn.addEventListener("click", () => {
    currentDate.setMonth(currentDate.getMonth() - 1);
    renderCalendar(currentDate);
});

nextMonthBtn.addEventListener("click", () => {
    currentDate.setMonth(currentDate.getMonth() + 1);
    renderCalendar(currentDate);
});

todayBtn.addEventListener("click", () => {
    currentDate = new Date();
    renderCalendar(currentDate);
});

// Modal controls
saveTaskBtn.addEventListener("click", saveTask);
deleteTaskBtn.addEventListener("click", deleteTask);
cancelBtn.addEventListener("click", closeModal);
closeBtn.addEventListener("click", closeModal);

// Close modal on overlay click
document.querySelector(".modal-overlay").addEventListener("click", closeModal);

// Close modal on escape key
document.addEventListener("keydown", (e) => {
    if (e.key === "Escape" && taskModal.classList.contains("active")) {
        closeModal();
    }
});

// Save task on enter key (when not in textarea)
taskInput.addEventListener("keydown", (e) => {
    if (e.key === "Enter" && !e.shiftKey) {
        e.preventDefault();
        saveTask();
    }
});

// Add click handler for task list items (keyboard support)
taskList.addEventListener("keydown", (e) => {
    if (e.key === "Enter" && e.target.classList.contains("task-item")) {
        const dateKey = e.target.getAttribute("data-date"); // Add data-date if needed, but using onclick for simplicity
        openModalFromList(e.target.onclick.toString().match(/'([^']+)'/)[1]); // Hacky but works; consider refactoring
    }
});

// Initialize
loadTasks();
renderCalendar(currentDate);

// Add some sample tasks for demonstration (will persist)
const today = new Date();
const tomorrow = new Date(today);
tomorrow.setDate(tomorrow.getDate() + 1);
const nextWeek = new Date(today);
nextWeek.setDate(nextWeek.getDate() + 7);

const sampleTasks = {
    [formatDate(today.getFullYear(), today.getMonth(), today.getDate())]: {
        name: "Team Meeting",
        description: "Discuss Q1 objectives and brainstorm ideas",
        date: formatDate(today.getFullYear(), today.getMonth(), today.getDate())
    },
    [formatDate(tomorrow.getFullYear(), tomorrow.getMonth(), tomorrow.getDate())]: {
        name: "Project Deadline",
        description: "Submit final deliverables to client review",
        date: formatDate(tomorrow.getFullYear(), tomorrow.getMonth(), tomorrow.getDate())
    },
    [formatDate(nextWeek.getFullYear(), nextWeek.getMonth(), nextWeek.getDate())]: {
        name: "Client Presentation",
        description: "Present new design concepts with interactive demo",
        date: formatDate(nextWeek.getFullYear(), nextWeek.getMonth(), nextWeek.getDate())
    }
};

Object.assign(tasks, sampleTasks);
saveTasks();
renderCalendar(currentDate);