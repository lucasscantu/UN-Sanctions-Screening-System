import { useEffect, useState } from 'react'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import { Separator } from '@/components/ui/separator'
import { Alert, AlertDescription, AlertTitle } from '@/components/ui/alert'
import { Loader2, RefreshCw, History, CheckCircle, XCircle, Clock, Settings, Brain } from 'lucide-react'
import { useSyncOperations, useOllamaOperations } from '@/hooks/useAdmin'
import { formatDateTime, formatNumber } from '@/lib/utils'
import type { ImportJob, ImportStatus } from '@/types'

export default function AdminPage() {
  const { 
    triggerSync, 
    isTriggeringSync, 
    syncStatus, 
    syncHistory 
  } = useSyncOperations()
  const { ollamaHealth, ollamaModels } = useOllamaOperations()
  const [selectedJob, setSelectedJob] = useState<ImportJob | null>(null)

  useEffect(() => {
    document.title = 'Sanctions Screening - Administration'
  }, [])

  const handleTriggerSync = async () => {
    try {
      await triggerSync()
    } catch (err) {
      console.error('Failed to trigger sync:', err)
    }
  }

  const getStatusBadge = (status: string) => {
    switch (status) {
      case 'COMPLETED':
        return <Badge variant="default" className="bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200">
          Completed
        </Badge>
      case 'IN_PROGRESS':
        return <Badge variant="default" className="bg-blue-100 text-blue-800 dark:bg-blue-900 dark:text-blue-200">
          In Progress
        </Badge>
      case 'FAILED':
        return <Badge variant="destructive">
          Failed
        </Badge>
      case 'PENDING':
        return <Badge variant="secondary">
          Pending
        </Badge>
      default:
        return <Badge variant="outline">
          {status}
        </Badge>
    }
  }

  return (
    <div className="container mx-auto py-8">
      <div className="space-y-8">
        {/* Header */}
        <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
          <div>
            <h1 className="text-3xl font-bold tracking-tight">Administration</h1>
            <p className="text-muted-foreground mt-1">
              System management and configuration
            </p>
          </div>
        </div>

        {/* System Status Overview */}
        <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
          <Card>
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle className="text-sm font-medium">Data Sync Status</CardTitle>
              <div className="p-2 bg-blue-100 rounded-md">
                <RefreshCw className="h-4 w-4 text-blue-600" />
              </div>
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold">
                {syncStatus.data?.importInProgress ? 'Syncing...' : 'Ready'}
              </div>
              <p className="text-xs text-muted-foreground mt-1">
                {syncStatus.data?.lastImportJob?.startedAt 
                  ? `Last sync: ${formatDateTime(syncStatus.data.lastImportJob.startedAt)}`
                  : 'Never synced'}
              </p>
              {syncStatus.data?.importInProgress && (
                <div className="mt-2 flex items-center gap-2">
                  <Loader2 className="h-3 w-3 animate-spin" />
                  <span className="text-xs text-muted-foreground">Import in progress</span>
                </div>
              )}
            </CardContent>
          </Card>

          <Card>
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle className="text-sm font-medium">Total Records</CardTitle>
              <div className="p-2 bg-green-100 rounded-md">
                <Settings className="h-4 w-4 text-green-600" />
              </div>
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold">
                {formatNumber((syncStatus.data?.totalIndividuals || 0) + (syncStatus.data?.totalEntities || 0))}
              </div>
              <p className="text-xs text-muted-foreground mt-1">
                Individuals: {formatNumber(syncStatus.data?.totalIndividuals || 0)} | 
                Entities: {formatNumber(syncStatus.data?.totalEntities || 0)}
              </p>
            </CardContent>
          </Card>

          <Card>
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle className="text-sm font-medium">AI Service</CardTitle>
              <div className="p-2 bg-purple-100 rounded-md">
                <Brain className="h-4 w-4 text-purple-600" />
              </div>
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold">
                {ollamaHealth.data ? 'Available' : 'Unavailable'}
              </div>
              <p className="text-xs text-muted-foreground mt-1">
                {ollamaHealth.data ? 'Ollama service is running' : 'Ollama service is not responding'}
              </p>
            </CardContent>
          </Card>
        </div>

        {/* Sync Actions */}
        <Card>
          <CardHeader>
            <CardTitle>Data Synchronization</CardTitle>
            <CardDescription>
              Manage UN sanctions list imports
            </CardDescription>
          </CardHeader>
          <CardContent className="space-y-6">
            <div className="flex flex-col sm:flex-row gap-4">
              <Button 
                onClick={handleTriggerSync}
                disabled={isTriggeringSync || syncStatus.data?.importInProgress}
                className="flex-1"
              >
                {isTriggeringSync || syncStatus.data?.importInProgress ? (
                  <>
                    <Loader2 className="h-4 w-4 mr-2 animate-spin" />
                    Syncing...
                  </>
                ) : (
                  <>
                    <RefreshCw className="h-4 w-4 mr-2" />
                    Trigger Manual Sync
                  </>
                )}
              </Button>
              
              <Button variant="outline" className="flex-1" disabled>
                <Settings className="h-4 w-4 mr-2" />
                Configure Auto-Sync
              </Button>
            </div>

            {syncStatus.data?.lastImportJob && (
              <div className="border rounded-lg p-4">
                <h3 className="font-semibold mb-3">Last Import Job</h3>
                <div className="grid gap-3">
                  <div className="flex justify-between">
                    <span className="text-muted-foreground">Job UUID</span>
                    <span className="font-mono text-sm">{syncStatus.data.lastImportJob.jobUuid}</span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-muted-foreground">Status</span>
                    {getStatusBadge(syncStatus.data.lastImportJob.status)}
                  </div>
                  <div className="flex justify-between">
                    <span className="text-muted-foreground">Started</span>
                    <span>{formatDateTime(syncStatus.data.lastImportJob.startedAt)}</span>
                  </div>
                  {syncStatus.data.lastImportJob.completedAt && (
                    <div className="flex justify-between">
                      <span className="text-muted-foreground">Completed</span>
                      <span>{formatDateTime(syncStatus.data.lastImportJob.completedAt)}</span>
                    </div>
                  )}
                  <div className="flex justify-between">
                    <span className="text-muted-foreground">Records Created</span>
                    <span>{formatNumber(syncStatus.data.lastImportJob.recordsCreated)}</span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-muted-foreground">Records Updated</span>
                    <span>{formatNumber(syncStatus.data.lastImportJob.recordsUpdated)}</span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-muted-foreground">Records Failed</span>
                    <span>{formatNumber(syncStatus.data.lastImportJob.recordsFailed)}</span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-muted-foreground">Total Processed</span>
                    <span>{formatNumber(syncStatus.data.lastImportJob.totalRecordsProcessed)}</span>
                  </div>
                </div>
                
                {syncStatus.data.lastImportJob.errorSummary && (
                  <Alert variant="destructive" className="mt-4">
                    <AlertTitle>Error Summary</AlertTitle>
                    <AlertDescription>
                      {syncStatus.data.lastImportJob.errorSummary}
                    </AlertDescription>
                  </Alert>
                )}
              </div>
            )}
          </CardContent>
        </Card>

        {/* Import History */}
        <Card>
          <CardHeader>
            <CardTitle>Import History</CardTitle>
            <CardDescription>
              Recent import jobs
            </CardDescription>
          </CardHeader>
          <CardContent>
            {syncHistory.data && syncHistory.data.length > 0 ? (
              <div className="space-y-3">
                {syncHistory.data.slice(0, 10).map((job, index) => (
                  <div 
                    key={`${job.jobUuid}-${index}`}
                    className={`border rounded-lg p-4 cursor-pointer transition-colors ${
                      selectedJob?.jobUuid === job.jobUuid 
                        ? 'bg-muted/50 border-primary' 
                        : 'hover:bg-muted/50'
                    }`}
                    onClick={() => setSelectedJob(job)}
                  >
                    <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2">
                      <div className="flex-1">
                        <div className="flex items-center gap-2">
                          {getStatusBadge(job.status)}
                          <span className="font-mono text-sm">{job.jobUuid.slice(0, 8)}...</span>
                        </div>
                        <p className="text-xs text-muted-foreground mt-1">
                          {formatDateTime(job.startedAt)}
                        </p>
                      </div>
                      <div className="text-sm">
                        <span className="font-medium">{formatNumber(job.totalRecordsProcessed)}</span>
                        <span className="text-muted-foreground ml-1">records</span>
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            ) : (
              <div className="flex flex-col items-center justify-center py-12">
                <div className="p-4 bg-muted rounded-full mb-4">
                  <History className="h-8 w-8 text-muted-foreground" />
                </div>
                <p className="text-muted-foreground text-center">
                  No import history available
                </p>
              </div>
            )}
          </CardContent>
        </Card>

        {/* Ollama Configuration */}
        <Card>
          <CardHeader>
            <CardTitle>AI Configuration</CardTitle>
            <CardDescription>
              Local AI model configuration for name analysis
            </CardDescription>
          </CardHeader>
          <CardContent className="space-y-6">
            <div className="grid gap-4">
              <div className="flex items-center justify-between">
                <div>
                  <h3 className="font-semibold">Ollama Service</h3>
                  <p className="text-sm text-muted-foreground">
                    Local AI model runtime
                  </p>
                </div>
                {ollamaHealth.data ? (
                  <Badge variant="default" className="bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200">
                    <CheckCircle className="h-3 w-3 mr-1" />
                    Available
                  </Badge>
                ) : (
                  <Badge variant="destructive">
                    <XCircle className="h-3 w-3 mr-1" />
                    Unavailable
                  </Badge>
                )}
              </div>

              <Separator />

              <div className="flex items-center justify-between">
                <div>
                  <h3 className="font-semibold">Available Models</h3>
                  <p className="text-sm text-muted-foreground">
                    Models installed in Ollama
                  </p>
                </div>
                {ollamaModels.data && ollamaModels.data.length > 0 ? (
                  <div className="flex flex-wrap gap-1">
                    {ollamaModels.data.slice(0, 5).map((model, index) => (
                      <Badge key={`${model}-${index}`} variant="outline" className="text-xs">
                        {model}
                      </Badge>
                    ))}
                    {ollamaModels.data.length > 5 && (
                      <Badge variant="outline" className="text-xs">
                        +{ollamaModels.data.length - 5} more
                      </Badge>
                    )}
                  </div>
                ) : (
                  <Badge variant="secondary">
                    No models found
                  </Badge>
                )}
              </div>
            </div>

            <Alert variant="info">
              <AlertTitle className="font-medium flex items-center gap-2">
                <Info className="h-4 w-4" />
                Model Configuration
              </AlertTitle>
              <AlertDescription>
                <p className="text-sm">
                  To use AI analysis, ensure Ollama is running and has the configured model installed.
                  The system will use the model specified in the backend configuration.
                </p>
              </AlertDescription>
            </Alert>
          </CardContent>
        </Card>

        {/* Action Buttons */}
        <div className="flex gap-2">
          <Button asChild variant="outline">
            <a href="/">
              <ArrowLeft className="h-4 w-4 mr-2" />
              Back to Dashboard
            </a>
          </Button>
        </div>
      </div>
    </div>
  )
}

// Import Info icon for the alert
import { Info } from 'lucide-react'
// Import ArrowLeft icon for the button
import { ArrowLeft } from 'lucide-react'
