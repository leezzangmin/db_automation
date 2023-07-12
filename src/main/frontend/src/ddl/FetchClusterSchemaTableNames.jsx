// import React, { useState, useEffect } from 'react';
//
// const FetchClusterSchemaTableNames = ({ onSelectClusterSchemaTable }) => {
//     const [clusterNames, setClusterNames] = useState([]);
//     const [selectedCluster, setSelectedCluster] = useState('');
//     const [schemaNames, setSchemaNames] = useState([]);
//     const [selectedSchema, setSelectedSchema] = useState('');
//     const [tableNames, setTableNames] = useState([]);
//     const [selectedTable, setSelectedTable] = useState('');
//
//     useEffect(() => {
//         fetchClusters();
//     }, []);
//
//     const fetchClusters = async () => {
//         try {
//             const response = await fetch('/describe/clusters');
//             if (response.ok) {
//                 const data = await response.json();
//                 const names = data.clusters.map((cluster) => cluster.clusterName);
//                 setClusterNames(names);
//             } else {
//                 console.error('Failed to fetch clusters:', response.status);
//             }
//         } catch (error) {
//             console.error('Failed to fetch clusters:', error);
//         }
//     };
//
//     const fetchSchemas = async (clusterName) => {
//         try {
//             const url = `/describe/cluster/schemaNames?databaseName=${clusterName}`;
//             const response = await fetch(url);
//             if (response.ok) {
//                 const data = await response.json();
//                 setSchemaNames(data.schemaNames);
//             } else {
//                 console.error('Failed to fetch schema and tables:', response.status);
//             }
//         } catch (error) {
//             console.error('Failed to fetch schema and tables:', error);
//         }
//     };
//
//     const fetchTables = async (clusterName, schemaName) => {
//         try {
//             const url = `/describe/cluster/schemas?databaseName=${clusterName}&schemaName=${schemaName}`;
//             const response = await fetch(url);
//             if (response.ok) {
//                 const data = await response.json();
//                 const schemaData = data.find((item) => item.schemaName === schemaName);
//                 if (schemaData) {
//                     const tableNames = schemaData.tableInfos.map((table) => table.tableName);
//                     setTableNames(tableNames);
//                 }
//             } else {
//                 console.error('Failed to fetch tables:', response.status);
//             }
//         } catch (error) {
//             console.error('Failed to fetch tables:', error);
//         }
//     };
//
//     const handleClusterChange = (e) => {
//         const selectedCluster = e.target.value;
//         setSelectedCluster(selectedCluster);
//         setSelectedSchema('');
//         setTableNames([]);
//         setSelectedTable('');
//
//         if (selectedCluster) {
//             fetchSchemas(selectedCluster);
//         } else {
//             setSchemaNames([]);
//         }
//
//         onSelectClusterSchemaTable({
//             selectedCluster,
//             selectedSchema: '',
//             selectedTable: ''
//         });
//     };
//
//     const handleSchemaChange = (e) => {
//         const selectedSchema = e.target.value;
//         setSelectedSchema(selectedSchema);
//         setTableNames([]);
//         setSelectedTable('');
//
//         if (selectedCluster && selectedSchema) {
//             fetchTables(selectedCluster, selectedSchema);
//         }
//
//         onSelectClusterSchemaTable({
//             selectedCluster,
//             selectedSchema,
//             selectedTable: ''
//         });
//     };
//
//     const handleTableChange = (e) => {
//         const selectedTable = e.target.value;
//         setSelectedTable(selectedTable);
//         onSelectClusterSchemaTable({
//             selectedCluster,
//             selectedSchema,
//             selectedTable
//         });
//     };
//
//     return (
//         <div>
//             <label>Select Cluster:</label>
//             <select value={selectedCluster} onChange={handleClusterChange}>
//                 <option value="">Select a cluster</option>
//                 {clusterNames.map((name, index) => (
//                     <option key={index} value={name}>
//                         {name}
//                     </option>
//                 ))}
//             </select>
//
//             <label>Select Schema:</label>
//             <select value={selectedSchema} onChange={handleSchemaChange}>
//                 <option value="">Select a schema</option>
//                 {schemaNames.map((name, index) => (
//                     <option key={index} value={name}>
//                         {name}
//                     </option>
//                 ))}
//             </select>
//
//             <label>Select Table:</label>
//             <select value={selectedTable} onChange={handleTableChange}>
//                 <option value="">Select a table</option>
//                 {tableNames.map((name, index) => (
//                     <option key={index} value={name}>
//                         {name}
//                     </option>
//                 ))}
//             </select>
//         </div>
//     );
// };
//
// export default FetchClusterSchemaTableNames;
