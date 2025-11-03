import React, { useEffect, useRef, useState } from "react";
import axios from "axios";
import cytoscape from "cytoscape";
import dagre from "cytoscape-dagre";
import qtip from "cytoscape-qtip";
import "qtip2/dist/jquery.qtip.min.css";


if (!cytoscape.prototype.hasQtip) {
  cytoscape.use(dagre);
  cytoscape.use(qtip);
  cytoscape.prototype.hasQtip = true;
}

const DependencyGraph = ({ analysisId }) => {
  const containerRef = useRef(null);
  const [hasData, setHasData] = useState(false);
  const [noData, setNoData] = useState(false);
  const [errorMessage, setErrorMessage] = useState("");

  useEffect(() => {
    const fetchData = async () => {
      try {
        const apiUrl = `${import.meta.env.VITE_REACT_APP_API_URL}/api/dependency-graph?analysisId=${analysisId}`;
        const response = await axios.get(apiUrl);

        if (!response.data || !response.data.data) {
          setNoData(true);
          return;
        }

        const graphData = response.data.data;
        renderGraph(graphData);
        setHasData(true);
      } catch (err) {
        console.error("Error fetching dependency graph data:", err);
        setErrorMessage("Failed to load dependency data.");
      }
    };

    fetchData();
  }, [analysisId]);

  const renderGraph = (graphData) => {
    if (!containerRef.current) return;

    containerRef.current.innerHTML = "";

    const elements = transformData(graphData);

    const cy = cytoscape({
      container: containerRef.current,
      elements,
      style: [
        {
          selector: "node",
          style: {
            "background-color": "#007bff",
            label: "data(label)",
            "text-valign": "center",
            "text-halign": "center",
            color: "#fff",
            "text-outline-width": 2,
            "text-outline-color": "#007bff",
            "font-size": 12,
            width: 200,
            height: 200,
            shape: "ellipse",
          },
        },
        {
          selector: "edge",
          style: {
            width: 5,
            "line-color": "#ccc",
            "target-arrow-color": "#ccc",
            "target-arrow-shape": "triangle",
            "curve-style": "bezier",
          },
        },
        {
          selector: ".root",
          style: {
            "background-color": "#28a745",
            width: 150,
            height: 150,
            "font-size": 18,
            "font-weight": "bold",
          },
        },
      ],
      layout: {
        name: "dagre",
        rankDir: "TB",
        nodeSep: 150,
        rankSep: 100,
        edgeSep: 50,
      },
      wheelSensitivity: 0.2,
    });

    // Keep dependencies node in center and zoom in
    cy.ready(() => {
      cy.fit(undefined, 10);
      cy.zoom(cy.zoom() * 4); 
      const centerNode = cy.$('node[label = "Dependencies"]');
      if (centerNode) cy.center(centerNode);
    });
  };

  const transformData = (data) => {
    const nodes = [];
    const edges = [];

    const root = "Dependencies";

    nodes.push({ data: { id: root, label: root } });

    Object.keys(data).forEach((pkg) => {
      nodes.push({ data: { id: pkg, label: `${pkg} ${data[pkg]}` } });
      edges.push({ data: { source: root, target: pkg } });
    });

    return { nodes, edges };
  };

  return (
    <div className="dependency-graph">
      <h2 className="text-2xl font-semibold text-center mb-4 text-gray-800">
        Dependency Graph
      </h2>

      {hasData ? (
        <div
          ref={containerRef}
          className="w-full max-w-6xl h-[600px] border border-gray-200 rounded-md shadow-sm"
        ></div>
      ) : noData ? (
        <div className="text-gray-500 text-center mt-6">
          No dependency data available.
        </div>
      ) : errorMessage ? (
        <div className="text-red-500 text-center mt-6">{errorMessage}</div>
      ) : (
        <div className="text-gray-500 text-center mt-6">
          Loading dependency graph...
        </div>
      )}
    </div>
  );
};

export default DependencyGraph;
