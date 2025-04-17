/// <reference types="vite/client" />

interface Window {
  _env_?: {
    [key: `VITE_${string}`]: string | undefined;
  };
}