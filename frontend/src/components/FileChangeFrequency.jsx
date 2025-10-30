import React, { useEffect, useState } from "react";
import axios from "axios";
import {
  Chart as ChartJS,
  Title,
  Tooltip,
  Legend,
  BarElement,
  CategoryScale,
  LinearScale,
} from "chart.js";
import { Bar } from "react-chartjs-2";

ChartJS.register(Title, Tooltip, Legend, BarElement, CategoryScale, LinearScale);

const FileChangeFrequency = ({ analysisId }) => {
  const [chartData, setChartData] = useState(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState(null);

  const chartOptions = {
    responsive: true,
    plugins: {
      legend: {
        position: "top",
        labels: {
          color: "#374151",
        },
      },
      title: {
        display: true,
        text: "File Change Frequency",
        color: "#111827", 
        font: {
          size: 18,
          weight: "bold",
        },
      },
    },
    scales: {
      y: {
        beginAtZero: true,
        ticks: {
          precision: 0,
          color: "#4b5563", 
        },
        grid: {
          color: "#e5e7eb", 
        },
      },
      x: {
        ticks: {
          color: "#4b5563",
        },
        grid: {
          color: "#f3f4f6", 
        },
      },
    },
  };

  const fetchData = async () => {
    try {
      const response = await axios.get(`${import.meta.env.VITE_REACT_APP_API_URL}/api/file-change-frequency`, {
        withCredentials: true,
        params: { analysisId },
      });

      let rawData = response.data?.data;
      const parsedData = typeof rawData === "string" ? JSON.parse(rawData) : rawData || {};

      const labels = Object.keys(parsedData);
      const data = Object.values(parsedData);

      if (labels.length === 0) {
        setChartData(null);
      } else {
        setChartData({
          labels,
          datasets: [
            {
              label: "Number of Changes",
              data,
              backgroundColor: "rgba(59, 130, 246, 0.6)", 
            },
          ],
        });
      }
    } catch (err) {
      console.error("Error fetching file change frequency data:", err);
      setError("Failed to load file change frequency data.");
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, [analysisId]);

  return (
    <div className="max-w-7xl mx-auto mt-8 p-6 bg-white shadow rounded-lg text-center">
      <h2 className="text-2xl font-semibold text-gray-800 mb-6">
        File Change Frequency
      </h2>

      {error ? (
        <div className="text-red-600 italic mt-4">{error}</div>
      ) : isLoading ? (
        <div className="text-blue-500 italic mt-4">
          Loading file change frequency...
        </div>
      ) : !chartData ? (
        <div className="text-gray-500 italic mt-4">
          No file change data available.
        </div>
      ) : (
        <div className="w-full max-w-5xl mx-auto">
          <Bar data={chartData} options={chartOptions} />
          <p className="italic text-sm text-gray-500 mt-3">
            Hover over the graph for more details
          </p>
        </div>
      )}
    </div>
  );
};

export default FileChangeFrequency;
