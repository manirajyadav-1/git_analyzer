import React, { useState, useEffect } from "react";
import axios from "axios";
import {
  Chart as ChartJS,
  Title,
  Tooltip,
  Legend,
  LineElement,
  PointElement,
  CategoryScale,
  LinearScale,
} from "chart.js";
import { Line } from "react-chartjs-2";

ChartJS.register(
  Title,
  Tooltip,
  Legend,
  LineElement,
  PointElement,
  CategoryScale,
  LinearScale
);

const CommitActivityTimeline = ({ analysisId }) => {
  const [commitActivity, setCommitActivity] = useState([]);
  const [chartData, setChartData] = useState(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState(null);

  const chartOptions = {
    responsive: true,
    plugins: {
      legend: {
        position: "top",
      },
      title: {
        display: true,
        text: "Commit Activity Timeline",
      },
    },
    scales: {
      y: {
        beginAtZero: true,
        ticks: { precision: 0 },
      },
    },
  };

  const prepareChartData = (commits) => {
    if (!commits.length) return null;

    const commitsPerDay = {};
    commits.forEach((commit) => {
      const dateStr = new Date(commit.author.date).toLocaleDateString();
      commitsPerDay[dateStr] = (commitsPerDay[dateStr] || 0) + 1;
    });

    const labels = Object.keys(commitsPerDay);
    const data = Object.values(commitsPerDay);

    return {
      labels,
      datasets: [
        {
          label: "Commits per Day",
          data,
          borderColor: "#2196F3",
          tension: 0.1,
          fill: false,
        },
      ],
    };
  };

  const fetchData = async () => {
    try {
      const response = await axios.get(`${import.meta.env.VITE_REACT_APP_API_URL}/api/commit-activity-timeline`, {
        withCredentials: true,
        params: { analysisId },
      });

      if (response.data.status === "success") {
        let rawData = response.data.data;
        if (typeof rawData === "string") {
          try {
            rawData = JSON.parse(rawData);
          } catch {
            console.error("Error parsing commit activity data JSON");
            rawData = [];
          }
        }

        setCommitActivity(rawData || []);
        setChartData(prepareChartData(rawData));
      } else {
        throw new Error(response.data.error || "Unknown error");
      }
    } catch (err) {
      console.error("Error fetching commit activity data:", err);
      setError(err.response?.data?.error || "Failed to load commit activity data.");
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, [analysisId]);

  return (
    <div className="max-w-4xl mx-auto p-6 bg-white shadow-md rounded-2xl mt-6">
      <h2 className="text-2xl font-semibold text-center text-gray-800 mb-4">
        Commit Activity Timeline
      </h2>

      {error && <div className="text-center text-red-500 italic">{error}</div>}

      {isLoading && (
        <div className="text-center text-gray-500 italic">
          Loading commit activity data...
        </div>
      )}

      {!isLoading && !error && commitActivity.length === 0 && (
        <div className="text-center text-gray-500 italic mt-4">
          No commit activity data available.
        </div>
      )}

      {!isLoading && !error && chartData && (
        <div className="mt-6">
          <Line data={chartData} options={chartOptions} />
        </div>
      )}
    </div>
  );
};

export default CommitActivityTimeline;
