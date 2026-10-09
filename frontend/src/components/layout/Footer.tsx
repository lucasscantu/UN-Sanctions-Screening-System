import { Link } from 'react-router-dom'
import { ShieldCheck } from 'lucide-react'

export default function Footer() {
  return (
    <footer className="border-t bg-muted/40">
      <div className="container mx-auto px-4 py-6">
        <div className="flex flex-col sm:flex-row items-center justify-between gap-4">
          <div className="flex items-center gap-3">
            <ShieldCheck className="h-5 w-5 text-muted-foreground" />
            <div>
              <p className="text-sm font-medium">UN Sanctions Screening System</p>
              <p className="text-xs text-muted-foreground">
                Powered by official UN Security Council data
              </p>
            </div>
          </div>
          
          <div className="flex gap-6 text-sm">
            <Link to="/" className="text-muted-foreground hover:text-foreground transition-colors">
              Dashboard
            </Link>
            <Link to="/search" className="text-muted-foreground hover:text-foreground transition-colors">
              Search
            </Link>
            <Link to="/admin" className="text-muted-foreground hover:text-foreground transition-colors">
              Admin
            </Link>
          </div>
        </div>
        
        <div className="mt-4 pt-4 border-t border-muted/20">
          <p className="text-xs text-muted-foreground text-center">
            <strong>Important Disclaimer:</strong> This system provides screening assistance only. 
            Name similarity does NOT prove identity. Human review is REQUIRED for all potential matches.
            Always verify information from official UN sources before taking any action.
          </p>
          <p className="text-xs text-muted-foreground text-center mt-2">
            &copy; {new Date().getFullYear()} Sanctions Screening System. 
            Data from United Nations Security Council Consolidated Sanctions List.
          </p>
        </div>
      </div>
    </footer>
  )
}
