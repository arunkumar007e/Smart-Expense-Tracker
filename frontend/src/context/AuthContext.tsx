import { createContext, useCallback, useContext, useEffect, useState } from 'react'
import type { ReactNode } from 'react'
import { authApi } from '../api/authApi'
import { clearToken, getToken, setToken, UNAUTHORIZED_EVENT } from '../api/client'
import type { User } from '../types'

interface AuthContextValue {
  user: User | null
  initializing: boolean
  login: (email: string, password: string) => Promise<void>
  register: (name: string, email: string, password: string) => Promise<void>
  logout: () => void
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined)

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(null)
  const [initializing, setInitializing] = useState(true)

  useEffect(() => {
    const restore = async () => {
      if (getToken()) {
        try {
          setUser(await authApi.me())
        } catch {
          clearToken()
        }
      }
      setInitializing(false)
    }
    restore()

    const onUnauthorized = () => logout()
    window.addEventListener(UNAUTHORIZED_EVENT, onUnauthorized)
    return () => window.removeEventListener(UNAUTHORIZED_EVENT, onUnauthorized)
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  const login = useCallback(async (email: string, password: string) => {
    const response = await authApi.login({ email, password })
    setToken(response.token)
    setUser(response.user)
  }, [])

  const register = useCallback(async (name: string, email: string, password: string) => {
    const response = await authApi.register({ name, email, password })
    setToken(response.token)
    setUser(response.user)
  }, [])

  const logout = useCallback(() => {
    clearToken()
    setUser(null)
  }, [])

  return (
    <AuthContext.Provider value={{ user, initializing, login, register, logout }}>
      {children}
    </AuthContext.Provider>
  )
}

// eslint-disable-next-line react-refresh/only-export-components
export function useAuth(): AuthContextValue {
  const context = useContext(AuthContext)
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider')
  }
  return context
}