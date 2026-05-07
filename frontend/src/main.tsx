import { createRoot } from 'react-dom/client'
import './index.css'
import { BrowserRouter, Route, Routes } from 'react-router-dom'
import Login from './components/login/login-screen.tsx'
import Orders from './components/orders/orders-screen.tsx'
import { createTheme, ThemeProvider } from '@mui/material'
import OrderDetailsScreen from './components/orders/order-details-screen.tsx'
import StatisticsDashboard from './components/dashboard/statistics-dashboard.tsx'
import Layout from './components/layout/layout.tsx'
import Logout from './components/login/logout.tsx'

const darkTheme = createTheme({
  palette: {
    mode: 'dark',
  },
});

createRoot(document.getElementById('root')!).render(
  <ThemeProvider theme={darkTheme}>
    <BrowserRouter>
      <Routes>
        <Route element={<Layout />}>
          <Route path="/dashboard" element={<StatisticsDashboard />} />
          <Route path="/orders" element={<Orders />} />
          <Route path="/orders/:externalReference" element={<OrderDetailsScreen />} />
        </Route>
        <Route path="/" element={<Login />} />
        <Route path="/logout" element={<Logout />} />
      </Routes>
    </BrowserRouter>
  </ThemeProvider>,
)
