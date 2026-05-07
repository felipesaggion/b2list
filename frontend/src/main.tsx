import { createRoot } from 'react-dom/client'
import './index.css'
import { BrowserRouter, Route, Routes } from 'react-router-dom'
import Login from './components/login/login-screen.tsx'
import Orders from './components/orders/orders-screen.tsx'
import { createTheme, ThemeProvider } from '@mui/material'
import OrderDetailsScreen from './components/orders/order-details-screen.tsx'

const darkTheme = createTheme({
  palette: {
    mode: 'dark',
  },
});

createRoot(document.getElementById('root')!).render(
    <ThemeProvider theme={darkTheme}>
      <BrowserRouter>
        <Routes>
          <Route path="/" element={<Login />} />
          <Route path="/orders" element={<Orders />} />
          <Route path="/orders/details/:externalReference" element={<OrderDetailsScreen />} />
        </Routes>
      </BrowserRouter>
    </ThemeProvider>,
)
