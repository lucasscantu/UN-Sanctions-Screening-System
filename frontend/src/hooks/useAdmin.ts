import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { adminApi, ollamaApi } from '@/lib/api'
import type { ImportJob, ImportStatus } from '@/types'

// Query keys for cache management
export const adminQueryKeys = {
  all: ['admin'] as const,
  syncStatus: () => ['admin', 'sync', 'status'] as const,
  syncHistory: () => ['admin', 'sync', 'history'] as const,
  syncJob: (jobUuid: string) => ['admin', 'sync', 'job', jobUuid] as const,
  ollamaHealth: () => ['admin', 'ollama', 'health'] as const,
  ollamaModels: () => ['admin', 'ollama', 'models'] as const,
}

// Hook for sync operations
export function useSyncOperations() {
  const queryClient = useQueryClient()
  
  // Trigger sync mutation
  const triggerSyncMutation = useMutation({
    mutationFn: () => adminApi.triggerSync(),
    onSuccess: () => {
      // Invalidate sync status and history
      queryClient.invalidateQueries({ queryKey: adminQueryKeys.syncStatus() })
      queryClient.invalidateQueries({ queryKey: adminQueryKeys.syncHistory() })
    },
    onError: (error) => {
      console.error('Sync error:', error)
    },
  })
  
  // Get sync status
  const syncStatusQuery = useQuery<ImportStatus, Error>({
    queryKey: adminQueryKeys.syncStatus(),
    queryFn: () => adminApi.getSyncStatus(),
    staleTime: 1000 * 30, // 30 seconds
    retry: 1,
  })
  
  // Get sync history
  const syncHistoryQuery = useQuery<ImportJob[], Error>({
    queryKey: adminQueryKeys.syncHistory(),
    queryFn: () => adminApi.getSyncHistory(),
    staleTime: 1000 * 60, // 1 minute
    retry: 1,
  })
  
  // Get specific sync job
  const syncJobQuery = (jobUuid: string) => {
    return useQuery<ImportJob, Error>({
      queryKey: adminQueryKeys.syncJob(jobUuid),
      queryFn: () => adminApi.getSyncJob(jobUuid),
      enabled: !!jobUuid,
      staleTime: 1000 * 60,
      retry: 1,
    })
  }
  
  return {
    triggerSync: triggerSyncMutation.mutateAsync,
    isTriggeringSync: triggerSyncMutation.isPending,
    triggerSyncError: triggerSyncMutation.error,
    syncStatus: syncStatusQuery,
    syncHistory: syncHistoryQuery,
    getSyncJob: syncJobQuery,
  }
}

// Hook for Ollama operations
export function useOllamaOperations() {
  const queryClient = useQueryClient()
  
  // Check Ollama health
  const ollamaHealthQuery = useQuery<boolean, Error>({
    queryKey: adminQueryKeys.ollamaHealth(),
    queryFn: () => ollamaApi.checkHealth(),
    staleTime: 1000 * 60, // 1 minute
    retry: 1,
  })
  
  // List available models
  const ollamaModelsQuery = useQuery<string[], Error>({
    queryKey: adminQueryKeys.ollamaModels(),
    queryFn: () => ollamaApi.listModels(),
    staleTime: 1000 * 60 * 5, // 5 minutes
    retry: 1,
  })
  
  return {
    ollamaHealth: ollamaHealthQuery,
    ollamaModels: ollamaModelsQuery,
  }
}

// Hook for dashboard statistics
export function useDashboardStats() {
  const { syncStatus } = useSyncOperations()
  const { ollamaHealth } = useOllamaOperations()
  
  // Combine data from multiple sources
  const stats = React.useMemo(() => {
    if (!syncStatus.data) {
      return {
        totalIndividuals: 0,
        totalEntities: 0,
        lastSyncDate: undefined,
        dataFreshness: 'Loading...',
        recentErrors: [],
        ollamaAvailable: ollamaHealth.data ?? false,
        ollamaModel: undefined,
      }
    }
    
    return {
      totalIndividuals: syncStatus.data.totalIndividuals,
      totalEntities: syncStatus.data.totalEntities,
      lastSyncDate: syncStatus.data.lastImportJob?.startedAt,
      dataFreshness: calculateDataFreshness(syncStatus.data.lastImportJob?.startedAt),
      recentErrors: syncStatus.data.lastImportJob?.errorSummary 
        ? [syncStatus.data.lastImportJob.errorSummary] 
        : [],
      ollamaAvailable: ollamaHealth.data ?? false,
      ollamaModel: undefined, // Will be updated when we have model info
    }
  }, [syncStatus.data, ollamaHealth.data])
  
  return {
    stats,
    isLoading: syncStatus.isLoading || ollamaHealth.isLoading,
    error: syncStatus.error || ollamaHealth.error,
  }
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
