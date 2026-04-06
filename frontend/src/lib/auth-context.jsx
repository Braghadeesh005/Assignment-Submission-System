import { createContext, useContext, useMemo, useState } from "react";
import { clearAuth, getAuth, setAuth } from "./storage";

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [auth, setAuthState] = useState(getAuth());

  const value = useMemo(
    () => ({
      auth,
      login: (data) => {
        setAuth(data);
        setAuthState(data);
      },
      logout: () => {
        clearAuth();
        setAuthState(null);
      }
    }),
    [auth]
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error("useAuth must be used within AuthProvider");
  }
  return context;
}
