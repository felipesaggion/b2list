import { Logout } from '@mui/icons-material';
import {
    Box, Drawer, AppBar, Toolbar, Typography, CssBaseline,
    ThemeProvider, createTheme, List, ListItem, ListItemButton,
    ListItemIcon, ListItemText
} from '@mui/material';
import { LayoutDashboard, LogOutIcon, ShoppingCart } from 'lucide-react';
import { useNavigate, Outlet } from 'react-router-dom';

const drawerWidth = 200;

const darkTheme = createTheme({
    palette: {
        mode: 'dark',
        background: {
            default: '#071421', // Fundo da página
            paper: '#0B1E2F',   // Fundo do Menu (Paper)
        },
        primary: { main: '#3399FF' },
    },
});

const Layout = () => {
    const navigate = useNavigate();

    const menuItems = [
        { text: 'Dashboard', icon: <LayoutDashboard size={22} />, path: '/dashboard' },
        { text: 'Pedidos', icon: <ShoppingCart size={22} />, path: '/orders' },
        { text: 'Logout', icon: <LogOutIcon size={22} />, path: '/logout' },
    ];

    return (
        <ThemeProvider theme={darkTheme}>
            <Box sx={{ display: 'flex', minHeight: '100vh' }}>
                <CssBaseline />
                <Drawer
                    variant="permanent"
                    anchor="left"
                    sx={{
                        width: drawerWidth,
                        flexShrink: 0,
                        '& .MuiDrawer-paper': {
                            width: drawerWidth,
                            boxSizing: 'border-box',
                            backgroundColor: '#0B1E2F', // Forçando a cor do menu
                            borderRight: '1px solid rgba(255,255,255,0.1)',
                        },
                    }}
                >
                    <Box sx={{ p: 2 }}>
                        <List>
                            {menuItems.map((item) => (
                                <ListItem key={item.text} disablePadding sx={{ mb: 1 }}>
                                    <ListItemButton
                                        onClick={() => navigate(item.path)}
                                        sx={{ borderRadius: '8px', '&:hover': { backgroundColor: 'rgba(51, 153, 255, 0.1)' } }}
                                    >
                                        <ListItemIcon sx={{ color: 'primary.main', minWidth: 45 }}>
                                            {item.icon}
                                        </ListItemIcon>
                                        <ListItemText
                                            primary={item.text}
                                            slotProps={{
                                                primary: {
                                                    sx: {
                                                        fontSize: '14px',
                                                        fontWeight: 500
                                                    }
                                                }
                                            }}
                                        />
                                    </ListItemButton>
                                </ListItem>
                            ))}
                        </List>
                    </Box>
                </Drawer>

                {/* Conteúdo Principal */}
                <Box sx={{
                    display: 'flex',
                    flexDirection: 'column',
                    width: '100%',
                    p: { xs: 1, sm: 3 },
                    boxSizing: 'border-box',
                    overflowX: 'hidden'
                }}>
                    <Toolbar /> {/* Espaçador para o conteúdo não ficar atrás da AppBar */}
                    <Outlet />
                </Box>
            </Box>
        </ThemeProvider>
    );
};

export default Layout;