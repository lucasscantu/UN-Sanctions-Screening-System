import { useEffect } from 'react'
import { useParams, Link } from 'react-router-dom'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import { Separator } from '@/components/ui/separator'
import { Alert, AlertDescription, AlertTitle } from '@/components/ui/alert'
import { ArrowLeft, ExternalLink, Clock, User, Building2, Calendar, Globe, IdCard, Home, Mail } from 'lucide-react'
import { useSanctionsRecord } from '@/hooks/useSanctionsRecords'
import { formatDate, formatNumber, getInitials, truncate } from '@/lib/utils'
import type { SanctionedRecord } from '@/types'

export default function RecordDetailPage() {
  const { id } = useParams<{ id: string }>()
  const { data: record, isLoading, error } = useSanctionsRecord(Number(id))

  useEffect(() => {
    document.title = `Sanctions Screening - ${record?.primaryName || 'Record Details'}`
  }, [record?.primaryName])

  if (isLoading) {
    return (
      <div className="container mx-auto py-8">
        <div className="flex items-center justify-center min-h-[400px]">
          <div className="text-center">
            <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary mx-auto mb-4" />
            <p className="text-muted-foreground">Loading record details...</p>
          </div>
        </div>
      </div>
    )
  }

  if (error) {
    return (
      <div className="container mx-auto py-8">
        <Alert variant="destructive">
          <AlertTitle>Error Loading Record</AlertTitle>
          <AlertDescription>
            {error.message}
          </AlertDescription>
        </Alert>
        <div className="mt-4">
          <Button asChild>
            <Link to="/search">
              <ArrowLeft className="h-4 w-4 mr-2" />
              Back to Search
            </Link>
          </Button>
        </div>
      </div>
    )
  }

  if (!record) {
    return (
      <div className="container mx-auto py-8">
        <Alert variant="destructive">
          <AlertTitle>Record Not Found</AlertTitle>
          <AlertDescription>
            The requested record could not be found.
          </AlertDescription>
        </Alert>
        <div className="mt-4">
          <Button asChild>
            <Link to="/search">
              <ArrowLeft className="h-4 w-4 mr-2" />
              Back to Search
            </Link>
          </Button>
        </div>
      </div>
    )
  }

  return (
    <div className="container mx-auto py-8">
      <div className="space-y-6">
        {/* Header */}
        <div className="flex flex-col gap-4">
          <div className="flex items-center gap-2">
            <Button asChild variant="ghost" size="icon">
              <Link to="/search">
                <ArrowLeft className="h-4 w-4" />
              </Link>
            </Button>
          </div>

          <div className="flex flex-col sm:flex-row sm:items-center gap-4">
            <div className="flex-1">
              <div className="flex items-center gap-3">
                <div className="flex h-12 w-12 items-center justify-center rounded-full bg-primary/10 border">
                  <span className="text-xl font-bold text-primary">
                    {getInitials(record.primaryName)}
                  </span>
                </div>
                <div>
                  <h1 className="text-2xl font-bold">{record.primaryName}</h1>
                  <div className="flex items-center gap-2 mt-1">
                    <Badge 
                      variant={record.recordType === 'INDIVIDUAL' ? 'default' : 'secondary'}
                    >
                      {record.recordType}
                    </Badge>
                    <span className="text-sm text-muted-foreground">
                      {record.unReferenceNumber}
                    </span>
                  </div>
                </div>
              </div>
            </div>
            <div className="flex gap-2">
              <Button asChild variant="outline">
                <Link to="/search">
                  New Search
                </Link>
              </Button>
            </div>
          </div>
        </div>

        {/* Disclaimer */}
        <Alert>
          <AlertTitle className="font-medium flex items-center gap-2">
            <Info className="h-4 w-4" />
            Important Notice
          </AlertTitle>
          <AlertDescription>
            <p className="text-sm">
              This record is from the official United Nations Security Council Consolidated Sanctions List.
              Name similarity does NOT prove identity. This information is for screening assistance only.
              Human review and verification are REQUIRED before taking any action.
            </p>
          </AlertDescription>
        </Alert>

        {/* Main Details */}
        <div className="grid gap-6 md:grid-cols-2">
          {/* Identity Information */}
          <Card>
            <CardHeader>
              <CardTitle>Identity Information</CardTitle>
              <CardDescription>
                Official identification details from the UN sanctions list
              </CardDescription>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="space-y-3">
                <div className="flex items-center gap-3">
                  <div className="p-2 bg-muted rounded-md">
                    {record.recordType === 'INDIVIDUAL' ? (
                      <User className="h-4 w-4" />
                    ) : (
                      <Building2 className="h-4 w-4" />
                    )}
                  </div>
                  <div>
                    <p className="text-sm font-medium">Primary Name</p>
                    <p className="font-semibold">{record.primaryName}</p>
                  </div>
                </div>

                {record.submittedName && (
                  <div className="flex items-center gap-3">
                    <div className="p-2 bg-muted rounded-md">
                      <IdCard className="h-4 w-4" />
                    </div>
                    <div>
                      <p className="text-sm font-medium">Submitted Name</p>
                      <p className="font-semibold">{record.submittedName}</p>
                    </div>
                  </div>
                )}

                {record.sortKey && (
                  <div className="flex items-center gap-3">
                    <div className="p-2 bg-muted rounded-md">
                      <Globe className="h-4 w-4" />
                    </div>
                    <div>
                      <p className="text-sm font-medium">Sort Key</p>
                      <p className="font-semibold">{record.sortKey}</p>
                    </div>
                  </div>
                )}

                {record.designation && (
                  <div className="flex items-center gap-3">
                    <div className="p-2 bg-muted rounded-md">
                      <Mail className="h-4 w-4" />
                    </div>
                    <div>
                      <p className="text-sm font-medium">Designation</p>
                      <p className="font-semibold">{record.designation}</p>
                    </div>
                  </div>
                )}
              </div>
            </CardContent>
          </Card>

          {/* Administrative Information */}
          <Card>
            <CardHeader>
              <CardTitle>Administrative Information</CardTitle>
              <CardDescription>
                UN sanctions list administrative details
              </CardDescription>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="space-y-3">
                <div className="flex items-center gap-3">
                  <div className="p-2 bg-muted rounded-md">
                    <IdCard className="h-4 w-4" />
                  </div>
                  <div>
                    <p className="text-sm font-medium">UN Reference Number</p>
                    <p className="font-mono font-semibold">{record.unReferenceNumber}</p>
                  </div>
                </div>

                {record.listedOn && (
                  <div className="flex items-center gap-3">
                    <div className="p-2 bg-muted rounded-md">
                      <Calendar className="h-4 w-4" />
                    </div>
                    <div>
                      <p className="text-sm font-medium">Listed On</p>
                      <p className="font-semibold">{formatDate(record.listedOn)}</p>
                    </div>
                  </div>
                )}

                <div className="flex items-center gap-3">
                  <div className="p-2 bg-muted rounded-md">
                    <Clock className="h-4 w-4" />
                  </div>
                  <div>
                    <p className="text-sm font-medium">First Imported</p>
                    <p className="font-semibold">{formatDate(record.firstImportedAt)}</p>
                  </div>
                </div>

                <div className="flex items-center gap-3">
                  <div className="p-2 bg-muted rounded-md">
                    <Clock className="h-4 w-4" />
                  </div>
                  <div>
                    <p className="text-sm font-medium">Last Synced</p>
                    <p className="font-semibold">{formatDate(record.lastSyncedAt)}</p>
                  </div>
                </div>

                <div className="flex items-center gap-3">
                  <div className="p-2 bg-muted rounded-md">
                    <div className="h-4 w-4 rounded-full bg-green-500" />
                  </div>
                  <div>
                    <p className="text-sm font-medium">Status</p>
                    <p className="font-semibold">
                      <Badge variant={record.isActive ? 'default' : 'secondary'}>
                        {record.isActive ? 'Active' : 'Inactive'}
                      </Badge>
                    </p>
                  </div>
                </div>
              </div>
            </CardContent>
          </Card>
        </div>

        {/* Aliases Section */}
        {record.aliases && record.aliases.length > 0 && (
          <Card>
            <CardHeader>
              <CardTitle>Known Aliases ({record.aliases.length})</CardTitle>
              <CardDescription>
                Alternative names and spellings associated with this record
              </CardDescription>
            </CardHeader>
            <CardContent>
              <div className="space-y-3">
                {record.aliases.map((alias, index) => (
                  <div 
                    key={`${alias.id}-${index}`}
                    className="flex items-center justify-between p-3 bg-muted/50 rounded-md"
                  >
                    <div>
                      <p className="font-medium">{alias.aliasName}</p>
                      {alias.nameOriginDesc && (
                        <p className="text-sm text-muted-foreground">
                          {alias.nameOriginDesc}
                        </p>
                      )}
                    </div>
                    {alias.quality && (
                      <Badge variant="outline" className="text-xs">
                        {alias.quality}
                      </Badge>
                    )}
                  </div>
                ))}
              </div>
            </CardContent>
          </Card>
        )}

        {/* Source Information */}
        <Card>
          <CardHeader>
            <CardTitle>Source Information</CardTitle>
            <CardDescription>
              Official source and reference information
            </CardDescription>
          </CardHeader>
          <CardContent>
            <div className="space-y-4">
              {record.sourceUrl && (
                <div className="flex items-center gap-3">
                  <ExternalLink className="h-4 w-4 text-muted-foreground" />
                  <div>
                    <p className="text-sm font-medium">Source URL</p>
                    <a 
                      href={record.sourceUrl} 
                      target="_blank" 
                      rel="noopener noreferrer"
                      className="text-primary hover:underline text-sm break-all"
                    >
                      {truncate(record.sourceUrl, 100)}
                    </a>
                  </div>
                </div>
              )}

              {record.sourceStatus && (
                <div className="flex items-center gap-3">
                  <div className="p-2 bg-muted rounded-md">
                    <div className="h-4 w-4 rounded-full bg-blue-500" />
                  </div>
                  <div>
                    <p className="text-sm font-medium">Source Status</p>
                    <p className="font-semibold">{record.sourceStatus}</p>
                  </div>
                </div>
              )}

              <Separator />

              <Alert variant="warning">
                <AlertTitle className="font-medium flex items-center gap-2">
                  <AlertCircle className="h-4 w-4" />
                  Verification Required
                </AlertTitle>
                <AlertDescription>
                  <p className="text-sm">
                    This information is from the official UN Security Council Consolidated Sanctions List.
                    Always verify the most current information from the official source before taking any action.
                  </p>
                </AlertDescription>
              </Alert>
            </CardContent>
          </Card>

        {/* Action Buttons */}
        <div className="flex gap-2">
          <Button asChild>
            <Link to="/search">
              <ArrowLeft className="h-4 w-4 mr-2" />
              Back to Search
            </Link>
          </Button>
          
          {record.sourceUrl && (
            <Button asChild variant="outline">
              <a href={record.sourceUrl} target="_blank" rel="noopener noreferrer">
                <ExternalLink className="h-4 w-4 mr-2" />
                View Official Source
              </a>
            </Button>
          )}
        </div>
      </div>
    </div>
  )
}

// Import Info icon for the disclaimer
import { Info } from 'lucide-react'
