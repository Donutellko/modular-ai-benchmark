import { createContext, useContext, useState, ReactNode } from 'react'

interface ModifiedFilesContextType {
  getModifiedContent: (directory: string, filename: string) => string | null
  setModifiedContent: (directory: string, filename: string, content: string) => void
  clearModifiedContent: (directory: string, filename: string) => void
}

const ModifiedFilesContext = createContext<ModifiedFilesContextType | null>(null)

interface ModifiedFilesProviderProps {
  children: ReactNode
}

export function ModifiedFilesProvider({ children }: ModifiedFilesProviderProps) {
  // Store as: { directory: { filename: content } }
  const [modifiedFiles, setModifiedFiles] = useState<Record<string, Record<string, string>>>({})

  const getModifiedContent = (directory: string, filename: string) => {
    return modifiedFiles[directory]?.[filename] ?? null
  }

  const setModifiedContent = (directory: string, filename: string, content: string) => {
    setModifiedFiles(prev => ({
      ...prev,
      [directory]: {
        ...prev[directory],
        [filename]: content
      }
    }))
  }

  const clearModifiedContent = (directory: string, filename: string) => {
    setModifiedFiles(prev => {
      const newState = { ...prev }
      if (newState[directory]) {
        delete newState[directory][filename]
        if (Object.keys(newState[directory]).length === 0) {
          delete newState[directory]
        }
      }
      return newState
    })
  }

  return (
    <ModifiedFilesContext.Provider value={{
      getModifiedContent,
      setModifiedContent,
      clearModifiedContent
    }}>
      {children}
    </ModifiedFilesContext.Provider>
  )
}

export function useModifiedFiles() {
  const context = useContext(ModifiedFilesContext)
  if (!context) {
    throw new Error('useModifiedFiles must be used within ModifiedFilesProvider')
  }
  return context
}
