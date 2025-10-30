import React, { useState, useEffect, useRef } from "react";
import axios from "axios";
import cytoscape from "cytoscape";
import dagre from "cytoscape-dagre";
import qtip from "cytoscape-qtip";
import { marked } from "marked";
import hljs from "highlight.js";
import "highlight.js/styles/default.css";
import "qtip2/dist/jquery.qtip.min.css";


import FileChangeFrequency from "./FileChangeFrequency";
import CommitActivityTimeline from "./CommitActivityTimeline";
import ContributorStatistics from "./ContributorStatistics";
import CodebaseHeatmap from "./CodebaseHeatmap";
import DependencyGraph from "./DependencyGraph";
import IssueIntegration from "./IssueIntegration";
import SimilarCommits from "./SimilarCommits";
import CommitQA from "./CommitQA";
import CommitSummary from "./CommitSummary";

cytoscape.use(dagre);
cytoscape.use(qtip);
axios.defaults.withCredentials = true;

marked.setOptions({
  highlight: function (code) {
    return hljs.highlightAuto(code).value;
  },
});

export default function AnalysisDashboard() {
  const [repoUrl, setRepoUrl] = useState("");
  const [isAnalyzing, setIsAnalyzing] = useState(false);
  const [isProcessing, setIsProcessing] = useState(false);
  const [statusMessage, setStatusMessage] = useState("");
  const [totalCommits, setTotalCommits] = useState(null);
  const [commitCount, setCommitCount] = useState(null);
  const [analysisId, setAnalysisId] = useState(null);
  const [activeTab, setActiveTab] = useState("ai-similar");
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState(null);
  const [codeEvolution, setCodeEvolution] = useState([]);
  const graphRef = useRef(null);
  const graphInstance = useRef(null);


  const commitTypes = {
    Feature: "#4CAF50",
    Bugfix: "#F44336",
    Refactor: "#2196F3",
    Chore: "#9e9e9e",
    Other: "#9C27B0",
  };

  const isValidGitHubUrl = (url) =>
    /^https:\/\/github\.com\/[\w.-]+\/[\w.-]+(?:\.git)?$/.test(url);

  const fetchTotalCommits = async () => {
    setIsLoading(true);
    try {
      const response = await axios.post(`${import.meta.env.VITE_REACT_APP_API_URL}/api/get-total-commits`, { repoUrl });
      setTotalCommits(response.data.totalCommits);
    } catch {
      setError("Failed to fetch total commits.");
    } finally {
      setIsLoading(false);
    }
  };

  const analyzeRepo = async () => {
    if (!isValidGitHubUrl(repoUrl)) {
      setStatusMessage("Invalid GitHub URL");
      return;
    }

    setIsAnalyzing(true);
    setError(null);
    try {
      const response = await axios.post(`${import.meta.env.VITE_REACT_APP_API_URL}/api/analyze`, { repoUrl });
      if (response.data.status === "success") {
        setAnalysisId(response.data.analysisId);
        setTotalCommits(response.data.totalCommits);
        setStatusMessage("Analysis initialized successfully");
        await fetchAnalysisData(response.data.analysisId);
      }
    } catch (error) {
      setStatusMessage(
        "Error: " + (error.response?.data?.message || error.message)
      );
    } finally {
      setIsAnalyzing(false);
    }
  };

  const processCommits = async () => {
    setIsProcessing(true);
    try {
      const response = await axios.post(`${import.meta.env.VITE_REACT_APP_API_URL}/api/process-commits`, {
        analysisId,
        commitCount,
      });
      setStatusMessage(response.data.message || "Commits processed successfully");
      await fetchAnalysisData(analysisId);
    } catch (error) {
      setStatusMessage(
        "Error processing commits: " +
          (error.response?.data?.message || error.message)
      );
    } finally {
      setIsProcessing(false);
    }
  };

  const fetchAnalysisData = async (id) => {
    try {
      const response = await axios.get(`${import.meta.env.VITE_REACT_APP_API_URL}/api/analysis-data`, {
        params: { analysisId: id },
      });
      if (response.data.status === "success") {
        const { data } = response.data;
        setCodeEvolution(data.codeEvolution || []);
      }
    } catch {
      setError("Failed to fetch analysis data");
    }
  };

  const prepareCytoscapeData = (data) => {
    if (!Array.isArray(data) || data.length === 0) return [];

    const elements = [];

    data.forEach((commit) => {
      const sha = commit?.sha;
      const message = commit?.message || "No message";
      const author = commit?.author?.name || "Unknown";
      const date = commit?.author?.date || "Unknown";
      const type = getCommitType(message.toLowerCase());

      if (sha) {
        elements.push({
          data: {
            id: sha,
            label: truncateLabel(message),
            fullLabel: message,
            author,
            date,
            type,
          },
          group: "nodes",
        });
      }

      if (Array.isArray(commit?.parents)) {
        commit.parents.forEach((p) => {
          if (p?.sha) {
            elements.push({
              data: { id: `${p.sha}-${sha}`, source: p.sha, target: sha },
              group: "edges",
            });
          }
        });
      }
    });

    return elements;
  };

  const getCommitType = (msg) => {
    const types = {
      feat: "Feature",
      fix: "Bugfix",
      refactor: "Refactor",
      chore: "Chore",
      other: "Other",
    };
    const prefix = msg.match(/^(\w+)(\(.+\))?:/)?.[1]?.toLowerCase() || "";
    return types[prefix] || "Other";
  };

  const truncateLabel = (label) =>
    label.length > 20 ? label.slice(0, 20) + "..." : label;

  const renderGraph = (data) => {
    const container = graphRef.current;
    if (!container) return;

    if (graphInstance.current) graphInstance.current.destroy();

    graphInstance.current = cytoscape({
      container,
      elements: data,
      style: [
        {
          selector: "node",
          style: {
            "background-color": (ele) => commitTypes[ele.data("type")],
            label: "data(label)",
            "font-size": 10,
            width: 60,
            height: 60,
          },
        },
        {
          selector: "edge",
          style: {
            width: 2,
            "line-color": "#ccc",
            "target-arrow-color": "#ccc",
            "target-arrow-shape": "triangle",
            "curve-style": "bezier",
          },
        },
      ],
      layout: { name: "dagre", rankDir: "LR" },
    });
  };

  useEffect(() => {
    if (codeEvolution.length && graphRef.current) {
      renderGraph(prepareCytoscapeData(codeEvolution));
    }
  }, [codeEvolution]);

  return (
    <div className="p-6 bg-white min-h-screen">
      <div className="py-5">
        <h1 className="text-3xl font-medium text-center text-gray-800 mb-6">
          Git Analyzer
        </h1>
      </div>
      {/* Repo Input */}
      <div className="flex flex-col md:flex-row justify-center items-center gap-4 mb-6">
        <input
          value={repoUrl}
          onChange={(e) => setRepoUrl(e.target.value)}
          placeholder="Enter GitHub Repository URL"
          className="w-80 px-4 py-2 rounded-md focus:ring-1 focus:ring-blue-500 border border-gray-300"
        />
        <button
          onClick={analyzeRepo}
          disabled={isAnalyzing}
          className="bg-blue-500 text-white px-6 py-2 rounded-md hover:bg-blue-600 disabled:bg-blue-300"
        >
          {isAnalyzing ? "Analyzing..." : "Analyze"}
        </button>
      </div>

      {/* Commit Input */}
      {totalCommits !== null && (
        <div className="flex flex-col items-center gap-3 mb-6">
          <p className="font-semibold">Total Commits: {totalCommits}</p>
          <input
            type="number"
            value={commitCount || ""}
            onChange={(e) => setCommitCount(e.target.value)}
            max={totalCommits}
            placeholder="Enter number of commits to analyze"
            className="px-4 py-2 border rounded-md w-64"
          />
          <button
            onClick={processCommits}
            disabled={isProcessing}
            className="bg-green-600 text-white px-6 py-2 rounded-md hover:bg-green-700 disabled:bg-green-300"
          >
            {isProcessing ? "Processing..." : "Process Commits"}
          </button>
        </div>
      )}

      {/* Status Messages */}
      {statusMessage && (
        <div className="text-center font-semibold text-blue-600 mb-4">
          {statusMessage}
        </div>
      )}

      {/* Graph Section */}
      <div className="bg-gray-50 p-4 rounded-lg shadow mb-8">
        <h2 className="text-xl font-medium mb-3 text-gray-800">
          Code Evolution Analysis
        </h2>
        <div
          ref={graphRef}
          className="w-full h-[400px] border border-gray-200 rounded-lg"
        />
        <div className="flex flex-wrap gap-4 mt-6">
          {Object.entries(commitTypes).map(([type, color]) => (
            <div key={type} className="flex items-center gap-2">
              <span
                className="w-4 h-4 rounded-full"
                style={{ backgroundColor: color }}
              ></span>
              <span className="text-gray-700 text-md font-light">{type}</span>
            </div>
          ))}
        </div>
      </div>

      {/* Tabs */}
      <div className="w-full bg-white shadow rounded-lg p-4 border-t border-gray-300">
        <div className="flex border-b border-gray-300 mb-3">
          {[
            "similar commits",
            "ask questions",
            "repository summary",
            "change frequency",
            "commit activity timeline",
            "contributors",
            "heatmap",
            "dependencies",
            "issues",
          ].map((tab) => (
            <button
              key={tab}
              onClick={() => setActiveTab(tab)}
              className={`px-5 py-2 text-sm transition-colors duration-300 
                ${
                  activeTab === tab
                    ? "border-b-2 border-blue-900 font-semibold text-blue-800"
                    : "text-gray-700 hover:bg-gray-100"
                }`}
            >
              {tab.replace(/-/g, " ").replace(/\b\w/g, (c) => c.toUpperCase())}
            </button>
          ))}
        </div>

        {/* Tab Content */}
        <div className="py-2">
          {activeTab === "similar commits" && <SimilarCommits analysisId={Number(analysisId)} />}
          {activeTab === "ask questions" && <CommitQA analysisId={Number(analysisId)} />}
          {activeTab === "repository summary" && <CommitSummary analysisId={Number(analysisId)} />}
          {activeTab === "change frequency" && (
            <FileChangeFrequency analysisId={Number(analysisId)} />
          )}
          {activeTab === "commit activity timeline" && (
            <CommitActivityTimeline analysisId={Number(analysisId)} />
          )}
          {activeTab === "contributors" && (
            <ContributorStatistics analysisId={Number(analysisId)} />
          )}
          {activeTab === "heatmap" && (
            <CodebaseHeatmap analysisId={Number(analysisId)} />
          )}
          {activeTab === "dependencies" && (
            <DependencyGraph analysisId={Number(analysisId)} />
          )}
          {activeTab === "issues" && (
            <IssueIntegration analysisId={Number(analysisId)} />
          )}
        </div>
      </div>
    </div>
  );
}
