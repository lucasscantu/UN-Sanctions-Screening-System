// Simplified toaster component for the application
// This provides basic toast functionality without the full shadcn/ui setup

import { useEffect, useState, useCallback } from 'react'
import { createPortal } from 'react-dom'
import { X } from 'lucide-react'
import { Button } from './button'
import { Alert, AlertDescription, AlertTitle } from './alert'
import { cn } from '@/lib/utils'

interface ToastMessage {
  id: string
  title?: string
  description?: string
  variant?: 'default' | 'destructive' | 'success' | 'warning'
  duration?: number
}

interface ToasterProps {
  toasts: ToastMessage[]
  onDismiss: (id: string) => void
}

function ToasterComponent({ toasts, onDismiss }: ToasterProps) {
  if (toasts.length === 0) return null

  return createPortal(
    <div className="fixed bottom-4 right-4 z-50 w-full max-w-md space-y-2">
      {toasts.map((toast) => (
        <Alert 
          key={toast.id} 
          variant={toast.variant}
          className="relative shadow-lg"
        >
          <div className="flex-1">
            {toast.title && <AlertTitle>{toast.title}</AlertTitle>}
            {toast.description && (
              <AlertDescription>{toast.description}</AlertDescription>
            )}
          </div>
          <Button
            variant="ghost"
            size="icon"
            className="h-6 w-6 absolute top-2 right-2"
            onClick={() => onDismiss(toast.id)}
          >
            <X className="h-4 w-4" />
          </Button>
        </Alert>
      ))}
    </div>,
    document.body
  )
}

// Toast provider and hook
let toastId = 0
const toasts: ToastMessage[] = []
const listeners: ((toasts: ToastMessage[]) => void)[] = []

function addToast(toast: Omit<ToastMessage, 'id'>) {
  const id = String(++toastId)
  const newToast: ToastMessage = { id, ...toast }
  toasts.push(newToast)
  
  // Auto-dismiss after duration
  if (toast.duration !== undefined) {
    setTimeout(() => {
      const index = toasts.findIndex(t => t.id === id)
      if (index !== -1) {
        toasts.splice(index, 1)
        notifyListeners()
      }
    }, toast.duration)
  }
  
  notifyListeners()
  return id
}

function dismissToast(id: string) {
  const index = toasts.findIndex(t => t.id === id)
  if (index !== -1) {
    toasts.splice(index, 1)
    notifyListeners()
  }
}

function notifyListeners() {
  listeners.forEach(listener => listener([...toasts]))
}

export function useToast() {
  const [localToasts, setLocalToasts] = useState<ToastMessage[]>([])
  
  useEffect(() => {
    listeners.push(setLocalToasts)
    return () => {
      const index = listeners.indexOf(setLocalToasts)
      if (index !== -1) {
        listeners.splice(index, 1)
      }
    }
  }, [])
  
  const toast = useCallback((props: Omit<ToastMessage, 'id'>) => {
    return addToast(props)
  }, [])
  
  const dismiss = useCallback((id: string) => {
    dismissToast(id)
  }, [])
  
  return {
    toast,
    dismiss,
    toasts: localToasts,
  }
}

export function Toaster() {
  const { toasts, dismiss } = useToast()
  return <ToasterComponent toasts={toasts} onDismiss={dismiss} />
}

// Export toast function for direct use
export function toast(props: Omit<ToastMessage, 'id'>) {
  return addToast(props)
}
