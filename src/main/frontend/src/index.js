import React from 'react';
import ReactDOM from 'react-dom/client';
import SelectDDLCommand from "./ddl/SelectDDLCommand";
import SelectDescribe from "./describe/SelectDescribe";

const Index = () => {
    return (
        <div>
            <SelectDDLCommand />
            <br/><br/><br/><br/><br/><br/><br/><br/><br/><br/><br/><br/><br/><br/><br/><br/>
            <SelectDescribe />
        </div>
    );
};

const root = ReactDOM.createRoot(document.getElementById('root'));
root.render(
  <React.StrictMode>
      <Index />
  </React.StrictMode>
);


//
// import React from 'react';
// import {BrowserRouter as Router, Route, Link, Routes} from 'react-router-dom';
// import SelectDDLCommand from "./ddl/SelectDDLCommand";
// import SelectDescribe from "./describe/SelectDescribe";
//
// const Index = () => {
//     return (
//         <Router>
//             <div>
//                 <nav>
//                     <ul>
//                         <li>
//                             <Link to="/">Home</Link>
//                         </li>
//                         <li>
//                             <Link to="/selectDDL">Select DDL Command</Link>
//                         </li>
//                         <li>
//                             <Link to="/selectDescribe">Select Describe</Link>
//                         </li>
//                     </ul>
//                 </nav>
//
//                 <Routes>
//                     <Route path="/selectDDL">
//                         <SelectDDLCommand />
//                     </Route>
//
//                     <Route path="/selectDescribe">
//                         <SelectDescribe />
//                     </Route>
//
//                     <Route path="/">
//                         <h1>DB Automation Home Page</h1>
//                     </Route>
//
//                 </Routes>
//             </div>
//         </Router>
//     );
// };
//
// export default Index;
