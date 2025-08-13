# AI Benchmark Tool

## Local Development

### Backend
```bash
# Build and run Spring Boot application
mvn spring-boot:run
```
The backend will start on http://localhost:8080

### Frontend
```bash
cd frontend
npm install
npm run dev
```
The frontend will start on http://localhost:5173

## Docker Build and Run

### Backend
```bash
docker build -t ai-benchmark-backend -f Dockerfile.backend .
docker run -p 8080:8080 ai-benchmark-backend
```

### Frontend
```bash
cd frontend
docker build -t ai-benchmark-frontend -f Dockerfile.frontend .
docker run -p 5173:5173 ai-benchmark-frontend
```

## Directory Structure
- `exec_configs/`: Execution configuration YAML files
- `task_sources/`: Task source YAML files
- `bench_status/`: Benchmark status files
- `bench_results/`: Benchmark results

## API Overview

The backend exposes a RESTful API for managing and running AI benchmarks:

- **/api/configs**: List, upload, and manage execution configuration files.
- **/api/tasks**: List, upload, and manage task source files.
- **/api/benchmarks**: Start new benchmarks, check status, and retrieve results.
- **/api/status**: Get the status of running or completed benchmarks.
- **/api/results**: Download or view benchmark results.

Each endpoint supports standard HTTP methods (GET, POST, DELETE) for CRUD operations and triggering benchmark runs.

## Project Description

### Backend

- **Framework**: Spring Boot (Java)
- **Responsibilities**:
  - Serves REST API for benchmark management.
  - Handles file uploads for configs and tasks.
  - Orchestrates benchmark execution and tracks status.
  - Stores results and exposes them via API.
- **Structure**:
  - Controllers for each resource (configs, tasks, benchmarks, status, results).
  - Service layer for business logic.
  - File-based storage for configs, tasks, status, and results.

### Frontend

- **Framework**: React (TypeScript)
- **Responsibilities**:
  - User interface for uploading configs and tasks.
  - Triggering and monitoring benchmarks.
  - Viewing benchmark status and results.
- **Structure**:
  - Main entry: `App.tsx`
  - Components for file upload, benchmark control, status display, and results view.
  - Communicates with backend via REST API.

## Frontend Features

- **Config and Task Upload**: Users can upload YAML files for execution configs and task sources.
- **Benchmark Control**: Start new benchmarks and monitor their progress.
- **Status Display**: View real-time status of running or completed benchmarks.
- **Results Viewer**: Download or inspect benchmark results.
- **Navigation**: Simple navigation between config/task management, benchmark control, and results.
