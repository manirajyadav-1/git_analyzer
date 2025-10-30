import React, { useEffect, useState, useRef } from "react";
import axios from "axios";
import cytoscape from "cytoscape";

const DependencyGraph = ({ analysisId }) => {
  const [hasData, setHasData] = useState(false);
  const [noData, setNoData] = useState(false);
  const [errorMessage, setErrorMessage] = useState("");
  const graphContainerRef = useRef(null);

  const fetchData = async () => {
    try {
      const response = await axios.get(`${import.meta.env.VITE_REACT_APP_API_URL}/api/dependency-graph`, {
        withCredentials: true,
        params: { analysisId },
      });

      const dependencies = response.data.data || {};

      if (dependencies && Object.keys(dependencies).length > 0) {
        setHasData(true);
        setNoData(false);
        setErrorMessage("");
        renderGraph(buildGraphElements(dependencies));
      } else {
        setHasData(false);
        setNoData(true);
      }
    } catch (error) {
      console.error("Error fetching dependency graph data:", error);
      setHasData(false);
      setErrorMessage("Failed to load dependency data.");
    }
  };

  const buildGraphElements = (deps) => {
    const elements = [];
    const rootNodeId = "root";
    elements.push({ data: { id: rootNodeId, label: "Project" } });

    for (const [dep, version] of Object.entries(deps)) {
      const depId = dep;
      elements.push({ data: { id: depId, label: `${dep}@${version}` } });
      elements.push({ data: { source: rootNodeId, target: depId } });
    }

    return elements;
  };

  const renderGraph = (elements) => {
    cytoscape({
      container: graphContainerRef.current,
      elements,
      style: [
        {
          selector: "node",
          style: {
            label: "data(label)",
            "text-valign": "center",
            "text-halign": "center",
            "background-color": "#2563eb", // Tailwind blue-600
            color: "#fff",
            width: 100,
            height: 100,
            "text-wrap": "wrap",
            "text-max-width": 90,
            "font-size": 10,
            padding: "10px",
          },
        },
        {
          selector: "edge",
          style: {
            "curve-style": "bezier",
            "target-arrow-shape": "triangle",
            width: 2,
            "line-color": "#d1d5db", // Tailwind gray-300
            "target-arrow-color": "#d1d5db",
          },
        },
      ],
      layout: {
        name: "breadthfirst",
      },
    });
  };

  useEffect(() => {
    fetchData();
    // Cleanup cytoscape instance when component unmounts
    return () => {
      if (graphContainerRef.current) {
        graphContainerRef.current.innerHTML = "";
      }
    };
  }, [analysisId]);

  return (
    <div className="max-w-4xl mx-auto mt-8 p-4 bg-white shadow rounded-lg">
      <h2 className="text-2xl font-semibold text-center text-gray-800 mb-4">
        Dependency Graph
      </h2>

      {hasData ? (
        <div
          ref={graphContainerRef}
          className="w-full h-[600px] border border-gray-200 rounded-md"
        />
      ) : noData ? (
        <div className="text-center text-gray-500 italic mt-4">
          No dependency data available.
        </div>
      ) : errorMessage ? (
        <div className="text-center text-red-600 italic mt-4">
          {errorMessage}
        </div>
      ) : (
        <div className="text-center text-gray-500 italic mt-4">
          Loading dependency graph...
        </div>
      )}
    </div>
  );
};

export default DependencyGraph;
