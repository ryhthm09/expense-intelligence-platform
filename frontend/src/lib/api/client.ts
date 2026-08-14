import type { ApiError } from "@/types/auth";

const API_BASE_URL =
  process.env.NEXT_PUBLIC_API_URL ?? "http://localhost:8080/api/v1";

export class ApiClientError extends Error {
  constructor(
    public status: number,
    public body: ApiError,
  ) {
    super(body.message);
    this.name = "ApiClientError";
  }
}

interface RequestOptions extends RequestInit {
  token?: string | null;
}

async function request<T>(
  endpoint: string,
  options: RequestOptions = {},
): Promise<T> {
  const { token, headers: customHeaders, ...rest } = options;

  const headers: HeadersInit = {
    "Content-Type": "application/json",
    ...customHeaders,
  };

  if (token) {
    (headers as Record<string, string>)["Authorization"] = `Bearer ${token}`;
  }

  const response = await fetch(`${API_BASE_URL}${endpoint}`, {
    headers,
    ...rest,
  });

  if (!response.ok) {
    const errorBody: ApiError = await response.json().catch(() => ({
      status: response.status,
      error: response.statusText,
      message: "An unexpected error occurred",
    }));
    throw new ApiClientError(response.status, errorBody);
  }

  if (response.status === 204) {
    return undefined as T;
  }

  return response.json();
}

export const apiClient = {
  get: <T>(endpoint: string, token?: string | null) =>
    request<T>(endpoint, { method: "GET", token }),

  post: <T>(endpoint: string, body: unknown, token?: string | null) =>
    request<T>(endpoint, {
      method: "POST",
      body: JSON.stringify(body),
      token,
    }),

  put: <T>(endpoint: string, body: unknown, token?: string | null) =>
    request<T>(endpoint, {
      method: "PUT",
      body: JSON.stringify(body),
      token,
    }),

  delete: <T>(endpoint: string, token?: string | null) =>
    request<T>(endpoint, { method: "DELETE", token }),
};
