import React, { createContext, useContext, useState, useEffect } from 'react';
import axios from 'axios';

const StoreContext = createContext();

export const StoreProvider = ({ children }) => {
  const [analysisId, setAnalysisId] = useState(
    localStorage.getItem('analysisId') || null
  );
  const [sessionId, setSessionId] = useState(
    localStorage.getItem('sessionId') || null
  );
  const [isAnalyzing, setIsAnalyzing] = useState(false);

  useEffect(() => {
    const init = async () => {
      try {
        const response = await axios.get('/api/session');
        if (response.data.analysisId) {
          setAnalysisId(response.data.analysisId);
          localStorage.setItem('analysisId', response.data.analysisId);
        }
      } catch (error) {
        console.error('Initialization error:', error);
      }
    };
    init();
  }, []);


  const handleSetAnalysisId = (id) => {
    if (!id) return;
    setAnalysisId(Number(id));
    localStorage.setItem('analysisId', id);
    console.log('Analysis ID set:', id);
  };

  const handleSetSessionId = (id) => {
    if (!id) return;
    setSessionId(id);
    localStorage.setItem('sessionId', id);
    console.log('Session ID set:', id);
  };

  const clear = () => {
    localStorage.removeItem('analysisId');
    localStorage.removeItem('sessionId');
    setAnalysisId(null);
    setSessionId(null);
  };

  const value = {
    analysisId,
    sessionId,
    isAnalyzing,
    setIsAnalyzing,
    setAnalysisId: handleSetAnalysisId,
    setSessionId: handleSetSessionId,
    clear,
  };

  return (
    <StoreContext.Provider value={value}>
      {children}
    </StoreContext.Provider>
  );
};


export const useStore = () => {
  return useContext(StoreContext);
};
