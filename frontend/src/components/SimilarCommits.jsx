import React, { useState } from "react";
import axios from "axios";

const SimilarCommits = ({ analysisId }) => {
  const [searchQuery, setSearchQuery] = useState("");
  const [commits, setCommits] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [hasSearched, setHasSearched] = useState(false);

  const searchCommits = async () => {
    if (!searchQuery.trim()) return;

    setLoading(true);
    setError(null);
    setCommits([]);
    setHasSearched(true);

    try {
      const response = await axios.get(
        `${import.meta.env.VITE_REACT_APP_API_URL}/api/search-commits?query=${encodeURIComponent(searchQuery)}`,
        {
          withCredentials: true,
          params: { analysisId },
        }
      );

      if (response.data.status === "success") {
        setCommits(response.data.results);
      } else {
        throw new Error(response.data.message);
      }
    } catch (err) {
      console.error("Search error:", err);
      setError(err.response?.data?.message || "Failed to search commits");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="p-6 max-w-3xl mx-auto">
      {/* Header */}
      <div className="flex flex-col items-center mb-6">
        <h2 className="text-2xl font-semibold mb-4">Search Similar Commits</h2>
        <div className="flex w-full max-w-lg gap-3">
          <input
            type="text"
            value={searchQuery}
            placeholder="Search commits..."
            onChange={(e) => setSearchQuery(e.target.value)}
            onKeyDown={(e) => e.key === "Enter" && searchCommits()}
            className="flex-1 px-4 py-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-400 focus:outline-none text-base"
          />
          <button
            onClick={searchCommits}
            disabled={!searchQuery.trim() || loading}
            className={`px-5 py-2 rounded-md text-white font-medium transition-transform ${
              loading || !searchQuery.trim()
                ? "bg-blue-300 cursor-not-allowed"
                : "bg-blue-500 hover:bg-blue-600 active:scale-95"
            }`}
          >
            {loading ? "Searching..." : "Search"}
          </button>
        </div>
      </div>

      {/* Error Message */}
      {error && (
        <div className="bg-red-50 text-red-700 px-4 py-3 rounded-md mb-4 text-center">
          {error}
        </div>
      )}

      {/* Loading State */}
      {loading && (
        <div className="text-center text-gray-500 py-4">
          Searching for similar commits...
        </div>
      )}

      {/* Results */}
      {!loading && commits.length > 0 && (
        <div className="space-y-3">
          {commits.map((commit) => (
            <div
              key={commit.commit_hash}
              className="bg-white rounded-lg shadow p-4 border border-gray-100"
            >
              <div className="font-mono text-gray-600 text-sm">
                {commit.commit_hash.substring(0, 7)}
              </div>
              <div className="text-lg font-medium mt-1">
                {commit.commit_message}
              </div>
              <div className="text-blue-500 text-sm mt-2">
                Similarity: {(commit.similarity * 100).toFixed(1)}%
              </div>
            </div>
          ))}
        </div>
      )}

      {/* No Results */}
      {!loading && hasSearched && commits.length === 0 && !error && (
        <div className="text-center text-gray-500 italic py-6">
          No similar commits found
        </div>
      )}
    </div>
  );
};

export default SimilarCommits;
