"use client";

import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useState,
} from "react";
import { authApi } from "@/lib/api/auth";
import {
  clearAuth,
  getStoredUser,
  getToken,
  setStoredUser,
  setToken,
} from "@/lib/auth/token";
import type { LoginRequest, RegisterRequest, User } from "@/types/auth";
import { ApiClientError } from "@/lib/api/client";

interface AuthContextValue {
  user: User | null;
  isLoading: boolean;
  login: (data: LoginRequest) => Promise<void>;
  register: (data: RegisterRequest) => Promise<void>;
  logout: () => void;
}

const AuthContext = createContext<AuthContextValue | null>(null);

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [user, setUser] = useState<User | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    const token = getToken();
    const storedUser = getStoredUser();
    if (token && storedUser) {
      try {
        setUser(JSON.parse(storedUser));
      } catch {
        clearAuth();
      }
    }
    setIsLoading(false);
  }, []);

  const handleAuthSuccess = useCallback((accessToken: string, userData: User) => {
    setToken(accessToken);
    setStoredUser(JSON.stringify(userData));
    setUser(userData);
  }, []);

  const login = useCallback(
    async (data: LoginRequest) => {
      const response = await authApi.login(data);
      handleAuthSuccess(response.accessToken, response.user);
    },
    [handleAuthSuccess],
  );

  const register = useCallback(
    async (data: RegisterRequest) => {
      const response = await authApi.register(data);
      handleAuthSuccess(response.accessToken, response.user);
    },
    [handleAuthSuccess],
  );

  const logout = useCallback(() => {
    clearAuth();
    setUser(null);
  }, []);

  return (
    <AuthContext.Provider value={{ user, isLoading, login, register, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error("useAuth must be used within an AuthProvider");
  }
  return context;
}

export function getAuthErrorMessage(error: unknown): string {
  if (error instanceof ApiClientError) {
    if (error.body.fieldErrors?.length) {
      return error.body.fieldErrors.map((f) => f.message).join(", ");
    }
    return error.body.message;
  }
  if (error instanceof Error) {
    return error.message;
  }
  return "An unexpected error occurred";
}
