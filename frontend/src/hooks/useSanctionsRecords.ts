import { useQuery } from '@tanstack/react-query'
import { recordsApi } from '@/lib/api'
import type { SanctionedRecord } from '@/types'

// Query keys for cache management
export const recordsQueryKeys = {
  all: ['records'] as const,
  byId: (id: number) => ['records', id] as const,
  byReference: (reference: string) => ['records', 'reference', reference] as const,
  allPaginated: (page: number, size: number) => ['records', 'all', page, size] as const,
}

// Hook for fetching a single record by ID
export function useSanctionsRecord(id: number) {
  return useQuery<SanctionedRecord, Error>({
    queryKey: recordsQueryKeys.byId(id),
    queryFn: () => recordsApi.getById(id),
    enabled: !!id && id > 0,
    staleTime: 1000 * 60 * 10, // 10 minutes
    retry: 1,
  })
}

// Hook for fetching a record by reference number
export function useSanctionsRecordByReference(referenceNumber: string) {
  return useQuery<SanctionedRecord, Error>({
    queryKey: recordsQueryKeys.byReference(referenceNumber),
    queryFn: () => recordsApi.getByReference(referenceNumber),
    enabled: !!referenceNumber && referenceNumber.trim().length > 0,
    staleTime: 1000 * 60 * 10,
    retry: 1,
  })
}

// Hook for fetching all records with pagination
export function useSanctionsRecords(page: number = 0, size: number = 20) {
  return useQuery<{ content: SanctionedRecord[], totalElements: number }, Error>({
    queryKey: recordsQueryKeys.allPaginated(page, size),
    queryFn: () => recordsApi.getAll(page, size),
    staleTime: 1000 * 60 * 5,
    retry: 1,
  })
}
