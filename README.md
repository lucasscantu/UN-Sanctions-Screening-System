# UN Sanctions Screening System

A complete, functional web application for screening individuals and entities against the United Nations Security Council Consolidated Sanctions List with local AI name matching.

## Overview

This application allows users to:
- Search the official UN sanctions list imported from XML
- Identify potentially matching records using deterministic similarity algorithms
- Get AI-powered linguistic analysis from a locally hosted Ollama model
- Calculate name similarity scores for screening assistance
- Inspect full details of each potential match

**Important**: This is a screening and research assistance tool. It **never** automatically concludes that a person is sanctioned solely because their name resembles a record in the database.

## Features

- **Official UN Data**: Downloads and parses the official UN Security Council Consolidated Sanctions List XML
- **Deterministic Matching**: Uses Levenshtein, Jaro-Winkler, and token-based similarity algorithms
- **Local AI Analysis**: Uses Ollama for linguistic analysis without external API calls
- **Multi-stage Pipeline**: Candidate retrieval → similarity calculation → AI analysis → final ranking
- **REST API**: Fully documented with OpenAPI/Swagger
- **React Frontend**: Modern, responsive UI with TypeScript and Tailwind CSS
- **Docker Support**: Full containerization with Docker Compose

## Technology Stack

### Backend
- Java 21
- Spring Boot 3.5.x
- Maven
- PostgreSQL
- Flyway (database migrations)
- Spring Security
- Spring Data JPA

### Frontend
- React 18
- TypeScript
- Vite
- Tailwind CSS
- TanStack Query
- React Hook Form + Zod
- React Router

### Local AI
- Ollama
- Qwen3 4B (configurable)
- HTTP communication with Ollama API

## Quick Start

### Prerequisites

- Docker and Docker Compose
- Node.js 20+ (for frontend development)
- Java 21 (for backend development)
- PostgreSQL 16+ (for backend development)

### Using Docker Compose (Recommended)

1. **Clone the repository**:
   ```bash
   git clone <repository-url>
   cd sanctions-screening
   ```

2. **Configure environment**:
   ```bash
   cp .env.example .env
   # Edit .env with your configuration
   ```

3. **Start all services**:
   ```bash
   docker-compose up -d
   ```

4. **Access the application**:
   - Frontend: http://localhost:5173
   - Backend API: http://localhost:8080/api
   - API Docs: http://localhost:8080/api/swagger-ui.html

5. **Install Ollama model** (optional, for AI analysis):
   ```bash
   # Run Ollama separately or use the Docker service
   ollama pull qwen3:4b
   ```

### Local Development

#### Backend

1. Start PostgreSQL (via Docker or local installation)
2. Configure database connection in `backend/src/main/resources/application-dev.yml`
3. Build and run:
   ```bash
   cd backend
   ./mvnw spring-boot:run
   ```

#### Frontend

1. Install dependencies:
   ```bash
   cd frontend
   npm install
   ```
2. Start development server:
   ```bash
   npm run dev
   ```

## Configuration

### Environment Variables

Copy `.env.example` to `.env` and update the values:

```bash
# Database
DB_URL=jdbc:postgresql://localhost:5432/sanctions
DB_USERNAME=sanctions
DB_PASSWORD=sanctions

# Ollama
OLLAMA_ENABLED=true
OLLAMA_BASE_URL=http://localhost:11434
OLLAMA_MODEL=qwen3:4b

# UN Sanctions XML
SANCTIONS_XML_URL=https://www.un.org/securitycouncil/content/un-sc-consolidated-list-xml

# Search
SEARCH_CANDIDATE_LIMIT=50
SEARCH_MIN_SCORE_FILTER=0
```

### Database Setup

The application uses Flyway for database migrations. On first startup, the schema will be created automatically.

### Ollama Setup

1. Install Ollama: https://ollama.ai
2. Pull the model: `ollama pull qwen3:4b`
3. Start Ollama server: `ollama serve`
4. Configure the backend to use the correct URL:
   - On macOS/Windows (Docker Desktop): `http://host.docker.internal:11434`
   - On Linux: `http://172.17.0.1:11434` or run Ollama on the host

## API Documentation

The backend provides a complete REST API with OpenAPI documentation:

- **API Base URL**: `/api`
- **Swagger UI**: http://localhost:8080/api/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/api/v3/api-docs

### Main Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/search?name={name}` | Search sanctions list |
| POST | `/api/search` | Search with POST body |
| GET | `/api/records/{id}` | Get record by ID |
| GET | `/api/records/reference/{ref}` | Get record by UN reference |
| GET | `/api/admin/sync/status` | Get import status |
| POST | `/api/admin/sync` | Trigger manual sync |
| GET | `/api/ollama/health` | Check Ollama health |

## Project Structure

```
sanctions-screening/
├── backend/                 # Spring Boot backend
│   ├── src/main/java/
│   │   └── com/sanctions/screening/
│   │       ├── config/        # Configuration classes
│   │       ├── controller/    # REST controllers
│   │       ├── dto/           # Data transfer objects
│   │       ├── entity/        # JPA entities
│   │       ├── exception/     # Custom exceptions
│   │       ├── importer/      # XML importer
│   │       │   └── jaxb/      # JAXB classes for XML parsing
│   │       ├── matching/      # Name matching algorithms
│   │       ├── ollama/        # Ollama integration
│   │       ├── repository/    # Spring Data repositories
│   │       ├── security/      # Security configuration
│   │       ├── service/       # Business services
│   │       └── util/          # Utility classes
│   └── src/main/resources/
│       ├── db/migration/     # Flyway migrations
│       ├── templates/        # Prompt templates
│       └── application*.yml   # Configuration files
│
├── frontend/                # React frontend
│   ├── src/
│   │   ├── components/       # React components
│   │   │   ├── layout/       # Layout components
│   │   │   ├── ui/           # UI components (shadcn/ui)
│   │   │   └── ...
│   │   ├── hooks/            # Custom React hooks
│   │   ├── lib/              # Library functions
│   │   ├── pages/            # Page components
│   │   ├── types/            # TypeScript types
│   │   └── App.tsx           # Main app component
│   └── vite.config.ts        # Vite configuration
│
├── docker/                  # Docker configurations
│   ├── Dockerfile.backend
│   ├── Dockerfile.frontend
│   └── nginx.conf
│
├── docker-compose.yml       # Docker Compose configuration
├── .env.example             # Environment template
├── .gitignore
└── README.md
```

## Database Schema

The application uses the following main entities:

- **SanctionedRecord**: Main sanctions records (individuals and entities)
- **NameAlias**: Alternative names and aliases
- **BiographicalDetail**: Dates of birth, nationalities, etc.
- **SanctionsProgram**: Committee and program information
- **Address**: Address information
- **Identifier**: Identification documents
- **ImportJob**: Import history and status
- **SearchHistory**: Search query history

## Name Matching Algorithm

The system uses a multi-stage matching pipeline:

### Stage 1: Deterministic Candidate Retrieval
- Unicode normalization (NFKD decomposition)
- Case and whitespace normalization
- Accent and punctuation removal
- PostgreSQL full-text search for initial candidates

### Stage 2: Objective Similarity Calculation
- **Levenshtein Similarity**: Edit distance based (30% weight)
- **Jaro-Winkler Similarity**: Prefix-sensitive string similarity (35% weight)
- **Token-based Similarity**: Jaccard similarity on name tokens (25% weight)
- **Alias Boost**: Additional score for matching aliases (10% weight)

### Stage 3: Local AI Analysis (Optional)
- Uses Ollama to analyze linguistic variations
- Checks for transliteration differences
- Identifies spelling variants and name order differences
- Returns structured JSON with assessment (HIGH/MEDIUM/LOW/INDETERMINATE)

### Stage 4: Final Ranking
- Combines all similarity scores
- Normalizes to 0-100 scale
- Sorts by combined score
- Clearly labels as "experimental" until calibrated

## Important Disclaimers

1. **Name Similarity ≠ Identity**: Similar names do not prove that two people are the same.
2. **Human Review Required**: All potential matches must be reviewed by a human.
3. **Official Source**: Always verify information from the official UN source.
4. **Experimental Scores**: Similarity scores are experimental and for screening assistance only.
5. **No Legal Determination**: This system does not make definitive legal or identity determinations.

## Security

- Secure XML parsing (XXE protection)
- Input validation on all API endpoints
- Parameterized database queries
- Rate limiting for expensive operations
- Secure handling of environment variables
- No credentials committed to source control
- Ollama port not exposed publicly

## Performance

- Indexed database searches
- Candidate shortlist limits (configurable)
- Model timeouts (configurable)
- Request concurrency limits
- Caching for frequent queries
- Graceful degradation when Ollama unavailable

## Testing

### Backend Tests
```bash
cd backend
./mvnw test
```

### Frontend Tests
```bash
cd frontend
npm run test
```

### Test Coverage
- XML importer tests (valid/invalid XML, network errors)
- Similarity engine tests (exact matches, variations, false positives)
- Ollama integration tests (mocked responses)
- REST API tests (validation, authorization)
- Frontend tests (form validation, rendering)

## Troubleshooting

### Ollama Connection Issues

**Problem**: Backend cannot connect to Ollama

**Solutions**:
1. **Docker Desktop (macOS/Windows)**: Use `http://host.docker.internal:11434`
2. **Linux**: Use `http://172.17.0.1:11434` or run Ollama on the host
3. **Verify Ollama is running**: `curl http://localhost:11434/api/tags`
4. **Check model**: `ollama list`

### Database Connection Issues

**Problem**: Backend cannot connect to PostgreSQL

**Solutions**:
1. Verify PostgreSQL is running: `docker ps` (if using Docker)
2. Check connection string in `.env`
3. Test connection: `psql -h localhost -U sanctions -d sanctions`

### XML Import Issues

**Problem**: Import fails or returns no records

**Solutions**:
1. Check the XML URL is accessible: `curl -I $SANCTIONS_XML_URL`
2. Verify secure XML parsing is enabled
3. Check for XXE protection settings
4. Review import logs for specific errors

### Frontend API Connection Issues

**Problem**: Frontend cannot connect to backend API

**Solutions**:
1. Verify backend is running: `curl http://localhost:8080/actuator/health`
2. Check CORS configuration in `.env`
3. Verify frontend API base URL in Vite config

## Deployment

### Production Deployment

1. **Build Docker images**:
   ```bash
   docker-compose build
   ```

2. **Configure production environment**:
   ```bash
   # Edit docker-compose.yml for production settings
   # Or create a .env.production file
   ```

3. **Start services**:
   ```bash
   docker-compose -f docker-compose.yml -f docker-compose.prod.yml up -d
   ```

4. **Set up reverse proxy** (e.g., Nginx, Traefik) for HTTPS

### Kubernetes Deployment

For Kubernetes deployments, you would need to:
1. Create Kubernetes manifests for each service
2. Configure persistent volumes for PostgreSQL and Ollama
3. Set up ingress controllers
4. Configure secrets for database credentials

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/your-feature`)
3. Commit your changes (`git commit -am 'Add some feature'`)
4. Push to the branch (`git push origin feature/your-feature`)
5. Open a Pull Request

## License

This project is licensed under the Apache License 2.0 - see the LICENSE file for details.

## Acknowledgments

- United Nations Security Council for the official sanctions list
- Ollama for the local AI runtime
- Spring Boot community for the excellent framework
- React community for the frontend ecosystem

---

**Data Source**: United Nations Security Council Consolidated Sanctions List

**Official Website**: https://www.un.org/securitycouncil/content/un-sc-consolidated-list

**Last Updated**: This README was last updated for version 1.0.0
