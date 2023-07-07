import React from 'react';
import ReactDOM from 'react-dom/client';
import FetchSchemaAndTable from "./FetchSchemaAndTable";
import DDLExecution from "./DDLExecution";
import SelectDDLCommand from "./SelectDDLCommand";

const App = () => {
    return (
        <div>
            <SelectDDLCommand />
        </div>
    );
};

const root = ReactDOM.createRoot(document.getElementById('root'));
root.render(
  <React.StrictMode>
      <App />
  </React.StrictMode>
);
