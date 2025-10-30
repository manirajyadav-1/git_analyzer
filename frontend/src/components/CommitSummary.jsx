import React, { useState } from "react";
import axios from "axios";
import { marked } from "marked";

const CommitSummary = ({ analysisId }) => {
  const [summary, setSummary] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const generateSummary = async () => {
    setLoading(true);
    setError(null);
    setSummary(null);

    try {
      const response = await axios.post(
        `${import.meta.env.VITE_REACT_APP_API_URL}/api/summarize`,
        {},
        {
          withCredentials: true,
          params: { analysisId },
        }
      );

      setSummary(response.data.summary);
    } catch (err) {
      setError(err.response?.data?.message || "Failed to generate summary");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="flex flex-col items-center p-6 bg-gray-50 min-h-screen">
      <div className="w-full max-w-2xl bg-white shadow-md rounded-2xl p-6">
        <h2 className="text-2xl font-semibold text-center text-gray-800 mb-6">
          Repository Summary
        </h2>

        <div className="flex justify-center">
          <button
            onClick={generateSummary}
            disabled={loading}
            className="px-6 py-2 bg-blue-500 text-white rounded-lg hover:bg-blue-600 transition-all duration-200 disabled:bg-blue-300 disabled:cursor-not-allowed"
          >
            {loading ? "Generating..." : "Generate Summary"}
          </button>
        </div>

        {loading && (
          <div className="text-center text-gray-500 italic mt-4">
            Generating summary...
          </div>
        )}

        {error && (
          <div className="text-center text-red-500 font-medium mt-4">
            {error}
          </div>
        )}

        {summary && (
          <div
            className="prose prose-blue bg-white p-5 rounded-xl shadow-sm mt-6 border border-gray-100"
            dangerouslySetInnerHTML={{ __html: marked(summary) }}
          ></div>
        )}
      </div>
    </div>
  );
};

export default CommitSummary;
