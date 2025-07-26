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
