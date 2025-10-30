import React, { useState } from "react";
import axios from "axios";
import { marked } from "marked";

const CommitQA = ({ analysisId }) => {
  const [question, setQuestion] = useState("");
  const [answer, setAnswer] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const askQuestion = async () => {
    if (!question.trim()) {
      setError("Please enter a question.");
      return;
    }

    setLoading(true);
    setError("");
    setAnswer("");

    try {
      const response = await axios.post(
        `${import.meta.env.VITE_REACT_APP_API_URL}/api/question-answering`,
        { question }, 
        {
          params: { analysisId }, 
          withCredentials: true, 
          headers: {
            "Content-Type": "application/json",
          },
        }
      );

      if (response.data.status === "success") {
        setAnswer(response.data.answer);
      } else {
        setError(response.data.message || "Failed to get an answer.");
      }
    } catch (err) {
      console.error("Error fetching answer:", err);
      setError(
        err.response?.data?.message ||
          "An error occurred while processing your question."
      );
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="flex flex-col items-center p-6 bg-gray-50 min-h-screen">
      <div className="w-full max-w-2xl bg-white shadow-md rounded-2xl p-6">
        <h2 className="text-2xl font-semibold text-center text-gray-800 mb-6">
          Ask About Commits
        </h2>

        <div className="flex flex-col sm:flex-row gap-3 justify-center mb-6">
          <input
            type="text"
            value={question}
            onChange={(e) => setQuestion(e.target.value)}
            placeholder="Ask a question about the commits..."
            className="flex-1 px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-400 focus:outline-none text-base"
          />
          <button
            onClick={askQuestion}
            disabled={loading}
            className="px-6 py-2 bg-blue-500 text-white rounded-lg hover:bg-blue-600 transition-all duration-200 disabled:bg-blue-300 disabled:cursor-not-allowed"
          >
            {loading ? "Thinking..." : "Ask"}
          </button>
        </div>

        {loading && (
          <p className="text-center text-gray-500 italic mt-4">Thinking...</p>
        )}

        {error && (
          <p className="text-center text-red-500 font-medium mt-4">{error}</p>
        )}

        {answer && (
          <div
            className="prose prose-blue bg-white p-5 rounded-xl shadow-sm mt-6 border border-gray-100"
            dangerouslySetInnerHTML={{ __html: marked.parse(answer) }}
          ></div>
        )}
      </div>
    </div>
  );
};

export default CommitQA;
