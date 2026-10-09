# REST API Documentation

This document provides detailed information about the UN Sanctions Screening System REST API.

## Base URL

```
http://localhost:8080/api
```

## Authentication

Most endpoints are publicly accessible. Administrative endpoints require basic authentication.

### Authentication Headers

For administrative endpoints:
```
Authorization: Basic base64(username:password)
```

Default credentials (change in production):
- Username: `admin`
- Password: `admin123`

## Response Format

All successful responses return:
```json
{
  "data": { ... },
  "success": true,
  "message": "Optional message"
}
```

Error responses return:
```json
{
  "message": "Error message",
  "status": 400,
  "timestamp": "2024-01-01T12:00:00.000+00:00"
}
```

## Endpoints

### Search

#### Search Sanctions List

**Endpoint**: `GET /api/search`

**Query Parameters**:

| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| `name` | string | No | - | Name to search |
| `type` | string | No | - | Record type filter (INDIVIDUAL, ENTITY) |
| `minScore` | number | No | 0 | Minimum similarity score (0-100) |
| `page` | integer | No | 1 | Page number |
| `pageSize` | integer | No | 20 | Results per page |

**Example Request**:
```
GET /api/search?name=John+Doe&minScore=60&page=1&pageSize=20
```

**Example Response**:
```json
{
  "query": "John Doe",
  "normalizedQuery": "john doe",
  "totalResults": 5,
  "results": [
    {
      "recordId": 123,
      "unReferenceNumber": "UNSC-001",
      "recordType": "INDIVIDUAL",
      "primaryName": "John Doe",
      "matchingAlias": "Johnny Doe",
      "similarityScore": 95.5,
      "levenshteinSimilarity": 98.0,
      "jaroWinklerSimilarity": 92.5,
      "tokenBasedSimilarity": 85.0,
      "aliasSimilarity": 90.0,
      "aliases": ["Johnny Doe", "J. Doe"],
      "aliasCount": 2
    }
  ],
  "searchDurationMs": 150,
  "candidatesRetrieved": 10
}
```

**Search with POST**:

**Endpoint**: `POST /api/search`

**Request Body**:
```json
{
  "name": "John Doe",
  "recordType": "INDIVIDUAL",
  "minScore": 60,
  "page": 1,
  "pageSize": 20
}
```

**Response**: Same as GET request

#### AI Analysis for Search Results

**Endpoint**: `GET /api/search/ai-analysis`

**Query Parameters**:

| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| `name` | string | Yes | - | Name to analyze |
| `minScore` | number | No | 0 | Minimum similarity score |

**Example Request**:
```
GET /api/search/ai-analysis?name=John+Doe&minScore=70
```

**Example Response**:
```json
[
  {
    "recordId": 123,
    "assessment": "HIGH",
    "explanation": "Names are very similar with minor spelling variations",
    "linguisticVariants": {
      "transliterationVariants": false,
      "spellingVariants": true,
      "nameOrderDifferences": false,
      "abbreviationDifferences": false
    },
    "matchingEvidence": {
      "exactMatch": false,
      "partialMatch": true,
      "aliasMatch": true,
      "bestMatchingAlias": "Johnny Doe",
      "matchingAliases": ["Johnny Doe", "J. Doe"]
    },
    "conflictingEvidence": {
      "differentNames": [],
      "missingInformation": []
    },
    "warning": "Name similarity alone CANNOT establish identity. Human review is REQUIRED.",
    "disclaimer": "This analysis is for screening assistance only and does not constitute a definitive determination.",
    "modelUsed": "qwen3:4b"
  }
]
```

### Records

#### Get Record by ID

**Endpoint**: `GET /api/records/{id}`

**Path Parameters**:

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `id` | integer | Yes | Database ID of the record |

**Example Request**:
```
GET /api/records/123
```

**Example Response**:
```json
{
  "id": 123,
  "unReferenceNumber": "UNSC-001",
  "recordType": "INDIVIDUAL",
  "primaryName": "John Doe",
  "submittedName": "DOE, JOHN",
  "sortKey": "DOE, JOHN",
  "designation": "Individual subject to sanctions",
  "listedOn": "2020-01-01",
  "sourceUrl": "https://www.un.org/securitycouncil/content/un-sc-consolidated-list",
  "firstImportedAt": "2024-01-01T12:00:00",
  "lastSyncedAt": "2024-01-02T12:00:00",
  "isActive": true,
  "sourceStatus": "Active",
  "aliases": [
    {
      "id": 456,
      "aliasName": "Johnny Doe",
      "quality": "GOOD",
      "nameOriginDesc": "Alias"
    }
  ]
}
```

#### Get Record by UN Reference Number

**Endpoint**: `GET /api/records/reference/{referenceNumber}`

**Path Parameters**:

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `referenceNumber` | string | Yes | UN reference number |

**Example Request**:
```
GET /api/records/reference/UNSC-001
```

**Response**: Same as Get Record by ID

#### Get All Records

**Endpoint**: `GET /api/records`

**Query Parameters**:

| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| `page` | integer | No | 0 | Page number |
| `size` | integer | No | 20 | Results per page |

**Example Request**:
```
GET /api/records?page=0&size=50
```

**Example Response**:
```json
{
  "content": [
    {
      "id": 123,
      "unReferenceNumber": "UNSC-001",
      "recordType": "INDIVIDUAL",
      "primaryName": "John Doe",
      "isActive": true
    }
  ],
  "totalElements": 1000,
  "last": false,
  "totalPages": 20,
  "size": 50,
  "number": 0,
  "sort": null,
  "first": true,
  "numberOfElements": 50
}
```

### Administration

#### Get Import Status

**Endpoint**: `GET /api/admin/sync/status`

**Authentication**: Required (Admin role)

**Example Request**:
```
GET /api/admin/sync/status
```

**Example Response**:
```json
{
  "importInProgress": false,
  "lastImportJob": {
    "id": 1,
    "jobUuid": "abc123",
    "sourceUrl": "https://www.un.org/securitycouncil/content/un-sc-consolidated-list-xml",
    "status": "COMPLETED",
    "startedAt": "2024-01-01T12:00:00",
    "completedAt": "2024-01-01T12:05:00",
    "recordsCreated": 500,
    "recordsUpdated": 50,
    "recordsDeactivated": 0,
    "recordsFailed": 0,
    "totalRecordsProcessed": 550,
    "errorSummary": null,
    "fileSizeBytes": 1048576,
    "isManualTrigger": false,
    "triggeredBy": "system"
  },
  "totalRecords": 550,
  "totalIndividuals": 400,
  "totalEntities": 150
}
```

#### Trigger Manual Sync

**Endpoint**: `POST /api/admin/sync`

**Authentication**: Required (Admin role)

**Example Request**:
```
POST /api/admin/sync
```

**Example Response**:
```json
{
  "success": true,
  "message": "Import completed successfully",
  "recordsCreated": 500,
  "recordsUpdated": 50,
  "recordsFailed": 0,
  "jobUuid": "def456"
}
```

#### Get Import History

**Endpoint**: `GET /api/admin/sync/history`

**Authentication**: Required (Admin role)

**Example Request**:
```
GET /api/admin/sync/history
```

**Example Response**:
```json
[
  {
    "id": 2,
    "jobUuid": "def456",
    "sourceUrl": "https://www.un.org/securitycouncil/content/un-sc-consolidated-list-xml",
    "status": "COMPLETED",
    "startedAt": "2024-01-02T12:00:00",
    "completedAt": "2024-01-02T12:05:00",
    "recordsCreated": 200,
    "recordsUpdated": 10,
    "recordsDeactivated": 0,
    "recordsFailed": 0,
    "totalRecordsProcessed": 210,
    "errorSummary": null,
    "fileSizeBytes": 1048576,
    "isManualTrigger": true,
    "triggeredBy": "admin"
  }
]
```

#### Get Import Job Details

**Endpoint**: `GET /api/admin/sync/jobs/{jobUuid}`

**Authentication**: Required (Admin role)

**Path Parameters**:

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `jobUuid` | string | Yes | Job UUID |

**Example Request**:
```
GET /api/admin/sync/jobs/def456
```

**Response**: Same as Import History item

### Ollama

#### Check Ollama Health

**Endpoint**: `GET /api/ollama/health`

**Example Request**:
```
GET /api/ollama/health
```

**Example Response**:
```json
true
```

#### List Available Models

**Endpoint**: `GET /api/ollama/models`

**Example Request**:
```
GET /api/ollama/models
```

**Example Response**:
```json
["qwen3:4b", "llama3:8b", "mistral:7b"]
```

### Health

#### Health Check

**Endpoint**: `GET /api/actuator/health`

**Example Request**:
```
GET /api/actuator/health
```

**Example Response**:
```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP",
      "details": {
        "database": "PostgreSQL",
        "validationQuery": "isValid()"
      }
    },
    "diskSpace": {
      "status": "UP",
      "details": {
        "total": 104857600,
        "free": 52428800,
        "threshold": 10485760
      }
    },
    "ollama": {
      "status": "UP",
      "details": {
        "available": true
      }
    },
    "ping": {
      "status": "UP"
    }
  }
}
```

## Error Codes

| Code | Description | Solution |
|------|-------------|----------|
| 400 | Bad Request | Check request parameters and format |
| 401 | Unauthorized | Provide valid credentials |
| 403 | Forbidden | Check user permissions |
| 404 | Not Found | Resource does not exist |
| 405 | Method Not Allowed | Use correct HTTP method |
| 408 | Request Timeout | Retry the request |
| 429 | Too Many Requests | Wait before retrying |
| 500 | Internal Server Error | Check server logs |
| 502 | Bad Gateway | Check backend service status |
| 503 | Service Unavailable | Service is temporarily unavailable |

## Rate Limiting

The API implements rate limiting to prevent abuse:

- Default: 100 requests per minute
- Can be configured via `resilience4j.ratelimiter` properties

**Response Headers**:
- `X-RateLimit-Limit`: Maximum requests allowed
- `X-RateLimit-Remaining`: Requests remaining in current window
- `X-RateLimit-Reset`: Time when limit resets (seconds)

## CORS

Cross-Origin Resource Sharing is configured to allow requests from:
- http://localhost:5173 (Vite development server)
- http://localhost:3000 (React development server)

Can be configured via `cors.allowed-origins` property.

## Versioning

The API does not currently implement versioning. All endpoints are at the root `/api` path.

Future versions may implement:
- Path versioning: `/api/v1/endpoint`
- Header versioning: `Accept: application/vnd.company.v1+json`

## Deprecation Policy

When endpoints are deprecated:
1. They will be marked as deprecated in the documentation
2. A `Deprecation` header will be included in responses
3. They will continue to work for at least one major version
4. A migration guide will be provided

## Examples

### Complete Search Flow

```bash
# Step 1: Search for a name
curl -X GET "http://localhost:8080/api/search?name=John+Doe&minScore=60"

# Step 2: Get AI analysis for the top results
curl -X GET "http://localhost:8080/api/search/ai-analysis?name=John+Doe&minScore=70"

# Step 3: Get details for a specific record
curl -X GET "http://localhost:8080/api/records/123"
```

### Python Example

```python
import requests

# Search
response = requests.get(
    "http://localhost:8080/api/search",
    params={"name": "John Doe", "minScore": 60}
)
results = response.json()

# Get record details
record = requests.get("http://localhost:8080/api/records/123").json()

# Get AI analysis
ai_results = requests.get(
    "http://localhost:8080/api/search/ai-analysis",
    params={"name": "John Doe"}
).json()
```

### JavaScript Example

```javascript
// Search
const search = async (name) => {
  const response = await fetch(
    `http://localhost:8080/api/search?name=${encodeURIComponent(name)}&minScore=60`
  );
  return await response.json();
};

// Get record details
const getRecord = async (id) => {
  const response = await fetch(`http://localhost:8080/api/records/${id}`);
  return await response.json();
};

// Usage
const results = await search("John Doe");
const record = await getRecord(results.results[0].recordId);
```

## Webhooks

Currently, the API does not provide webhook functionality. This may be added in future versions for:
- Import completion notifications
- New sanctions list updates
- System health alerts

## GraphQL

The API currently only provides REST endpoints. GraphQL may be added in future versions for more flexible querying.

---

For more information, see the [OpenAPI/Swagger documentation](http://localhost:8080/api/swagger-ui.html) or the [main README](../README.md).
