import { Navigate, Route, Routes } from "react-router-dom";
import { AuthProvider, useAuth } from "./lib/auth-context";
import { useEffect, useState } from "react";
import api from "./lib/api";

function ProtectedRoute({ role, children }) {
  const { auth } = useAuth();

  if (!auth?.token) {
    return <Navigate to="/login" replace />;
  }

  if (role && auth.role !== role) {
    return <Navigate to="/" replace />;
  }

  return children;
}

function Layout({ title, children }) {
  const { auth, logout } = useAuth();
  return (
    <div className="app-shell">
      <header className="app-header">
        <div>
          <p className="eyebrow">Interactive Assignment Submission System</p>
          <h1>{title}</h1>
        </div>
        {auth && (
          <div className="header-actions">
            <span className="badge">{auth.username}</span>
            <span className="badge badge-muted">{auth.role}</span>
            <button className="secondary-button" onClick={logout}>Logout</button>
          </div>
        )}
      </header>
      {children}
    </div>
  );
}

function AuthPage({ mode }) {
  const { login } = useAuth();
  const [form, setForm] = useState({ username: "", password: "", role: "STUDENT" });
  const [error, setError] = useState("");

  const handleSubmit = async (event) => {
    event.preventDefault();
    setError("");
    try {
      const endpoint = mode === "register" ? "/auth/register" : "/auth/login";
      const payload = mode === "register" ? form : { username: form.username, password: form.password };
      const { data } = await api.post(endpoint, payload);
      login(data);
      window.location.href = "/";
    } catch (requestError) {
      setError(requestError.response?.data?.message || "Request failed");
    }
  };

  return (
    <Layout title={mode === "register" ? "Create account" : "Welcome back"}>
      <div className="auth-card">
        <form onSubmit={handleSubmit} className="stack">
          <label>
            Username
            <input
              value={form.username}
              onChange={(event) => setForm({ ...form, username: event.target.value })}
              placeholder="Enter username"
            />
          </label>
          <label>
            Password
            <input
              type="password"
              value={form.password}
              onChange={(event) => setForm({ ...form, password: event.target.value })}
              placeholder="Enter password"
            />
          </label>
          {mode === "register" && (
            <label>
              Role
              <select value={form.role} onChange={(event) => setForm({ ...form, role: event.target.value })}>
                <option value="STUDENT">STUDENT</option>
                <option value="REVIEWER">REVIEWER</option>
              </select>
            </label>
          )}
          {error && <p className="error-text">{error}</p>}
          <button type="submit">{mode === "register" ? "Register" : "Login"}</button>
          <a className="link-text" href={mode === "register" ? "/login" : "/register"}>
            {mode === "register" ? "Already have an account? Login" : "Need an account? Register"}
          </a>
        </form>
      </div>
    </Layout>
  );
}

function StudentDashboard() {
  const [assignments, setAssignments] = useState([]);
  const [form, setForm] = useState({ title: "", description: "" });
  const [error, setError] = useState("");

  const loadAssignments = async () => {
    const { data } = await api.get("/assignments/my");
    setAssignments(data);
  };

  useEffect(() => {
    loadAssignments();
  }, []);

  const handleSubmit = async (event) => {
    event.preventDefault();
    setError("");
    try {
      await api.post("/assignments", form);
      setForm({ title: "", description: "" });
      await loadAssignments();
    } catch (requestError) {
      setError(requestError.response?.data?.message || "Unable to submit assignment");
    }
  };

  return (
    <Layout title="Student dashboard">
      <div className="grid-layout">
        <section className="panel">
          <h2>Submit assignment</h2>
          <form className="stack" onSubmit={handleSubmit}>
            <label>
              Title
              <input value={form.title} onChange={(event) => setForm({ ...form, title: event.target.value })} />
            </label>
            <label>
              Description
              <textarea value={form.description} onChange={(event) => setForm({ ...form, description: event.target.value })} rows="6" />
            </label>
            {error && <p className="error-text">{error}</p>}
            <button type="submit">Submit</button>
          </form>
        </section>
        <section className="panel">
          <h2>My submissions</h2>
          <AssignmentList assignments={assignments} showStudent={false} />
        </section>
      </div>
    </Layout>
  );
}

function ReviewerDashboard() {
  const [assignments, setAssignments] = useState([]);
  const [reviewDrafts, setReviewDrafts] = useState({});

  const loadAssignments = async () => {
    const { data } = await api.get("/assignments");
    setAssignments(data);
  };

  useEffect(() => {
    loadAssignments();
  }, []);

  const handleReview = async (assignmentId) => {
    const draft = reviewDrafts[assignmentId];
    if (!draft?.feedback || draft?.grade === undefined) {
      return;
    }
    await api.put(`/assignments/${assignmentId}/review`, {
      feedback: draft.feedback,
      grade: Number(draft.grade)
    });
    await loadAssignments();
  };

  return (
    <Layout title="Reviewer dashboard">
      <div className="panel">
        <h2>Review assignments</h2>
        <div className="assignment-stack">
          {assignments.map((assignment) => (
            <article key={assignment.id} className="assignment-card">
              <div className="assignment-meta">
                <div>
                  <h3>{assignment.title}</h3>
                  <p>{assignment.description}</p>
                </div>
                <div className="meta-column">
                  <span className="badge">{assignment.status}</span>
                  <span className="badge badge-muted">{assignment.studentUsername}</span>
                </div>
              </div>
              <div className="review-grid">
                <textarea
                  rows="4"
                  placeholder="Feedback"
                  value={reviewDrafts[assignment.id]?.feedback ?? assignment.review?.feedback ?? ""}
                  onChange={(event) =>
                    setReviewDrafts({
                      ...reviewDrafts,
                      [assignment.id]: {
                        ...reviewDrafts[assignment.id],
                        feedback: event.target.value,
                        grade: reviewDrafts[assignment.id]?.grade ?? assignment.review?.grade ?? 0
                      }
                    })
                  }
                />
                <input
                  type="number"
                  min="0"
                  max="100"
                  placeholder="Grade"
                  value={reviewDrafts[assignment.id]?.grade ?? assignment.review?.grade ?? ""}
                  onChange={(event) =>
                    setReviewDrafts({
                      ...reviewDrafts,
                      [assignment.id]: {
                        ...reviewDrafts[assignment.id],
                        feedback: reviewDrafts[assignment.id]?.feedback ?? assignment.review?.feedback ?? "",
                        grade: event.target.value
                      }
                    })
                  }
                />
                <button onClick={() => handleReview(assignment.id)}>Save review</button>
              </div>
            </article>
          ))}
        </div>
      </div>
    </Layout>
  );
}

function AssignmentList({ assignments, showStudent }) {
  if (assignments.length === 0) {
    return <p className="empty-state">No assignments available.</p>;
  }

  return (
    <div className="assignment-stack">
      {assignments.map((assignment) => (
        <article key={assignment.id} className="assignment-card">
          <div className="assignment-meta">
            <div>
              <h3>{assignment.title}</h3>
              <p>{assignment.description}</p>
            </div>
            <div className="meta-column">
              <span className="badge">{assignment.status}</span>
              {showStudent && <span className="badge badge-muted">{assignment.studentUsername}</span>}
            </div>
          </div>
          {assignment.review && (
            <div className="review-summary">
              <strong>Review:</strong> {assignment.review.feedback} ({assignment.review.grade})
            </div>
          )}
        </article>
      ))}
    </div>
  );
}

function HomeRedirect() {
  const { auth } = useAuth();
  if (!auth) {
    return <Navigate to="/login" replace />;
  }
  return <Navigate to={auth.role === "REVIEWER" ? "/reviewer" : "/student"} replace />;
}

export default function App() {
  return (
    <AuthProvider>
      <Routes>
        <Route path="/" element={<HomeRedirect />} />
        <Route path="/login" element={<AuthPage mode="login" />} />
        <Route path="/register" element={<AuthPage mode="register" />} />
        <Route
          path="/student"
          element={
            <ProtectedRoute role="STUDENT">
              <StudentDashboard />
            </ProtectedRoute>
          }
        />
        <Route
          path="/reviewer"
          element={
            <ProtectedRoute role="REVIEWER">
              <ReviewerDashboard />
            </ProtectedRoute>
          }
        />
      </Routes>
    </AuthProvider>
  );
}
