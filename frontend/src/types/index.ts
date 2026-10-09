// Sanctions Record Types
export interface SanctionedRecord {
  id: number
  unReferenceNumber: string
  recordType: 'INDIVIDUAL' | 'ENTITY'
  primaryName: string
  submittedName?: string
  sortKey?: string
  designation?: string
  listedOn?: string
  sourceUrl?: string
  firstImportedAt?: string
  lastSyncedAt?: string
  isActive: boolean
  sourceStatus?: string
  aliases?: NameAlias[]
}

export interface NameAlias {
  id: number
  aliasName: string
  quality?: string
  nameOriginDesc?: string
}

// Search Types
export interface SearchRequest {
  name: string
  recordType?: 'INDIVIDUAL' | 'ENTITY'
  minScore?: number
  page?: number
  pageSize?: number
  includeInactive?: boolean
}

export interface SearchResult {
  query: string
  normalizedQuery: string
  totalResults: number
  results: SearchResultItem[]
  searchDurationMs: number
  candidatesRetrieved: number
}

export interface SearchResultItem {
  recordId: number
  unReferenceNumber: string
  recordType: 'INDIVIDUAL' | 'ENTITY'
  primaryName: string
  matchingAlias?: string
  similarityScore: number
  levenshteinSimilarity: number
  jaroWinklerSimilarity: number
  tokenBasedSimilarity: number
  aliasSimilarity: number
  aliases?: string[]
  aliasCount: number
}

// AI Analysis Types
export interface AIAnalysisResult {
  recordId: number
  assessment: 'HIGH' | 'MEDIUM' | 'LOW' | 'INDETERMINATE'
  explanation: string
  linguisticVariants?: LinguisticVariants
  matchingEvidence?: MatchingEvidence
  conflictingEvidence?: ConflictingEvidence
  warning?: string
  disclaimer?: string
  modelUsed?: string
}

export interface LinguisticVariants {
  transliterationVariants: boolean
  spellingVariants: boolean
  nameOrderDifferences: boolean
  abbreviationDifferences: boolean
}

export interface MatchingEvidence {
  exactMatch: boolean
  partialMatch: boolean
  aliasMatch: boolean
  bestMatchingAlias?: string
  matchingAliases?: string[]
}

export interface ConflictingEvidence {
  differentNames?: string[]
  missingInformation?: string[]
}

// Import Types
export interface ImportJob {
  id: number
  jobUuid: string
  sourceUrl: string
  status: string
  startedAt: string
  completedAt?: string
  recordsCreated: number
  recordsUpdated: number
  recordsDeactivated: number
  recordsFailed: number
  totalRecordsProcessed: number
  errorSummary?: string
  fileSizeBytes: number
  isManualTrigger: boolean
  triggeredBy?: string
}

export interface ImportStatus {
  importInProgress: boolean
  lastImportJob?: ImportJob
  totalRecords: number
  totalIndividuals: number
  totalEntities: number
}

// Dashboard Types
export interface DashboardStats {
  totalIndividuals: number
  totalEntities: number
  lastSyncDate?: string
  dataFreshness?: string
  recentErrors?: string[]
  ollamaAvailable: boolean
  ollamaModel?: string
}

// API Response Types
export interface ApiResponse<T> {
  data?: T
  message?: string
  success: boolean
}

export interface ApiError {
  message: string
  status: number
  timestamp: string
}

// Form Types
export interface SearchFormValues {
  name: string
  recordType?: 'INDIVIDUAL' | 'ENTITY' | 'ALL'
  minScore: number
}

// UI State Types
export interface ToastMessage {
  title: string
  description?: string
  variant: 'default' | 'destructive' | 'success' | 'warning'
}

export type AssessmentLevel = 'HIGH' | 'MEDIUM' | 'LOW' | 'INDETERMINATE'

export const ASSESSMENT_COLORS: Record<AssessmentLevel, string> = {
  HIGH: 'bg-red-100 text-red-800 dark:bg-red-900 dark:text-red-200',
  MEDIUM: 'bg-orange-100 text-orange-800 dark:bg-orange-900 dark:text-orange-200',
  LOW: 'bg-yellow-100 text-yellow-800 dark:bg-yellow-900 dark:text-yellow-200',
  INDETERMINATE: 'bg-gray-100 text-gray-800 dark:bg-gray-900 dark:text-gray-200',
}
