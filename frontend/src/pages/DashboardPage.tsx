import { useEffect } from 'react'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Loader2, AlertTriangle, CheckCircle, XCircle, Clock } from 'lucide-react'
import { useSyncOperations, useDashboardStats } from '@/hooks/useAdmin'
import { formatDateTime, formatNumber } from '@/lib/utils'
import { Link } from 'react-router-dom'

export default function DashboardPage() {
  const { triggerSync, isTriggeringSync, syncStatus } = useSyncOperations()
  const { stats, isLoading, error } = useDashboardStats()

  useEffect(() => {
    document.title = 'Sanctions Screening - Dashboard'
  }, [])

  const handleTriggerSync = async () => {
    try {
      await triggerSync()
    } catch (err) {
      console.error('Failed to trigger sync:', err)
    }
  }

  if (isLoading) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <div className="flex flex-col items-center gap-4">
          <Loader2 className="h-8 w-8 animate-spin text-primary" />
          <p className="text-muted-foreground">Loading dashboard...</p>
        </div>
      </div>
    )
  }

  return (
    <div className="container mx-auto py-8">
      <div className="space-y-8">
        {/* Header */}
        <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
          <div>
            <h1 className="text-3xl font-bold tracking-tight">Sanctions Screening Dashboard</h1>
            <p className="text-muted-foreground mt-1">
              UN Security Council Consolidated Sanctions List Screening System
            </p>
          </div>
          <div className="flex gap-2">
            <Button asChild>
              <Link to="/search">Go to Search</Link>
            </Button>
            <Button 
              variant="outline" 
              onClick={handleTriggerSync}
              disabled={isTriggeringSync}
            >
              {isTriggeringSync ? (
                <>
                  <Loader2 className="h-4 w-4 mr-2 animate-spin" />
                  Syncing...
                </>
              ) : (
                'Refresh Data'
              )}
            </Button>
          </div>
        </div>

        {/* Stats Grid */}
        <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
          <Card>
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle className="text-sm font-medium">Total Individuals</CardTitle>
              <div className="p-2 bg-blue-100 rounded-md">
                <svg className="w-4 h-4 text-blue-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z" />
                </svg>
              </div>
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold">{formatNumber(stats.totalIndividuals)}</div>
              <p className="text-xs text-muted-foreground">
                Individuals in the sanctions list
              </p>
            </CardContent>
          </Card>

          <Card>
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle className="text-sm font-medium">Total Entities</CardTitle>
              <div className="p-2 bg-green-100 rounded-md">
                <svg className="w-4 h-4 text-green-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4" />
                </svg>
              </div>
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold">{formatNumber(stats.totalEntities)}</div>
              <p className="text-xs text-muted-foreground">
                Entities and organizations in the sanctions list
              </p>
            </CardContent>
          </Card>

          <Card>
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle className="text-sm font-medium">Last Sync</CardTitle>
              <div className="p-2 bg-purple-100 rounded-md">
                <Clock className="w-4 h-4 text-purple-600" />
              </div>
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold">
                {stats.lastSyncDate ? formatDateTime(stats.lastSyncDate) : 'Never'}
              </div>
              <p className="text-xs text-muted-foreground">
                {stats.dataFreshness}
              </p>
            </CardContent>
          </Card>

          <Card>
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle className="text-sm font-medium">AI Status</CardTitle>
              <div className="p-2 bg-pink-100 rounded-md">
                <svg className="w-4 h-4 text-pink-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M9.663 17h4.673M12 3v1m6.364 1.636l-.707.707M21 12h-1M4 12H3m3.343-5.657l-.707-.707m2.828 9.9a5 5 0 117.072 0l-.548.547A3.374 3.374 0 0014 18.469V19a2 2 0 11-4 0v-.531c0-.895-.356-1.754-.988-2.386l-.548-.547z" />
                </svg>
              </div>
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold">
                {stats.ollamaAvailable ? 'Available' : 'Unavailable'}
              </div>
              <p className="text-xs text-muted-foreground">
                Local AI model for name analysis
              </p>
            </CardContent>
          </Card>
        </div>

        {/* Sync Status Section */}
        <div className="grid gap-4 md:grid-cols-2">
          <Card>
            <CardHeader>
              <CardTitle>Data Status</CardTitle>
              <CardDescription>
                Current status of the sanctions data
              </CardDescription>
            </CardHeader>
            <CardContent>
              <div className="space-y-4">
                {syncStatus.data?.importInProgress ? (
                  <div className="flex items-center gap-2 p-3 bg-yellow-50 rounded-md">
                    <Loader2 className="h-4 w-4 animate-spin text-yellow-600" />
                    <div>
                      <p className="font-medium text-yellow-800">Import in progress</p>
                      <p className="text-sm text-muted-foreground">
                        Data is being updated from the UN source
                      </p>
                    </div>
                  </div>
                ) : (
                  <div className="flex items-center gap-2 p-3 bg-green-50 rounded-md">
                    <CheckCircle className="h-4 w-4 text-green-600" />
                    <div>
                      <p className="font-medium text-green-800">Data current</p>
                      <p className="text-sm text-muted-foreground">
                        Last updated: {stats.dataFreshness}
                      </p>
                    </div>
                  </div>
                )}

                {syncStatus.data?.lastImportJob?.errorSummary && (
                  <div className="flex items-center gap-2 p-3 bg-red-50 rounded-md">
                    <XCircle className="h-4 w-4 text-red-600" />
                    <div>
                      <p className="font-medium text-red-800">Import error</p>
                      <p className="text-sm text-muted-foreground">
                        {syncStatus.data.lastImportJob.errorSummary}
                      </p>
                    </div>
                  </div>
                )}
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle>Quick Actions</CardTitle>
              <CardDescription>
                Common actions for sanctions screening
              </CardDescription>
            </CardHeader>
            <CardContent className="space-y-4">
              <Button asChild className="w-full">
                <Link to="/search">
                  <svg className="w-4 h-4 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
                  </svg>
                  Perform New Search
                </Link>
              </Button>
              
              <Button asChild variant="outline" className="w-full">
                <Link to="/admin">
                  <svg className="w-4 h-4 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M12 18v-5.25m0 0a6.01 6.01 0 001.5-.189m-1.5.189a6.01 6.01 0 01-1.5-.189m3.75 7.478a12.06 12.06 0 01-4.5 0m3.75 2.311l-3.75 0M12 12.75L12 15M12 9l3.75 3.75M12 9l-3.75 3.75" />
                  </svg>
                  Administration
                </Link>
              </Button>

              <div className="text-xs text-muted-foreground pt-2">
                <p className="font-medium mb-1">Important Disclaimer:</p>
                <p>
                  This system provides screening assistance only. Name similarity does NOT prove identity.
                  Human review is REQUIRED for all potential matches.
                </p>
              </div>
            </CardContent>
          </Card>
        </div>

        {/* Recent Stats */}
        <Card>
          <CardHeader>
            <CardTitle>System Information</CardTitle>
            <CardDescription>
              Technical details about the screening system
            </CardDescription>
          </CardHeader>
          <CardContent>
            <div className="grid gap-4 md:grid-cols-2">
              <div className="space-y-2">
                <p className="text-sm font-medium">Total Records</p>
                <p className="text-lg font-semibold">
                  {formatNumber(stats.totalIndividuals + stats.totalEntities)}
                </p>
              </div>
              <div className="space-y-2">
                <p className="text-sm font-medium">Data Source</p>
                <p className="text-lg font-semibold">UN Security Council</p>
              </div>
              <div className="space-y-2">
                <p className="text-sm font-medium">AI Model</p>
                <p className="text-lg font-semibold">
                  {stats.ollamaModel || 'Not configured'}
                </p>
              </div>
              <div className="space-y-2">
                <p className="text-sm font-medium">Status</p>
                <p className="text-lg font-semibold">
                  {syncStatus.data?.importInProgress ? 'Syncing...' : 'Ready'}
                </p>
              </div>
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  )
}
