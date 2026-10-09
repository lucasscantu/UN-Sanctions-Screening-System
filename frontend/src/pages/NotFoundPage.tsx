import { Link } from 'react-router-dom'
import { Button } from '@/components/ui/button'
import { AlertTriangle } from 'lucide-react'

export default function NotFoundPage() {
  return (
    <div className="min-h-screen flex flex-col items-center justify-center bg-muted/50">
      <div className="text-center space-y-6">
        <div className="p-6 bg-muted rounded-full">
          <AlertTriangle className="h-12 w-12 text-muted-foreground" />
        </div>
        
        <div className="space-y-2">
          <h1 className="text-4xl font-bold tracking-tight">404 - Page Not Found</h1>
          <p className="text-lg text-muted-foreground">
            The page you're looking for doesn't exist or has been moved.
          </p>
        </div>

        <div className="flex gap-4">
          <Button asChild>
            <Link to="/">
              Go to Dashboard
            </Link>
          </Button>
          
          <Button asChild variant="outline">
            <Link to="/search">
              Go to Search
            </Link>
          </Button>
        </div>
      </div>
    </div>
  )
}
