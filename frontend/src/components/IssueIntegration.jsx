import React, { useEffect, useState } from "react";
import axios from "axios";

const IssueIntegration = ({ analysisId }) => {
  const [issues, setIssues] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState(null);

  const fetchIssues = async () => {
    try {
      const response = await axios.get(`${import.meta.env.VITE_REACT_APP_API_URL}/api/linked-issues`, {
        withCredentials: true,
        params: { analysisId },
      });
      const fetched = response.data?.data;
      
      if (Array.isArray(fetched)) {
        setIssues(fetched);
      } else if (Array.isArray(response.data)) {
        setIssues(response.data);
      } else if (Array.isArray(fetched?.issues)) {
        setIssues(fetched.issues);
      } else {
        console.warn("Unexpected issues response:", response.data);
        setIssues([]);
      }
    } catch (err) {
      console.error("Error fetching issues:", err);
      setError("Failed to load issues.");
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchIssues();
  }, [analysisId]);

  return (
    <div className="max-w-4xl mx-auto mt-8 p-6 bg-white shadow rounded-lg">
      <h2 className="text-2xl font-semibold text-center text-gray-800 mb-6">
        Issue Tracking
      </h2>

      {error ? (
        <div className="text-center text-red-600 italic">{error}</div>
      ) : isLoading ? (
        <div className="text-center text-blue-500 italic">Loading issues...</div>
      ) : issues.length === 0 ? (
        <div className="text-center text-gray-500 italic">
          No issues available.
        </div>
      ) : (
        <ul className="divide-y divide-gray-200">
          {issues.map((issue) => (
            <li key={issue.id} className="py-3 flex justify-between items-center">
              <a
                href={issue.html_url}
                target="_blank"
                rel="noopener noreferrer"
                className="text-blue-600 hover:underline font-medium"
              >
                {issue.title}
              </a>
              <span
                className={`px-3 py-1 rounded-full text-xs font-semibold text-white ${
                  issue.state === "open"
                    ? "bg-green-600"
                    : issue.state === "closed"
                    ? "bg-red-600"
                    : "bg-gray-500"
                }`}
              >
                {issue.state}
              </span>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
};

export default IssueIntegration;
