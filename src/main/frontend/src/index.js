import React from 'react';
import ReactDOM from 'react-dom/client';
import SelectDDLCommand from "./ddl/SelectDDLCommand";

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
