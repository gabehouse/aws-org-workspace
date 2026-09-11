import { useCallback, useEffect, useLayoutEffect, useState, type CSSProperties } from 'react'

const AUTO_DISMISS_MS = 6500

interface RequestSentToastProps {
  anchorRef: React.RefObject<HTMLButtonElement | null>
  onDismiss: () => void
  onOpenRequests: () => void
}

export function RequestSentToast({
  anchorRef,
  onDismiss,
  onOpenRequests,
}: RequestSentToastProps) {
  const [style, setStyle] = useState<CSSProperties>({ visibility: 'hidden' })

  const reposition = useCallback(() => {
    const anchor = anchorRef.current
    if (!anchor) return

    const rect = anchor.getBoundingClientRect()
    const toastWidth = 280
    const gap = 10
    const top = rect.bottom + gap
    let left = rect.left + rect.width / 2 - toastWidth / 2
    const margin = 8
    left = Math.max(margin, Math.min(left, window.innerWidth - toastWidth - margin))
    const arrowLeft = rect.left + rect.width / 2 - left

    setStyle({
      top,
      left,
      width: toastWidth,
      visibility: 'visible',
      ['--request-sent-toast-arrow-left' as string]: `${arrowLeft}px`,
    })
  }, [anchorRef])

  useLayoutEffect(() => {
    reposition()
  }, [reposition])

  useEffect(() => {
    window.addEventListener('resize', reposition)
    return () => window.removeEventListener('resize', reposition)
  }, [reposition])

  useEffect(() => {
    const timer = window.setTimeout(onDismiss, AUTO_DISMISS_MS)
    return () => window.clearTimeout(timer)
  }, [onDismiss])

  const open = () => {
    onOpenRequests()
    onDismiss()
  }

  return (
    <div
      className="request-sent-toast"
      style={style}
      role="status"
      aria-live="polite"
    >
      <button type="button" className="request-sent-toast__body" onClick={open}>
        <span className="request-sent-toast__title">Request sent</span>
        <span className="request-sent-toast__hint">
          Track it under <strong>Requests</strong> ↑
        </span>
      </button>
      <button
        type="button"
        className="request-sent-toast__close"
        aria-label="Dismiss"
        onClick={onDismiss}
      >
        ×
      </button>
    </div>
  )
}
