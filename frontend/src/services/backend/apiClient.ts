// Read API base URL from environment variable
const API_BASE_URL =
	window._env_?.VITE_API_BASE_URL || "http://localhost:1234";

interface RequestOptions extends RequestInit {
	requiresAuth?: boolean;
}

/**
 * Base function to send HTTP requests
 */
async function sendRequest<T>(
	endpoint: string,
	method: string,
	data?: unknown,
	options: RequestOptions = {},
): Promise<T> {
	const { ...fetchOptions } = options;

	// Build full URL
	const url = `${API_BASE_URL}${endpoint}`;

	// Prepare headers
	const headers: Record<string, string> = {
		"Content-Type": "application/json",
		Accept: "application/json",
		...((fetchOptions.headers as Record<string, string>) || {}),
	};

	// Configure request options
	const requestOptions: RequestInit = {
		method,
		headers,
		credentials: "include",
		...fetchOptions,
	};

	// For GET and HEAD requests, do not include body
	if (data && !["GET", "HEAD"].includes(method)) {
		requestOptions.body = JSON.stringify(data);
	}

	try {
		const response = await fetch(url, requestOptions);

		// Check response status
		if (!response.ok) {
			// Try to parse error response
			const errorData = await response.json().catch(() => null);
			throw new Error(
				errorData?.message ||
					`Request failed: ${response.status} ${response.statusText}`,
			);
		}

		// If 204 No Content, return null
		if (response.status === 204) {
			return null as T;
		}

		// Parse JSON response
		return await response.json();
	} catch (error) {
		if (error instanceof Error) {
			throw error;
		}
		throw new Error("Error sending request");
	}
}

// Export HTTP methods
export const apiClient = {
	get: <T>(endpoint: string, options?: RequestOptions) =>
		sendRequest<T>(endpoint, "GET", undefined, options),

	post: <T>(endpoint: string, data: unknown, options?: RequestOptions) =>
		sendRequest<T>(endpoint, "POST", data, options),

	put: <T>(endpoint: string, data: unknown, options?: RequestOptions) =>
		sendRequest<T>(endpoint, "PUT", data, options),

	patch: <T>(endpoint: string, data: unknown, options?: RequestOptions) =>
		sendRequest<T>(endpoint, "PATCH", data, options),

	delete: <T>(endpoint: string, options?: RequestOptions) =>
		sendRequest<T>(endpoint, "DELETE", undefined, options),
};
