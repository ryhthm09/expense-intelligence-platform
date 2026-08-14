import { apiClient } from "@/lib/api/client";
import type {
  AuthResponse,
  LoginRequest,
  RegisterRequest,
} from "@/types/auth";

export const authApi = {
  login: (data: LoginRequest) =>
    apiClient.post<AuthResponse>("/auth/login", data),

  register: (data: RegisterRequest) =>
    apiClient.post<AuthResponse>("/auth/register", data),

  health: () =>
    apiClient.get<{ status: string; service: string }>("/health"),
};
