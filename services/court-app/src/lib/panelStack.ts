import { useCallback, useState } from 'react'

export type PanelId = 'court' | 'challenges' | 'match' | 'profile' | 'addCourt' | 'account'

const BASE_Z = 20

/** Tracks which overlay was opened most recently; highest entry renders on top. */
export function usePanelStack() {
  const [stack, setStack] = useState<PanelId[]>([])

  const focus = useCallback((id: PanelId) => {
    setStack((prev) => [...prev.filter((x) => x !== id), id])
  }, [])

  const blur = useCallback((id: PanelId) => {
    setStack((prev) => prev.filter((x) => x !== id))
  }, [])

  const zIndex = useCallback(
    (id: PanelId) => {
      const idx = stack.indexOf(id)
      return idx === -1 ? BASE_Z : BASE_Z + (idx + 1) * 10
    },
    [stack],
  )

  return { focus, blur, zIndex, stack }
}
