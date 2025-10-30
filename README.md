# Git Analyzer

A powerful Git repository analysis tool that leverages AI to provide insights into code evolution, commit patterns, and development trends. Built with React and Spring Boot.

## Features

### Core Analysis
- **Repository Analysis**: Analyze GitHub repositories for commits, contributors, and file changes
- **Commit History**: Track commit patterns and evolution over time
- **File Change Tracking**: Monitor file modification frequencies
- **Contributor Analytics**: Analyze team contributions and patterns

### AI-Powered Features
- **Semantic Search**: Find similar commits using vector embeddings
- **Question Answering**: Get AI-generated answers about your codebase
- **Smart Summaries**: Generate concise summaries of commit histories

### Visualizations
- **Codebase Heatmap**: Identify hotspots in your codebase
- **Commit Timeline**: Visualize commit activity over time  
- **Dependency Graph**: Map project dependencies
- **File Change Frequency**: Track most modified files

## Technology Stack

### Frontend
- React
- Chart.js
- Cytoscape.js
- Tailwind CSS
- Vite

### Backend  
- Spring Boot
- PostgreSQL with pgvector
- GitHub API
- OpenAI/Ollama API

## Prerequisites

- Java 17+
- PostgreSQL 12+ with pgvector extension
- Ollama with nomic-embed-text model
- Maven 3.6+

## Installation

### 1. Backend Setup

```bash
# Clone repository
git clone https://github.com/your-username/git-analyzer.git
cd git-analyzer/backend

# Configure environment variables
cp .env.example .env
# Edit .env with your settings

# Build and run with Maven
./mvnw clean install
./mvnw spring-boot:run
```

### 2. Frontend Setup

```bash 
# Navigate to frontend directory
cd ../frontend

# Install dependencies
npm install

# Start development server
npm run dev
```

### 3. Database Setup

```sql
-- Enable required PostgreSQL extensions
CREATE EXTENSION IF NOT EXISTS vector;

-- Initialize schema (if not using auto-generation)
-- See backend/src/main/resources/schema.sql
```

### 4. Ollama Setup

```bash
# Install Ollama
curl https://ollama.ai/install.sh | sh

# Pull required model
ollama pull nomic-embed-text

# Start Ollama server
ollama serve
```

## Configuration

### Backend Configuration
Key properties in `application.properties`:
```properties
# Database
spring.datasource.url=jdbc:postgresql://localhost:5433/code_archaeologist
spring.datasource.username=postgres
spring.datasource.password=postgres

# APIs
github.token=${GITHUB_TOKEN:}
openai.api-key=${OPENAI_API_KEY:}
ollama.model=${OLLAMA_MODEL:nomic-embed-text}
ollama.base-url=${OLLAMA_BASE_URL:http://localhost:11434}
```

### Frontend Configuration 
Environment variables in `.env`:
```
VITE_REACT_APP_API_URL=http://localhost:8080
```

## Usage

1. Start the backend server (runs on port 8080)
2. Start the frontend development server (runs on port 5173)
3. Open http://localhost:5173 in your browser
4. Enter a GitHub repository URL to analyze
5. Explore various analytics features:
   - View commit history and patterns
   - Search similar commits
   - Ask questions about the codebase
   - Generate summaries
   - Explore visualizations

## Project Structure

```
git-analyzer/
├── backend/                 # Spring Boot backend
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/      # Java source files
│   │   │   └── resources/ # Configuration files
│   │   └── test/          # Test files
│   └── pom.xml            # Maven configuration
├── frontend/               # React frontend
│   ├── src/
│   │   ├── components/    # React components
│   │   ├── store/        # State management
│   │   └── main.jsx      # Entry point
│   ├── package.json
│   └── vite.config.js
└── README.md
```

## Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Acknowledgments

- [Spring Boot](https://spring.io/projects/spring-boot)
- [React](https://reactjs.org/)
- [pgvector](https://github.com/pgvector/pgvector)
- [Ollama](https://ollama.ai/)