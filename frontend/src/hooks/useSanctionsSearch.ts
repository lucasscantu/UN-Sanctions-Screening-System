import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { searchApi } from '@/lib/api'
import type { SearchRequest, SearchResult, AIAnalysisResult } from '@/types'

// Query keys for cache management
export const searchQueryKeys = {
  all: ['search'] as const,
  results: (request: SearchRequest) => ['search', request] as const,
  aiAnalysis: (name: string, minScore?: number) => ['search', 'ai-analysis', name, minScore] as const,
}

// Hook for searching sanctions
export function useSanctionsSearch() {
  const queryClient = useQueryClient()
  
  // Search mutation
  const searchMutation = useMutation({
    mutationFn: (request: SearchRequest) => searchApi.search(request),
    onSuccess: (data) => {
      // Invalidate any previous search results
      queryClient.invalidateQueries({ queryKey: searchQueryKeys.all })
    },
    onError: (error) => {
      console.error('Search error:', error)
    },
  })
  
  // AI Analysis query
  const aiAnalysisQuery = (name: string, minScore?: number) => {
    return useQuery<AIAnalysisResult[], Error>({
      queryKey: searchQueryKeys.aiAnalysis(name, minScore),
      queryFn: () => searchApi.analyzeWithAI(name, minScore),
      enabled: !!name && name.trim().length > 0,
      staleTime: 1000 * 60 * 5, // 5 minutes
      retry: 1,
    })
  }
  
  return {
    search: searchMutation.mutateAsync,
    searchAsync: searchMutation.mutateAsync,
    isSearching: searchMutation.isPending,
    searchError: searchMutation.error,
    aiAnalysisQuery,
  }
}

// Hook for debounced search
export function useDebouncedSanctionsSearch(debounceTime: number = 500) {
  const { search, isSearching } = useSanctionsSearch()
  const [debouncedSearch, setDebouncedSearch] = React.useState<string>('')
  
  // Debounce the search
  React.useEffect(() => {
    const timer = setTimeout(() => {
      if (debouncedSearch.trim().length > 0) {
        search({ name: debouncedSearch, minScore: 0, page: 1, pageSize: 20 })
      }
    }, debounceTime)
    
    return () => clearTimeout(timer)
  }, [debouncedSearch, debounceTime, search])
  
  return {
    debouncedSearch,
    setDebouncedSearch,
    isSearching,
  }
}
