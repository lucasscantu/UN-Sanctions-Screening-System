import axios, { type AxiosInstance, type AxiosError, type InternalAxiosRequestConfig } from 'axios'
import type { 
  SearchRequest, 
  SearchResult, 
  SanctionedRecord,
  ImportJob,
  ImportStatus,
  AIAnalysisResult
} from '@/types'

// Create axios instance with base configuration
const api: AxiosInstance = axios.create({
  baseURL: '/api',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json',
  },
})

// Request interceptor for adding auth token if available
api.interceptors.request.use((config: InternalAxiosRequestConfig) => {
  const token = localStorage.getItem('authToken')
  if (token && config.headers) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// Response interceptor for error handling
api.interceptors.response.use(
  (response) => response,
  (error: AxiosError) => {
    if (error.response?.status === 401) {
      // Handle unauthorized - clear token and redirect
      localStorage.removeItem('authToken')
      window.location.href = '/login'
    }
    return Promise.reject(error)
  }
)

// Search API
export const searchApi = {
  search: (request: SearchRequest): Promise<SearchResult> => {
    return api.post<SearchResult>('/search', request).then(res => res.data)
  },
  
  searchGet: (params: {
    name?: string
    type?: string
    minScore?: number
    page?: number
    pageSize?: number
  }): Promise<SearchResult> => {
    return api.get<SearchResult>('/search', { params }).then(res => res.data)
  },
  
  analyzeWithAI: (name: string, minScore?: number): Promise<AIAnalysisResult[]> => {
    return api.get<AIAnalysisResult[]>('/search/ai-analysis', { 
      params: { name, minScore }
    }).then(res => res.data)
  },
}

// Records API
export const recordsApi = {
  getById: (id: number): Promise<SanctionedRecord> => {
    return api.get<SanctionedRecord>(`/records/${id}`).then(res => res.data)
  },
  
  getByReference: (referenceNumber: string): Promise<SanctionedRecord> => {
    return api.get<SanctionedRecord>(`/records/reference/${referenceNumber}`).then(res => res.data)
  },
  
  getAll: (page?: number, size?: number): Promise<{ content: SanctionedRecord[], totalElements: number }> => {
    return api.get<{ content: SanctionedRecord[], totalElements: number }>('/records', {
      params: { page, size }
    }).then(res => res.data)
  },
}

// Admin API
export const adminApi = {
  triggerSync: (): Promise<{ success: boolean, message: string }> => {
    return api.post<{ success: boolean, message: string }>('/admin/sync').then(res => res.data)
  },
  
  getSyncStatus: (): Promise<ImportStatus> => {
    return api.get<ImportStatus>('/admin/sync/status').then(res => res.data)
  },
  
  getSyncHistory: (): Promise<ImportJob[]> => {
    return api.get<ImportJob[]>('/admin/sync/history').then(res => res.data)
  },
  
  getSyncJob: (jobUuid: string): Promise<ImportJob> => {
    return api.get<ImportJob>(`/admin/sync/jobs/${jobUuid}`).then(res => res.data)
  },
}

// Ollama API
export const ollamaApi = {
  checkHealth: (): Promise<boolean> => {
    return api.get<boolean>('/ollama/health').then(res => res.data)
  },
  
  listModels: (): Promise<string[]> => {
    return api.get<string[]>('/ollama/models').then(res => res.data)
  },
}

// Health API
export const healthApi = {
  check: (): Promise<any> => {
    return api.get('/actuator/health').then(res => res.data)
  },
}

// Dashboard API
export const dashboardApi = {
  getStats: (): Promise<{
    totalIndividuals: number
    totalEntities: number
    lastSyncDate?: string
    dataFreshness?: string
    recentErrors?: string[]
    ollamaAvailable: boolean
    ollamaModel?: string
  }> => {
    return adminApi.getSyncStatus().then(status => {
      return {
        totalIndividuals: status.totalIndividuals,
        totalEntities: status.totalEntities,
        lastSyncDate: status.lastImportJob?.startedAt,
        dataFreshness: calculateDataFreshness(status.lastImportJob?.startedAt),
        recentErrors: status.lastImportJob?.errorSummary ? [status.lastImportJob.errorSummary] : [],
        ollamaAvailable: false, // Will be updated separately
        ollamaModel: undefined,
      }
    })
  },
}

// Helper function to calculate data freshness
function calculateDataFreshness(lastSyncDate?: string): string {
  if (!lastSyncDate) return 'Never synced'
  
  const lastSync = new Date(lastSyncDate)
  const now = new Date()
  const hoursDiff = (now.getTime() - lastSync.getTime()) / (1000 * 60 * 60)
  
  if (hoursDiff < 1) return 'Less than 1 hour ago'
  if (hoursDiff < 24) return `Updated ${Math.floor(hoursDiff)} hours ago`
  if (hoursDiff < 48) return 'Updated yesterday'
  if (hoursDiff < 72) return 'Updated 2 days ago'
  
  const daysDiff = Math.floor(hoursDiff / 24)
  return `Updated ${daysDiff} days ago`
}

// Export the base api instance for custom requests
export { api }

export default {
  search: searchApi,
  records: recordsApi,
  admin: adminApi,
  ollama: ollamaApi,
  health: healthApi,
  dashboard: dashboardApi,
}
