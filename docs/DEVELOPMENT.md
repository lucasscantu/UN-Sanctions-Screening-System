# Development Guide

This guide covers development setup, workflow, and best practices for the UN Sanctions Screening System.

## Development Environment Setup

### Prerequisites

- **Java 21**: Required for Spring Boot 3.5
- **Node.js 20+**: For frontend development
- **PostgreSQL 16+**: Database server
- **Docker and Docker Compose**: For containerized development
- **Ollama** (optional): For local AI analysis

### Installation

#### Backend

1. Navigate to backend directory:
   ```bash
   cd backend
   ```

2. Build with Maven:
   ```bash
   ./mvnw clean install
   ```

3. Run with development profile:
   ```bash
   ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
   ```

#### Frontend

1. Navigate to frontend directory:
   ```bash
   cd frontend
   ```

2. Install dependencies:
   ```bash
   npm install
   ```

3. Start development server:
   ```bash
   npm run dev
   ```

#### Database

Using Docker (recommended):
```bash
docker run --name sanctions-postgres -e POSTGRES_PASSWORD=sanctions -e POSTGRES_USER=sanctions -e POSTGRES_DB=sanctions -p 5432:5432 -d postgres:16-alpine
```

Or install PostgreSQL locally and create the database:
```bash
createdb sanctions
createuser sanctions -P
```

#### Ollama (Optional)

1. Install Ollama: https://ollama.ai
2. Pull the model:
   ```bash
   ollama pull qwen3:4b
   ```
3. Start the server:
   ```bash
   ollama serve
   ```

## Development Workflow

### Backend Development

#### Code Structure

```
backend/src/main/java/com/sanctions/screening/
├── config/           # Configuration classes
├── controller/       # REST API controllers
├── dto/              # Data transfer objects
├── entity/           # JPA entities
├── exception/        # Custom exceptions
├── importer/         # XML import functionality
│   └── jaxb/         # JAXB classes for XML parsing
├── matching/         # Name matching algorithms
├── ollama/           # Ollama integration
├── repository/       # Spring Data repositories
├── security/         # Security configuration
├── service/          # Business logic services
└── util/             # Utility classes
```

#### Adding New Features

1. Create the entity class in `entity/`
2. Create the repository interface in `repository/`
3. Create the DTO in `dto/`
4. Add business logic in `service/`
5. Create the controller in `controller/`
6. Add Flyway migration in `resources/db/migration/`

#### Database Migrations

Use Flyway for database schema changes:

1. Create a new migration file: `V2__your_migration.sql`
2. Increment the version number sequentially
3. Test the migration locally before committing

### Frontend Development

#### Code Structure

```
frontend/src/
├── components/        # React components
│   ├── layout/        # Layout components (Header, Footer, etc.)
│   ├── ui/            # UI components (shadcn/ui)
│   └── ...
├── hooks/            # Custom React hooks
├── lib/              # Library functions and API clients
├── pages/            # Page components
├── types/            # TypeScript type definitions
└── App.tsx           # Main application component
```

#### Adding New Components

1. Create the component file in the appropriate directory
2. Add TypeScript types if needed
3. Use existing UI components from `components/ui/`
4. Follow the shadcn/ui pattern for new UI components

#### API Integration

Use the provided API client in `lib/api.ts`:

```typescript
import { searchApi, recordsApi } from '@/lib/api'

// Search example
const results = await searchApi.search({ name: 'John Doe' })

// Get record example
const record = await recordsApi.getById(123)
```

### Common Tasks

#### Adding a New API Endpoint

1. Create a controller method:
   ```java
   @RestController
   @RequestMapping("/api/new-resource")
   public class NewResourceController {
       @GetMapping
       public ResponseEntity<NewResourceDto> getResource() {
           // Implementation
       }
   }
   ```

2. Add API documentation with Swagger annotations:
   ```java
   @Operation(summary = "Get resource", description = "Retrieve a resource by ID")
   @GetMapping("/{id}")
   public ResponseEntity<NewResourceDto> getById(@PathVariable Long id) {
       // Implementation
   }
   ```

3. Create the DTO and service as needed

#### Adding a New Page

1. Create the page component in `pages/`:
   ```tsx
   export default function NewPage() {
     return <div>New Page Content</div>
   }
   ```

2. Add route in `App.tsx`:
   ```tsx
   <Route path="/new-page" element={<NewPage />} />
   ```

3. Add navigation link in `Header.tsx` if needed

## Testing

### Backend Testing

Run all tests:
```bash
cd backend
./mvnw test
```

Run specific test class:
```bash
./mvnw test -Dtest=XmlSanctionsImporterTest
```

Run with coverage:
```bash
./mvnw test -Dcoverage.format=HTML
```

### Frontend Testing

Run tests:
```bash
cd frontend
npm run test
```

Run with UI:
```bash
npm run test:ui
```

Run with coverage:
```bash
npm run test:coverage
```

### Test Data

Create test data in:
- `backend/src/test/resources/` for backend tests
- Mock data in frontend tests

## Debugging

### Backend Debugging

Enable debug logging:
```yaml
# application-dev.yml
logging:
  level:
    com.sanctions.screening: DEBUG
    org.springframework: INFO
```

Remote debugging with IntelliJ:
1. Add to `pom.xml`:
   ```xml
   <profiles>
     <profile>
       <id>debug</id>
       <properties>
         <spring-boot.run.jvmArguments>-Xdebug -Xrunjdwp:5005/server=*,suspend=n</spring-boot.run.jvmArguments>
       </properties>
     </profile>
   </profiles>
   ```
2. Run with debug profile:
   ```bash
   ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev,debug
   ```
3. Attach debugger on port 5005

### Frontend Debugging

Debug in browser:
```bash
npm run dev
```

Debug with VS Code:
1. Install Debugger for Chrome/Firefox
2. Add launch configuration:
   ```json
   {
     "type": "chrome",
     "request": "launch",
     "name": "Launch Chrome against localhost",
     "url": "http://localhost:5173",
     "webRoot": "${workspaceFolder}/frontend"
   }
   ```

## Code Quality

### Formatting

Backend uses Spotless (configured in pom.xml):
```bash
./mvnw spotless:apply
```

Frontend uses Prettier (via Vite):
```bash
npm run format
```

### Linting

Backend uses Checkstyle:
```bash
./mvnw checkstyle:check
```

Frontend uses ESLint:
```bash
npm run lint
```

### Code Review Checklist

- [ ] All tests pass
- [ ] No console.log statements
- [ ] Proper error handling
- [ ] Input validation
- [ ] Security considerations (SQL injection, XSS, etc.)
- [ ] Documentation updated
- [ ] Code follows existing patterns
- [ ] No hardcoded credentials
- [ ] Environment variables used for configuration

## Docker Development

### Using Docker Compose

Start all services:
```bash
docker-compose up -d
```

View logs:
```bash
docker-compose logs -f backend
```

Stop services:
```bash
docker-compose down
```

### Building Images

Build specific service:
```bash
docker-compose build backend
```

Build all services:
```bash
docker-compose build
```

### Debugging in Docker

Shell into backend container:
```bash
docker exec -it sanctions-backend sh
```

View database:
```bash
docker exec -it sanctions-postgres psql -U sanctions -d sanctions
```

## Performance Optimization

### Database Optimization

- Add indexes for frequently queried columns
- Use pagination for large result sets
- Consider caching for expensive queries
- Use projections (DTOs) instead of full entities

### API Optimization

- Implement caching for frequent requests
- Use pagination for list endpoints
- Consider rate limiting for public endpoints
- Optimize JSON serialization

### Frontend Optimization

- Use React.memo for expensive components
- Implement virtual scrolling for large lists
- Use useMemo and useCallback for expensive calculations
- Optimize images and assets

## Security Best Practices

### Backend Security

- Always validate input
- Use parameterized queries
- Enable secure XML parsing
- Implement proper authentication and authorization
- Use HTTPS in production
- Keep dependencies updated
- Regular security audits

### Frontend Security

- Sanitize user input
- Use Content Security Policy (CSP)
- Avoid XSS vulnerabilities
- Use secure authentication
- Don't store sensitive data in localStorage
- Regular dependency updates

### Database Security

- Use strong passwords
- Limit database user permissions
- Encrypt sensitive data
- Use SSL for database connections
- Regular backups
- Audit logging

## Troubleshooting

### Common Issues

#### Port Already in Use

Check and kill the process:
```bash
# Linux/macOS
lsof -i :8080
kill -9 <PID>

# Windows
netstat -ano | findstr :8080
taskkill /PID <PID> /F
```

#### Database Connection Refused

Verify PostgreSQL is running:
```bash
# Docker
ps aux | grep postgres

# Local
pg_isready -h localhost -U sanctions
```

#### Ollama Not Responding

Check Ollama status:
```bash
curl http://localhost:11434/api/tags
ollama ps
```

#### Memory Issues

Increase JVM heap size:
```bash
./mvnw spring-boot:run -Dspring-boot.run.jvmArguments=-Xmx1g
```

### Debugging Tips

#### Enable H2 Console (Development)

Add to `application-dev.yml`:
```yaml
spring:
  h2:
    console:
      enabled: true
      path: /h2-console
```

Access at: http://localhost:8080/h2-console

#### Enable Actuator Endpoints

All actuator endpoints are enabled by default:
- http://localhost:8080/actuator/health
- http://localhost:8080/actuator/info
- http://localhost:8080/actuator/metrics

## Contributing

### Git Workflow

1. Create a feature branch:
   ```bash
   git checkout -b feature/your-feature
   ```

2. Make your changes
3. Commit with descriptive messages:
   ```bash
   git commit -am 'Add new search filter feature'
   ```

4. Push to the branch:
   ```bash
   git push origin feature/your-feature
   ```

5. Open a Pull Request

### Commit Message Guidelines

- Use present tense ("Add feature" not "Added feature")
- First line: 50 characters or less
- Include body if needed
- Reference issues: "Closes #123"

### Pull Request Guidelines

- Include clear description
- Reference related issues
- Include screenshots for UI changes
- Update documentation
- Ensure all tests pass
- Follow existing code patterns

## Version Management

### Backend Version

Update in `pom.xml`:
```xml
<version>1.0.0</version>
```

### Frontend Version

Update in `frontend/package.json`:
```json
"version": "1.0.0"
```

### Release Process

1. Update version numbers
2. Update CHANGELOG.md
3. Create Git tag:
   ```bash
   git tag -a v1.0.0 -m 'Release v1.0.0'
   git push origin v1.0.0
   ```
4. Build and push Docker images
5. Deploy to production

## Documentation

### Updating Documentation

- Keep README.md up to date
- Update API documentation (Swagger annotations)
- Add inline code comments for complex logic
- Document configuration options
- Update development guide as needed

### Code Documentation

Use JavaDoc for backend:
```java
/**
 * Calculates similarity between two names.
 * 
 * @param name1 First name to compare
 * @param name2 Second name to compare
 * @return Similarity score between 0 and 100
 */
public double calculateSimilarity(String name1, String name2) {
    // Implementation
}
```

Use JSDoc for frontend:
```typescript
/**
 * Searches the sanctions list for potential matches.
 * 
 * @param request - Search request containing name and filters
 * @returns Promise with search results
 */
async function searchSanctions(request: SearchRequest): Promise<SearchResult> {
  // Implementation
}
```

## Monitoring

### Metrics

Spring Boot Actuator provides metrics:
- http://localhost:8080/actuator/metrics

Common metrics:
- `http.server.requests`: HTTP request counts
- `jvm.memory.used`: JVM memory usage
- `jvm.threads.live`: Live thread count
- `hikari.pool.ActiveConnections`: Database connection pool

### Logging

View application logs:
```bash
# Docker
docker logs sanctions-backend

# Local
tail -f backend/logs/application.log
```

Log levels can be configured in `application.yml`:
```yaml
logging:
  level:
    root: INFO
    com.sanctions.screening: DEBUG
```

## Continuous Integration

### GitHub Actions

Create workflow file: `.github/workflows/ci.yml`

Example workflow:
```yaml
name: CI

on: [push, pull_request]

jobs:
  backend-test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - name: Set up JDK 21
        uses: actions/setup-java@v3
        with:
          java-version: '21'
          distribution: 'temurin'
      - name: Run backend tests
        run: cd backend && ./mvnw test

  frontend-test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - name: Set up Node.js
        uses: actions/setup-node@v3
        with:
          node-version: '20'
      - name: Install dependencies
        run: cd frontend && npm ci
      - name: Run frontend tests
        run: cd frontend && npm run test
```

### Quality Gates

- All tests must pass
- No critical vulnerabilities
- Code coverage > 80%
- No linting errors
- Build must succeed

## Deployment

### Production Checklist

- [ ] All tests pass
- [ ] Security audit completed
- [ ] Performance testing completed
- [ ] Database migrations tested
- [ ] Backup strategy in place
- [ ] Monitoring configured
- [ ] Logging configured
- [ ] SSL certificates ready
- [ ] DNS configured
- [ ] Documentation updated

### Rollback Plan

1. Database: Restore from backup
2. Application: Rollback to previous Docker image
3. Configuration: Revert to previous configuration
4. Data: Have import rollback procedure

### Maintenance Tasks

- Regular database backups
- Monitor application health
- Update dependencies regularly
- Review and rotate credentials
- Monitor performance metrics
- Review logs for errors

## Additional Resources

- [Spring Boot Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [React Documentation](https://react.dev/)
- [TypeScript Documentation](https://www.typescriptlang.org/docs/)
- [Tailwind CSS Documentation](https://tailwindcss.com/docs)
- [Ollama Documentation](https://github.com/jmorganca/ollama)
- [UN Sanctions List](https://www.un.org/securitycouncil/content/un-sc-consolidated-list)

---

For questions or issues, please refer to the main README.md or open an issue in the repository.
