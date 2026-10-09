import { useState, useEffect, useCallback } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { Search, Loader2, AlertCircle, Info, Filter } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import { Slider } from '@/components/ui/slider'
import { Badge } from '@/components/ui/badge'
import { Alert, AlertDescription, AlertTitle } from '@/components/ui/alert'
import { useSanctionsSearch } from '@/hooks/useSanctionsSearch'
import { useDebouncedSanctionsSearch } from '@/hooks/useSanctionsSearch'
import type { SearchRequest, SearchResult, SearchResultItem } from '@/types'
import { formatPercentage, getScoreColor, truncate } from '@/lib/utils'
import { Link } from 'react-router-dom'

// Form schema
const searchFormSchema = z.object({
  name: z.string().min(1, 'Please enter a name to search'),
  recordType: z.enum(['ALL', 'INDIVIDUAL', 'ENTITY']).optional(),
  minScore: z.number().min(0).max(100).optional(),
})

type SearchFormValues = z.infer<typeof searchFormSchema>

const DEFAULT_MIN_SCORE = 0

export default function SearchPage() {
  const [searchResults, setSearchResults] = useState<SearchResult | null>(null)
  const [isLoading, setIsLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [selectedRecordType, setSelectedRecordType] = useState<'ALL' | 'INDIVIDUAL' | 'ENTITY'>('ALL')
  const [minScore, setMinScore] = useState<number>(DEFAULT_MIN_SCORE)

  const { search, isSearching } = useSanctionsSearch()

  const form = useForm<SearchFormValues>({
    resolver: zodResolver(searchFormSchema),
    defaultValues: {
      name: '',
      recordType: 'ALL',
      minScore: DEFAULT_MIN_SCORE,
    },
  })

  useEffect(() => {
    document.title = 'Sanctions Screening - Search'
  }, [])

  const handleSearch = useCallback(async (data: SearchFormValues) => {
    setIsLoading(true)
    setError(null)

    try {
      const request: SearchRequest = {
        name: data.name.trim(),
        recordType: data.recordType === 'ALL' ? undefined : data.recordType,
        minScore: data.minScore,
        page: 1,
        pageSize: 50,
      }

      const result = await search(request)
      setSearchResults(result)
    } catch (err) {
      setError(err instanceof Error ? err.message : 'An error occurred during search')
      setSearchResults(null)
    } finally {
      setIsLoading(false)
    }
  }, [search])

  const handleFormSubmit = form.handleSubmit(handleSearch)

  const filterResults = useCallback((results: SearchResultItem[]) => {
    if (!selectedRecordType || selectedRecordType === 'ALL') {
      return results
    }
    return results.filter(item => item.recordType === selectedRecordType)
  }, [selectedRecordType])

  const filteredResults = searchResults ? filterResults(searchResults.results) : []

  return (
    <div className="container mx-auto py-8">
      <div className="space-y-8">
        {/* Header */}
        <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
          <div>
            <h1 className="text-3xl font-bold tracking-tight">Sanctions Screening Search</h1>
            <p className="text-muted-foreground mt-1">
              Search the UN Security Council Consolidated Sanctions List
            </p>
          </div>
        </div>

        {/* Search Form */}
        <Card>
          <CardHeader>
            <CardTitle>Search Criteria</CardTitle>
            <CardDescription>
              Enter a name to search for potential matches in the sanctions list
            </CardDescription>
          </CardHeader>
          <CardContent>
            <form onSubmit={handleFormSubmit} className="space-y-6">
              <div className="space-y-4">
                <div className="grid gap-4 md:grid-cols-2">
                  <div className="space-y-2">
                    <label htmlFor="name" className="text-sm font-medium leading-none">
                      Name to Search
                    </label>
                    <div className="relative">
                      <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
                      <Input
                        id="name"
                        placeholder="Enter full name (e.g., John Doe, ABC Corporation)"
                        className="pl-10"
                        autoComplete="off"
                        {...form.register('name')}
                      />
                    </div>
                    {form.formState.errors.name && (
                      <p className="text-sm text-destructive">
                        {form.formState.errors.name.message}
                      </p>
                    )}
                  </div>

                  <div className="space-y-2">
                    <label htmlFor="recordType" className="text-sm font-medium leading-none">
                      Record Type
                    </label>
                    <Select
                      defaultValue="ALL"
                      onValueChange={(value: 'ALL' | 'INDIVIDUAL' | 'ENTITY') => {
                        setSelectedRecordType(value)
                        form.setValue('recordType', value)
                      }}
                    >
                      <SelectTrigger id="recordType">
                        <SelectValue placeholder="Select record type" />
                      </SelectTrigger>
                      <SelectContent>
                        <SelectItem value="ALL">All Records</SelectItem>
                        <SelectItem value="INDIVIDUAL">Individuals Only</SelectItem>
                        <SelectItem value="ENTITY">Entities Only</SelectItem>
                      </SelectContent>
                    </Select>
                  </div>
                </div>

                <div className="space-y-2">
                  <label className="text-sm font-medium leading-none">
                    Minimum Similarity Score: {formatPercentage(minScore)}
                  </label>
                  <Slider
                    defaultValue={[DEFAULT_MIN_SCORE]}
                    max={100}
                    step={5}
                    value={[minScore]}
                    onValueChange={(value) => {
                      setMinScore(value[0])
                      form.setValue('minScore', value[0])
                    }}
                  />
                  <div className="flex justify-between text-xs text-muted-foreground">
                    <span>0%</span>
                    <span>25%</span>
                    <span>50%</span>
                    <span>75%</span>
                    <span>100%</span>
                  </div>
                  <p className="text-xs text-muted-foreground">
                    Only show results with similarity score above this threshold
                  </p>
                </div>

                <div className="flex gap-2">
                  <Button type="submit" disabled={isLoading || isSearching}>
                    {isLoading || isSearching ? (
                      <>
                        <Loader2 className="h-4 w-4 mr-2 animate-spin" />
                        Searching...
                      </>
                    ) : (
                      <>
                        <Search className="h-4 w-4 mr-2" />
                        Search
                      </>
                    )}
                  </Button>
                  <Button 
                    type="button" 
                    variant="outline" 
                    onClick={() => {
                      form.reset()
                      setSearchResults(null)
                      setError(null)
                    }}
                  >
                    Clear
                  </Button>
                </div>
              </div>
            </form>
          </CardContent>
        </Card>

        {/* Disclaimer */}
        <Alert>
          <Info className="h-4 w-4" />
          <AlertTitle className="font-medium">Important Notice</AlertTitle>
          <AlertDescription>
            <p className="text-sm">
              This system provides <strong>screening assistance only</strong>. Name similarity does NOT prove identity.
              All potential matches require <strong>human review and verification</strong>.
              Do not take automated action based solely on search results.
            </p>
          </AlertDescription>
        </Alert>

        {/* Search Results */}
        {error && (
          <Alert variant="destructive">
            <AlertCircle className="h-4 w-4" />
            <AlertTitle>Search Error</AlertTitle>
            <AlertDescription>{error}</AlertDescription>
          </Alert>
        )}

        {searchResults && (
          <Card>
            <CardHeader>
              <CardTitle>
                Search Results ({filteredResults.length} of {searchResults.totalResults})
              </CardTitle>
              <CardDescription>
                Results for "{searchResults.query}" - Found {searchResults.totalResults} potential matches
                {searchResults.searchDurationMs > 0 && (
                  <span> in {searchResults.searchDurationMs}ms</span>
                )}
              </CardDescription>
            </CardHeader>
            <CardContent>
              {filteredResults.length === 0 ? (
                <div className="flex flex-col items-center justify-center py-12">
                  <div className="p-4 bg-muted rounded-full mb-4">
                    <Search className="h-8 w-8 text-muted-foreground" />
                  </div>
                  <p className="text-muted-foreground text-center">
                    No potential matches found above the {formatPercentage(minScore)} similarity threshold
                  </p>
                  <p className="text-sm text-muted-foreground text-center mt-2">
                    Try adjusting your search criteria or lowering the minimum score
                  </p>
                </div>
              ) : (
                <div className="space-y-4">
                  <div className="text-xs text-muted-foreground mb-4">
                    <p>
                      <strong>Note:</strong> Similarity scores are experimental and for screening assistance only.
                      Human review is REQUIRED for all potential matches.
                    </p>
                  </div>

                  <div className="space-y-3">
                    {filteredResults.map((result, index) => (
                      <Card 
                        key={`${result.recordId}-${index}`}
                        className="transition-shadow hover:shadow-md"
                      >
                        <CardContent className="pt-4">
                          <div className="flex flex-col sm:flex-row sm:items-start gap-4">
                            <div className="flex-1">
                              <div className="flex items-start gap-3">
                                <div className="flex flex-col gap-1">
                                  <Link
                                    to={`/records/${result.recordId}`}
                                    className="font-semibold text-lg hover:text-primary hover:underline"
                                  >
                                    {result.primaryName}
                                  </Link>
                                  <div className="flex items-center gap-2">
                                    <Badge 
                                      variant="outline" 
                                      className="text-xs"
                                    >
                                      {result.recordType}
                                    </Badge>
                                    <span className="text-sm text-muted-foreground">
                                      {result.unReferenceNumber}
                                    </span>
                                  </div>
                                </div>
                              </div>

                              {result.matchingAlias && (
                                <div className="mt-2 text-sm">
                                  <span className="text-muted-foreground">Matched via alias: </span>
                                  <span className="font-medium text-primary">{result.matchingAlias}</span>
                                </div>
                              )}

                              <div className="mt-3 flex flex-wrap items-center gap-4 text-sm">
                                <div className="flex items-center gap-1">
                                  <span className="text-muted-foreground">Similarity:</span>
                                  <span className={`font-medium ${getScoreColor(result.similarityScore)}`}>
                                    {formatPercentage(result.similarityScore)}
                                  </span>
                                </div>
                                
                                {result.aliasCount > 0 && (
                                  <div className="flex items-center gap-1">
                                    <span className="text-muted-foreground">Aliases:</span>
                                    <span className="font-medium">{result.aliasCount}</span>
                                  </div>
                                )}

                                {result.aliases && result.aliases.length > 0 && (
                                  <div className="flex items-center gap-1">
                                    <span className="text-muted-foreground">Also known as:</span>
                                    <span className="font-medium">
                                      {truncate(result.aliases.join(', '), 50)}
                                    </span>
                                  </div>
                                )}
                              </div>
                            </div>

                            <div className="flex flex-col items-end justify-between sm:items-center gap-2">
                              <Badge 
                                className={`text-sm font-medium ${getScoreColor(result.similarityScore)}`}
                              >
                                {formatPercentage(result.similarityScore)} Match
                              </Badge>
                              <Button asChild variant="ghost" size="sm">
                                <Link to={`/records/${result.recordId}`}>
                                  View Details
                                </Link>
                              </Button>
                            </div>
                          </div>
                        </CardContent>
                      </Card>
                    ))}
                  </div>

                  <div className="text-xs text-muted-foreground pt-4 border-t">
                    <p>
                      Showing {filteredResults.length} results
                      {selectedRecordType !== 'ALL' && (
                        <span> (filtered by {selectedRecordType})</span>
                      )}
                    </p>
                  </div>
                </div>
              )}
            </CardContent>
          </Card>
        )}
      </div>
    </div>
  )
}
