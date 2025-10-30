import React, { useEffect, useRef } from "react";
import axios from "axios";
import * as d3 from "d3";

const CodebaseHeatmap = ({ analysisId }) => {
  const heatmapRef = useRef(null);

  useEffect(() => {
    const fetchData = async () => {
      try {
        const response = await axios.get(`${import.meta.env.VITE_REACT_APP_API_URL}/api/codebase-heatmap?analysisId=${analysisId}`, {
          withCredentials: true,
        });

        if (response.data.status === "success" && response.data.data) {
          const fileChanges = JSON.parse(response.data.data);

          if (typeof fileChanges === "object" && Object.keys(fileChanges).length > 0) {
            const data = buildHierarchy(fileChanges);
            renderHeatmap(data);
          } else {
            console.warn("No file changes data available");
          }
        }
      } catch (error) {
        console.error("Error fetching codebase heatmap data:", error);
      }
    };

    const buildHierarchy = (fileChanges) => {
      const root = { name: "root", children: [] };

      Object.entries(fileChanges).forEach(([path, count]) => {
        const parts = path.split("/");
        let current = root;

        parts.forEach((part, index) => {
          let child = current.children.find((c) => c.name === part);
          if (!child) {
            child = {
              name: part,
              children: [],
              value: index === parts.length - 1 ? count : undefined,
            };
            current.children.push(child);
          }
          current = child;
        });
      });

      return root;
    };

    const renderHeatmap = (data) => {
      const width = 800;
      const height = 600;
      const svg = d3.select(heatmapRef.current)
        .attr("width", width)
        .attr("height", height);

      svg.selectAll("*").remove();

      const root = d3
        .hierarchy(data)
        .sum((d) => d.value || 0)
        .sort((a, b) => b.value - a.value);

      d3.treemap().size([width, height]).padding(1)(root);

      const maxVal = d3.max(root.leaves(), (d) => d.value);
      const color = d3.scaleSequential()
        .domain([0, maxVal])
        .interpolator(d3.interpolate("#fff9c4", "#ff8a65"));

      svg
        .selectAll("rect")
        .data(root.leaves())
        .enter()
        .append("rect")
        .attr("x", (d) => d.x0)
        .attr("y", (d) => d.y0)
        .attr("width", (d) => d.x1 - d.x0)
        .attr("height", (d) => d.y1 - d.y0)
        .attr("fill", (d) => color(d.value))
        .append("title")
        .text((d) => `${d.data.name}: ${d.value} changes`);

      // Labels (only if space permits)
      svg
        .selectAll("text")
        .data(root.leaves())
        .enter()
        .append("text")
        .attr("x", (d) => d.x0 + 5)
        .attr("y", (d) => d.y0 + 15)
        .text((d) => {
          const width = d.x1 - d.x0;
          return width > 30 ? d.data.name : "";
        })
        .attr("font-size", "10px")
        .attr("fill", "#090900");
    };

    if (analysisId) {
      fetchData();
    }
  }, [analysisId]);

  return (
    <div className="flex flex-col items-center space-y-4 p-4">
      <h2 className="text-lg font-semibold text-gray-800">Codebase Heatmap</h2>
      <svg ref={heatmapRef} className="w-full max-w-4xl border border-gray-200 rounded-md shadow-sm" />

      {/* Legend */}
      <div className="flex justify-center gap-6 mt-3 text-sm text-gray-700">
        <span className="flex items-center gap-2">
          <span className="w-4 h-4 bg-[#fff9c4] border border-gray-300"></span> Low Changes
        </span>
        <span className="flex items-center gap-2">
          <span className="w-4 h-4 bg-[#ffe0b2] border border-gray-300"></span> Medium Changes
        </span>
        <span className="flex items-center gap-2">
          <span className="w-4 h-4 bg-[#ff8a65] border border-gray-300"></span> High Changes
        </span>
      </div>

      <i className="text-gray-500 text-sm italic mt-2">
        Hover over the heatmap for more details
      </i>
    </div>
  );
};

export default CodebaseHeatmap;
