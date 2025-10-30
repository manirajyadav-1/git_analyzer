# Code Archeologist - Spring Boot Version

This is the Spring Boot conversion of the Code Archeologist project, a comprehensive code analysis tool that provides insights into repository evolution, contributor patterns, and code changes.

## Features

- **Repository Analysis**: Analyze GitHub repositories for commits, contributors, and file changes
- **Semantic Search**: Search through commit messages using vector embeddings
- **AI-Powered Insights**: Generate summaries and answer questions about code evolution
- **Visual Analytics**: File change frequency, commit activity timelines, and contributor statistics
- **Dependency Analysis**: Track and visualize project dependencies

## Technology Stack

- **Backend**: Spring Boot 3.2.0, Java 17
- **Database**: PostgreSQL with pgvector extension
- **Session Management**: Redis
- **External APIs**: GitHub API, OpenAI API, Ollama
- **Build Tool**: Maven

## Prerequisites

- Java 17 or higher
- Maven 3.6+
- PostgreSQL 12+ with pgvector extension
- Redis (for session management)
- GitHub API token
- OpenAI API key (optional, can use Ollama as fallback)

## Setup Instructions

### 1. Database Setup

```sql
-- Install required PostgreSQL extensions
CREATE EXTENSION IF NOT EXISTS vector;
```

### 2. Environment Configuration

Create a `.env` file or set environment variables:

```bash
# Database Configuration
DB_HOST=localhost
DB_PORT=5432
DB_NAME=archeologist
DB_USER=postgres
DB_PASSWORD=your_password

# Redis Configuration
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=

# External APIs
GITHUB_TOKEN=your_github_token
OPENAI_API_KEY=your_openai_key
OLLAMA_MODEL=nomic-embed-text
OLLAMA_BASE_URL=http://localhost:11434

# Session Configuration
SESSION_SECRET=your-secret-key-here
```

### 3. Build and Run

```bash
# Clone the repository
cd code-archeologist-springboot

# Build the project
mvn clean install

# Run the application
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

## API Endpoints

### Analysis Endpoints
- `POST /api/analyze` - Analyze a GitHub repository
- `GET /api/analysis-data?analysisId={id}` - Get analysis data
- `POST /api/process-commits` - Process commits with embeddings
- `GET /api/analysis/{analysisId}` - Get specific analysis

### Data Endpoints
- `GET /api/file-change-frequency` - File change frequency data
- `GET /api/commit-activity-timeline` - Commit activity timeline
- `GET /api/contributor-statistics` - Contributor statistics
- `GET /api/codebase-heatmap` - Codebase heatmap data
- `GET /api/dependency-graph` - Dependency graph data
- `GET /api/linked-issues` - Linked issues data

### Search Endpoints
- `GET /api/search-commits?query={query}` - Semantic search in commits
- `POST /api/question-answering` - AI-powered Q&A
- `POST /api/summarize` - Generate commit summaries

### Utility Endpoints
- `GET /api/session` - Get current session info
- `GET /health` - Health check

## Configuration

The application uses Spring Boot's configuration system. Key configuration files:

- `application.properties` - Main configuration
- `src/main/resources/schema.sql` - Database initialization

## Development

### Project Structure

```
src/main/java/com/archeologist/
├── CodeArcheologistApplication.java    # Main application class
├── config/                             # Configuration classes
│   ├── AppConfig.java
│   ├── CorsConfig.java
│   └── SessionConfig.java
├── controller/                         # REST controllers
│   ├── AnalysisController.java
│   ├── DataController.java
│   └── SearchController.java
├── entity/                            # JPA entities
│   ├── CodeAnalysis.java
│   └── CommitEmbedding.java
├── repository/                        # Data repositories
│   ├── CodeAnalysisRepository.java
│   └── CommitEmbeddingRepository.java
└── service/                          # Business logic
    ├── AnalysisService.java
    ├── GitHubService.java
    └── OpenAIService.java
```

### Key Differences from Node.js Version

1. **Session Management**: Uses Spring Session with Redis instead of express-session
2. **Database Access**: JPA/Hibernate instead of raw PostgreSQL queries
3. **HTTP Client**: Spring WebFlux instead of axios
4. **Configuration**: Spring Boot properties instead of dotenv
5. **Dependency Injection**: Spring IoC container instead of manual imports

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests if applicable
5. Submit a pull request

## License

This project is licensed under the MIT License - see the LICENSE file for details.
