import React, { useEffect, useState } from "react";
import axios from "axios";

const ContributorStatistics = ({ analysisId }) => {
  const [contributors, setContributors] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState(null);

  const fetchData = async () => {
    try {
      const response = await axios.get(`${import.meta.env.VITE_REACT_APP_API_URL}/api/contributor-statistics`, {
        withCredentials: true,
        params: { analysisId },
      });

      if (response.data.status === "success") {
        const parsedData = JSON.parse(response.data.data);
        setContributors(parsedData || []);
      } else {
        throw new Error(response.data.error || "Unknown error occurred.");
      }
    } catch (err) {
      console.error("Error fetching contributor statistics:", err);
      setError(
        err.response?.data?.error || "Failed to load contributor statistics."
      );
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, [analysisId]);

  return (
    <div className="w-full max-w-7xl mx-auto mt-8 p-4 bg-white shadow rounded-lg">
      <h2 className="text-2xl font-semibold text-center mb-4 text-gray-800">
        Contributor Statistics
      </h2>

      {error ? (
        <div className="text-center text-red-600 italic mt-4">{error}</div>
      ) : isLoading ? (
        <div className="text-center text-gray-500 italic mt-4">
          Loading contributor statistics...
        </div>
      ) : contributors.length === 0 ? (
        <div className="text-center text-gray-500 italic mt-4">
          No contributor data available.
        </div>
      ) : (
        <div className="overflow-x-auto mt-4">
          <table className="min-w-full border border-gray-200">
            <thead>
              <tr className="bg-gray-100">
                <th className="px-4 py-2 text-left font-semibold text-gray-700 border-b">
                  Contributor
                </th>
                <th className="px-4 py-2 text-left font-semibold text-gray-700 border-b">
                  Commits
                </th>
              </tr>
            </thead>
            <tbody>
              {contributors.map((contributor) => (
                <tr
                  key={contributor.login}
                  className="hover:bg-gray-50 transition-colors"
                >
                  <td className="px-4 py-2 border-b text-gray-800">
                    {contributor.login}
                  </td>
                  <td className="px-4 py-2 border-b text-gray-800">
                    {contributor.contributions}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
};

export default ContributorStatistics;
