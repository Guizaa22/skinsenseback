import React from 'react';
import { HashRouter, Routes, Route, Navigate } from 'react-router-dom';
import Layout from '@/src/components/Layout';
import Auth from '@/src/pages/Auth';
import Dashboard from '@/src/pages/Dashboard';
import ClientFile from '@/src/pages/ClientFile';
import Booking from '@/src/pages/Booking';
import Settings from '@/src/pages/Settings';
import Landing from '@/src/pages/Landing';
import History from '@/src/pages/History';

export default function App() {
  return (
    <HashRouter>
      <Routes>
        <Route path="/" element={<Landing />} />
        <Route path="/login" element={<Auth />} />
        
        <Route path="/app" element={<Layout />}>
          <Route index element={<Dashboard />} />
          <Route path="client-file" element={<ClientFile />} />
          <Route path="booking" element={<Booking />} />
          <Route path="settings" element={<Settings />} />
          <Route path="history" element={<History />} /> 
        </Route>
      </Routes>
    </HashRouter>
  );
}